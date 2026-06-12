import { Client, type StompSubscription } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

let client: Client | null = null
const subscriptions = new Map<string, StompSubscription>()

export function connect(onConnected?: () => void): void {
  if (client?.connected) return

  client = new Client({
    webSocketFactory: () => new SockJS('/ws'),
    connectHeaders: {
      Authorization: `Bearer ${localStorage.getItem('accessToken') ?? ''}`,
    },
    reconnectDelay: 5000,
    onConnect: () => {
      console.log('[STOMP] Connected')
      onConnected?.()
    },
    onDisconnect: () => console.log('[STOMP] Disconnected'),
    onStompError: (frame) => console.error('[STOMP] Error', frame),
  })

  client.activate()
}

export function subscribe(topic: string, callback: (body: unknown) => void): void {
  if (!client?.connected) {
    setTimeout(() => subscribe(topic, callback), 500)
    return
  }
  if (subscriptions.has(topic)) return
  const sub = client.subscribe(topic, (msg) => {
    try {
      callback(JSON.parse(msg.body))
    } catch {
      callback(msg.body)
    }
  })
  subscriptions.set(topic, sub)
}

export function unsubscribe(topic: string): void {
  subscriptions.get(topic)?.unsubscribe()
  subscriptions.delete(topic)
}

export function disconnect(): void {
  client?.deactivate()
  client = null
  subscriptions.clear()
}
