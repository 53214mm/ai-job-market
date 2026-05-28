import { Client } from '@stomp/stompjs'
import { ref } from 'vue'

let client = null
let connectPromise = null
const subscriptions = new Map()
const onMessageCallbacks = []
const onUnreadCallbacks = []

const connected = ref(false)

function getToken() {
  return localStorage.getItem('token') || ''
}

function wsUrl() {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  return `${protocol}//${host}/api/ws`
}

export function useStomp() {
  function connect() {
    if (client?.connected) return Promise.resolve()
    if (connectPromise) return connectPromise

    connectPromise = new Promise((resolve, reject) => {
      client = new Client({
        brokerURL: wsUrl(),
        connectHeaders: { token: getToken() },
        debug: () => {},
        reconnectDelay: 5000,
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
      })

      client.onConnect = () => {
        connected.value = true
        subscribe('/user/queue/messages', (msg) => {
          const body = JSON.parse(msg.body)
          onMessageCallbacks.forEach(cb => cb(body))
        })
        subscribe('/user/queue/unread-count', (msg) => {
          const body = JSON.parse(msg.body)
          onUnreadCallbacks.forEach(cb => cb(body.count || 0))
        })
        resolve()
      }

      client.onDisconnect = () => {
        connected.value = false
        connectPromise = null
      }

      client.onWebSocketClose = () => {
        connected.value = false
        connectPromise = null
      }

      client.onStompError = () => {
        connected.value = false
        connectPromise = null
        reject(new Error('STOMP error'))
      }

      client.activate()
    })

    return connectPromise
  }

  function subscribe(destination, handler) {
    if (subscriptions.has(destination)) {
      subscriptions.get(destination).unsubscribe()
    }
    const sub = client.subscribe(destination, handler)
    subscriptions.set(destination, sub)
    return sub
  }

  function disconnect() {
    if (client) {
      subscriptions.forEach(sub => sub.unsubscribe())
      subscriptions.clear()
      client.deactivate()
      client = null
      connectPromise = null
      connected.value = false
    }
  }

  function onMessage(cb) {
    onMessageCallbacks.push(cb)
    return () => {
      const idx = onMessageCallbacks.indexOf(cb)
      if (idx >= 0) onMessageCallbacks.splice(idx, 1)
    }
  }

  function onUnreadCount(cb) {
    onUnreadCallbacks.push(cb)
    return () => {
      const idx = onUnreadCallbacks.indexOf(cb)
      if (idx >= 0) onUnreadCallbacks.splice(idx, 1)
    }
  }

  function sendMessage(payload) {
    if (!client?.connected) return
    client.publish({
      destination: '/app/messages',
      body: JSON.stringify(payload),
    })
  }

  return { connected, connect, disconnect, subscribe, onMessage, onUnreadCount, sendMessage }
}
