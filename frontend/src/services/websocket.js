import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

let stompClient = null
const subscriptions = new Map()

export function connectWebSocket(onAlert, onViolation) {
  stompClient = new Client({
    webSocketFactory: () => new SockJS('/ws'),
    reconnectDelay: 5000,

    onConnect: () => {
      console.log('[WebSocket] 연결됨')

      // 전체 알림 채널
      subscriptions.set('alerts',
        stompClient.subscribe('/topic/alerts', msg => {
          onAlert(JSON.parse(msg.body))
        })
      )

      // 불법주차 감지 실시간 피드
      subscriptions.set('violations',
        stompClient.subscribe('/topic/violations', msg => {
          onViolation(JSON.parse(msg.body))
        })
      )
    },

    onDisconnect: () => console.log('[WebSocket] 연결 해제'),
    onStompError: frame => console.error('[WebSocket] 오류:', frame),
  })

  stompClient.activate()
  return stompClient
}

export function subscribeToStation(stationId, callback) {
  if (!stompClient?.connected) return
  const key = `station-${stationId}`
  if (subscriptions.has(key)) return

  subscriptions.set(key,
    stompClient.subscribe(`/topic/stations/${stationId}`, msg => {
      callback(JSON.parse(msg.body))
    })
  )
}

export function disconnectWebSocket() {
  subscriptions.forEach(sub => sub.unsubscribe())
  subscriptions.clear()
  stompClient?.deactivate()
  stompClient = null
}
