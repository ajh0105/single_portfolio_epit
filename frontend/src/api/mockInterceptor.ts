import type { InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import http from './http'
import {
  ok, page,
  STATIONS, CHARGERS, VIOLATIONS, PREDICTIONS, ALERTS, MEMBERS, OVERVIEW, RISK_MAP,
  latestHealth, healthHistory, predictionByCharger,
} from './demoData'

type MockAdapter = (config: InternalAxiosRequestConfig) => unknown

interface Route { method: string; pattern: RegExp; handler: MockAdapter }

const routes: Route[] = []

function on(method: string, pattern: RegExp, handler: MockAdapter) {
  routes.push({ method: method.toUpperCase(), pattern, handler })
}

// ── 인증 ──────────────────────────────────────────────────
on('POST', /\/auth\/login/, () => ok({
  accessToken: 'demo-access-token',
  refreshToken: 'demo-refresh-token',
  tokenType: 'Bearer',
  expiresIn: 1800,
  member: { id: 1, username: 'admin', name: '관리자 (데모)', role: 'ADMIN' },
}))
on('POST', /\/auth\/logout/, () => ok(null))
on('POST', /\/auth\/refresh/, () => ok({ accessToken: 'demo-access-token' }))
on('GET',  /\/auth\/me/,      () => ok({ id: 1, username: 'admin', name: '관리자 (데모)', role: 'ADMIN' }))

// ── 대시보드 ───────────────────────────────────────────────
on('GET', /\/dashboard\/overview/, () => ok(OVERVIEW))

// ── 충전소 ─────────────────────────────────────────────────
on('GET', /\/stations$/, () => ok(page(STATIONS, STATIONS.length)))
on('GET', /\/stations\/(\d+)$/, (cfg) => {
  const id = Number(cfg.url!.match(/\/stations\/(\d+)/)![1])
  return ok(STATIONS.find(s => s.id === id) ?? null)
})
on('PATCH', /\/stations\/\d+\/status/, (cfg) => {
  const id = Number(cfg.url!.match(/\/stations\/(\d+)/)![1])
  const s = STATIONS.find(s => s.id === id)
  const status = (cfg.params as Record<string, string>)?.status ?? 'ACTIVE'
  return ok(s ? { ...s, operationStatus: status } : null)
})

// ── 충전기 ─────────────────────────────────────────────────
on('GET', /\/chargers\/station\/(\d+)/, (cfg) => {
  const id = Number(cfg.url!.match(/\/chargers\/station\/(\d+)/)![1])
  return ok(CHARGERS.filter(c => c.stationId === id))
})
on('GET', /\/chargers\/\d+\/health\/latest/, (cfg) => {
  const id = Number(cfg.url!.match(/\/chargers\/(\d+)\/health/)![1])
  return ok(latestHealth(id))
})
on('GET', /\/chargers\/\d+\/health$/, (cfg) => {
  const id = Number(cfg.url!.match(/\/chargers\/(\d+)\/health/)![1])
  const params = cfg.params as Record<string, unknown> | undefined
  let hours = Number(params?.hours ?? 24)
  if (params?.startDate && params?.endDate) {
    const diffMs = new Date(params.endDate as string).getTime() - new Date(params.startDate as string).getTime()
    hours = Math.ceil(diffMs / 3600000)
  }
  hours = Math.min(Math.max(hours, 1), 720)
  return ok(healthHistory(id, hours))
})
on('PATCH', /\/chargers\/\d+\/status/, (cfg) => {
  const id = Number(cfg.url!.match(/\/chargers\/(\d+)\/status/)![1])
  const c = CHARGERS.find(c => c.id === id)
  const status = (cfg.params as Record<string, string>)?.status ?? 'AVAILABLE'
  return ok(c ? { ...c, status } : null)
})

// ── 위반 ───────────────────────────────────────────────────
on('GET', /\/violations$/, (cfg) => {
  const params = cfg.params as Record<string, unknown> | undefined
  const stationId = params?.stationId ? Number(params.stationId) : null
  const list = stationId ? VIOLATIONS.filter(v => v.stationId === stationId) : VIOLATIONS
  return ok(page(list, list.length))
})
on('PATCH', /\/violations\/\d+\/resolve/, (cfg) => {
  const id = Number(cfg.url!.match(/\/violations\/(\d+)\/resolve/)![1])
  const v = VIOLATIONS.find(v => v.id === id)
  return ok(v ? { ...v, status: 'RESOLVED', resolvedAt: new Date().toISOString() } : null)
})

// ── PHM 예측 ───────────────────────────────────────────────
on('GET', /\/predictions\/critical/, () => ok(PREDICTIONS))
on('GET', /\/predictions\/charger\/(\d+)/, (cfg) => {
  const id = Number(cfg.url!.match(/\/predictions\/charger\/(\d+)/)![1])
  return ok(predictionByCharger(id))
})

// ── 교통 위험도 ─────────────────────────────────────────────
on('GET', /\/stations\/\d+\/risk\/latest/, (cfg) => {
  const id = Number(cfg.url!.match(/\/stations\/(\d+)\/risk/)![1])
  return ok(RISK_MAP[id] ?? null)
})

// ── 알림 ───────────────────────────────────────────────────
const alertState = ALERTS.map(a => ({ ...a }))
on('GET', /\/alerts$/, () => ok(page([...alertState].sort((a, b) =>
  new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()), alertState.length)))
on('GET', /\/alerts\/unread-count/, () => ok({ count: alertState.filter(a => !a.isRead).length }))
on('PATCH', /\/alerts\/read-all/, () => {
  alertState.forEach(a => { a.isRead = true }); return ok(null)
})
on('PATCH', /\/alerts\/\d+\/read/, (cfg) => {
  const id = Number(cfg.url!.match(/\/alerts\/(\d+)\/read/)![1])
  const a = alertState.find(a => a.id === id)
  if (a) a.isRead = true
  return ok(null)
})

// ── 사용자 ─────────────────────────────────────────────────
on('GET',    /\/members$/, () => ok(page(MEMBERS, MEMBERS.length)))
on('POST',   /\/members$/, (cfg) => ok({ id: Date.now(), ...(cfg.data ? JSON.parse(cfg.data as string) : {}), enabled: true, createdAt: new Date().toISOString(), lastLoginAt: null }))
on('DELETE', /\/members\/\d+/, () => ok(null))

// ── 인터셉터 등록 ──────────────────────────────────────────
export function setupMockInterceptor() {
  http.interceptors.request.use((config) => {
    const url = config.url ?? ''
    const method = (config.method ?? 'GET').toUpperCase()

    for (const route of routes) {
      if (route.method === method && route.pattern.test(url)) {
        const mockData = route.handler(config)
        config.adapter = (): Promise<AxiosResponse> =>
          Promise.resolve({
            data: mockData,
            status: 200,
            statusText: 'OK',
            headers: {},
            config,
            request: {},
          } as AxiosResponse)
        break
      }
    }
    return config
  })
}
