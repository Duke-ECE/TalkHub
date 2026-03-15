import { FormEvent, KeyboardEvent, useEffect, useMemo, useRef, useState } from 'react'

type UserProfile = {
  id: number
  username: string
  nickname: string | null
  avatarUrl: string | null
}

type AuthState = {
  token: string
  user: UserProfile
}

type MessageItem = {
  id: number
  messageId: string
  channelId: number
  senderId: number
  senderUsername: string
  content: string
  status: string
  createdAt: string
}

type ApiErrorResponse = {
  code?: string
  message?: string
  details?: string[]
}

type OnlineUser = {
  userId: number
  username: string
}

const AUTH_STORAGE_KEY = 'talkhub_auth'
const DEFAULT_CHANNEL_ID = 1
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
const imGatewayUrl = import.meta.env.VITE_IM_GATEWAY_URL ?? ''
const imTransport = import.meta.env.VITE_IM_TRANSPORT_HINT ?? 'netty-tcp'

function readAuthFromStorage(): AuthState | null {
  const raw = localStorage.getItem(AUTH_STORAGE_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as AuthState
  } catch {
    localStorage.removeItem(AUTH_STORAGE_KEY)
    return null
  }
}

function formatTime(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

export default function App() {
  const [auth, setAuth] = useState<AuthState | null>(null)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [messages, setMessages] = useState<MessageItem[]>([])
  const [messagesLoading, setMessagesLoading] = useState(false)
  const [messagesError, setMessagesError] = useState('')
  const [composerText, setComposerText] = useState('')
  const [lastRefreshAt, setLastRefreshAt] = useState<string | null>(null)
  const [sendLoading, setSendLoading] = useState(false)
  const [sendError, setSendError] = useState('')
  const [silentRefreshFailed, setSilentRefreshFailed] = useState(false)
  const [onlineUsers, setOnlineUsers] = useState<OnlineUser[]>([])
  const [onlineUsersLoading, setOnlineUsersLoading] = useState(false)
  const [onlineUsersError, setOnlineUsersError] = useState('')
  const messageListRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    setAuth(readAuthFromStorage())
  }, [])

  async function fetchMessages(token: string, silent = false) {
    if (!silent) {
      setMessagesLoading(true)
      setMessagesError('')
    }

    try {
      const resp = await fetch(`${apiBaseUrl}/api/channels/${DEFAULT_CHANNEL_ID}/messages?limit=50`, {
        headers: {
          Authorization: `Bearer ${token}`,
          Accept: 'application/json',
        },
      })

      if (!resp.ok) {
        throw new Error(`history request failed: ${resp.status}`)
      }

      const data = (await resp.json()) as MessageItem[]
      setMessages([...data].reverse())
      setLastRefreshAt(new Date().toISOString())
      setSilentRefreshFailed(false)
    } catch {
      if (silent) {
        setSilentRefreshFailed(true)
      } else {
        setMessagesError('历史消息加载失败，请检查后端服务或鉴权状态。')
      }
    } finally {
      if (!silent) {
        setMessagesLoading(false)
      }
    }
  }

  async function fetchOnlineUsers(token: string, silent = false) {
    if (!silent) {
      setOnlineUsersLoading(true)
      setOnlineUsersError('')
    }

    try {
      const resp = await fetch(`${apiBaseUrl}/api/channels/${DEFAULT_CHANNEL_ID}/online-users`, {
        headers: {
          Authorization: `Bearer ${token}`,
          Accept: 'application/json',
        },
      })

      if (!resp.ok) {
        throw new Error(`presence request failed: ${resp.status}`)
      }

      const data = (await resp.json()) as OnlineUser[]
      setOnlineUsers(data)
    } catch {
      if (!silent) {
        setOnlineUsersError('在线用户加载失败，请确认至少有一个 IM 会话已完成鉴权。')
      }
    } finally {
      if (!silent) {
        setOnlineUsersLoading(false)
      }
    }
  }

  useEffect(() => {
    if (!auth) {
      setMessages([])
      setMessagesError('')
      setLastRefreshAt(null)
      setSilentRefreshFailed(false)
      setOnlineUsers([])
      setOnlineUsersError('')
      return
    }

    void fetchMessages(auth.token)
    void fetchOnlineUsers(auth.token)
    const timer = window.setInterval(() => {
      void fetchMessages(auth.token, true)
      void fetchOnlineUsers(auth.token, true)
    }, 10000)

    return () => window.clearInterval(timer)
  }, [auth])

  useEffect(() => {
    const container = messageListRef.current
    if (!container) return
    container.scrollTop = container.scrollHeight
  }, [messages])

  async function handleLogin(event: FormEvent) {
    event.preventDefault()
    setLoading(true)
    setError('')
    try {
      const resp = await fetch(`${apiBaseUrl}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      })

      if (!resp.ok) {
        setError(resp.status === 401 ? '用户名或密码错误' : '登录失败，请检查后端服务或配置')
        return
      }

      const data = (await resp.json()) as AuthState
      localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(data))
      setAuth(data)
      setUsername('')
      setPassword('')
    } catch {
      setError('无法连接后端，请确认后端服务已启动')
    } finally {
      setLoading(false)
    }
  }

  function handleLogout() {
    localStorage.removeItem(AUTH_STORAGE_KEY)
    setAuth(null)
    setComposerText('')
    setSendError('')
  }

  async function handleSendMessage() {
    if (!auth) return
    const content = composerText.trim()
    if (!content) {
      setSendError('消息内容不能为空。')
      return
    }

    setSendLoading(true)
    setSendError('')
    try {
      const resp = await fetch(`${apiBaseUrl}/api/channels/${DEFAULT_CHANNEL_ID}/messages`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${auth.token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ content }),
      })

      if (!resp.ok) {
        let errorMessage = `send request failed: ${resp.status}`
        try {
          const payload = (await resp.json()) as ApiErrorResponse
          errorMessage = payload.details?.[0] || payload.message || errorMessage
        } catch {
          // Keep fallback message when the backend does not return structured JSON.
        }
        throw new Error(errorMessage)
      }

      const created = (await resp.json()) as MessageItem
      setMessages((current) => [...current, created])
      setComposerText('')
      setLastRefreshAt(new Date().toISOString())
      setSilentRefreshFailed(false)
      void fetchOnlineUsers(auth.token, true)
    } catch (error) {
      setSendError(error instanceof Error ? error.message : '消息发送失败，请检查后端服务。')
    } finally {
      setSendLoading(false)
    }
  }

  function handleComposerKeyDown(event: KeyboardEvent<HTMLTextAreaElement>) {
    if ((event.metaKey || event.ctrlKey) && event.key === 'Enter') {
      event.preventDefault()
      void handleSendMessage()
    }
  }

  const fallbackParticipantNames = useMemo(() => {
    const names = new Set<string>()
    if (auth) {
      names.add(auth.user.nickname || auth.user.username)
    }
    messages.forEach((message) => names.add(message.senderUsername))
    return Array.from(names)
  }, [auth, messages])

  const runtimeCards = [
    { label: 'REST API', value: apiBaseUrl, accent: 'text-teal-700' },
    { label: 'IM Gateway', value: imGatewayUrl || '未配置浏览器适配层', accent: 'text-amber-700' },
    { label: 'Transport', value: imTransport, accent: 'text-slate-700' },
  ]

  const imReady = Boolean(imGatewayUrl)
  const messageCountLabel = `${messages.length} message${messages.length === 1 ? '' : 's'}`

  return (
    <main className="min-h-screen bg-[radial-gradient(circle_at_top_left,_rgba(245,158,11,0.18),_transparent_28%),radial-gradient(circle_at_85%_15%,_rgba(20,184,166,0.18),_transparent_32%),linear-gradient(145deg,_#f8fafc_0%,_#e2e8f0_45%,_#cbd5e1_100%)] text-slate-900">
      <section className="mx-auto min-h-screen max-w-7xl px-5 py-8 sm:px-8">
        <header className="mb-8 flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div className="space-y-3">
            <div className="inline-flex w-fit items-center rounded-full border border-slate-900/10 bg-white/70 px-4 py-2 text-xs font-semibold uppercase tracking-[0.28em] text-teal-700 shadow-sm backdrop-blur">
              TalkHub Frontend Workspace
            </div>
            <div>
              <h1 className="text-4xl font-black tracking-tight sm:text-5xl">Channel cockpit for the Netty MVP.</h1>
              <p className="mt-3 max-w-3xl text-sm leading-7 text-slate-600 sm:text-base">
                前端已接上登录态、频道历史消息和运行态展示。浏览器端即时收发仍依赖 Netty 网关或 HTTP 调试适配层，
                当前界面会把这层缺口直接暴露出来。
              </p>
            </div>
          </div>

          <div className="grid gap-3 sm:grid-cols-3">
            {runtimeCards.map((card) => (
              <div
                key={card.label}
                className="rounded-2xl border border-white/70 bg-white/80 px-4 py-3 shadow-lg shadow-slate-200/60 backdrop-blur"
              >
                <div className="text-xs uppercase tracking-[0.2em] text-slate-400">{card.label}</div>
                <div className={`mt-2 break-all text-sm font-semibold ${card.accent}`}>{card.value}</div>
              </div>
            ))}
          </div>
        </header>

        {!auth ? (
          <section className="grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
            <div className="rounded-[2rem] border border-white/70 bg-slate-950 px-8 py-10 text-white shadow-2xl shadow-slate-400/30">
              <div className="max-w-xl">
                <div className="text-sm uppercase tracking-[0.35em] text-amber-300">Stage 1 UI</div>
                <h2 className="mt-5 text-3xl font-black sm:text-4xl">先拿到 JWT，再进入频道工作台。</h2>
                <p className="mt-5 text-sm leading-7 text-slate-300 sm:text-base">
                  这个前端已经不是单纯的登录落地页。登录成功后会自动恢复会话、读取 `general` 频道历史消息，并展示
                  Netty 运行态与浏览器适配缺口，方便后续继续接 IM 网关。
                </p>
              </div>
            </div>

            <div className="rounded-[2rem] border border-white/70 bg-white/85 p-6 shadow-2xl shadow-slate-300/40 backdrop-blur">
              <form onSubmit={handleLogin} className="space-y-5">
                <div>
                  <div className="text-xs uppercase tracking-[0.24em] text-slate-400">Sign In</div>
                  <h2 className="mt-2 text-3xl font-black text-slate-900">登录</h2>
                </div>

                <div>
                  <label className="mb-2 block text-sm font-medium text-slate-700">用户名</label>
                  <input
                    className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition focus:border-teal-500 focus:bg-white focus:ring-4 focus:ring-teal-500/10"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    placeholder="例如：admin"
                    required
                  />
                </div>

                <div>
                  <label className="mb-2 block text-sm font-medium text-slate-700">密码</label>
                  <input
                    type="password"
                    className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition focus:border-teal-500 focus:bg-white focus:ring-4 focus:ring-teal-500/10"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="输入密码"
                    required
                  />
                </div>

                {error ? <div className="rounded-2xl bg-rose-50 px-4 py-3 text-sm text-rose-700">{error}</div> : null}

                <button
                  type="submit"
                  className="w-full rounded-2xl bg-slate-950 px-5 py-3 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
                  disabled={loading}
                >
                  {loading ? '登录中...' : '进入工作台'}
                </button>

                <div className="rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
                  默认管理员账号来自后端环境变量。登录成功后，JWT 会保存到本地并用于历史消息查询。
                </div>
              </form>
            </div>
          </section>
        ) : (
          <section className="grid gap-5 xl:grid-cols-[280px_minmax(0,1fr)_320px]">
            <aside className="rounded-[2rem] border border-white/70 bg-white/80 p-5 shadow-xl shadow-slate-300/30 backdrop-blur">
              <div className="rounded-[1.5rem] bg-slate-950 p-5 text-white">
                <div className="text-xs uppercase tracking-[0.28em] text-teal-300">Current User</div>
                <div className="mt-3 text-2xl font-black">{auth.user.nickname || auth.user.username}</div>
                <div className="mt-1 text-sm text-slate-300">@{auth.user.username}</div>
                <div className="mt-4 inline-flex rounded-full bg-emerald-400/15 px-3 py-1 text-xs font-semibold text-emerald-300">
                  JWT active
                </div>
              </div>

              <div className="mt-5 space-y-3">
                <div className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-4">
                  <div className="text-xs uppercase tracking-[0.22em] text-slate-400">Channel</div>
                  <div className="mt-2 text-lg font-bold text-slate-900"># general</div>
                  <div className="mt-1 text-sm text-slate-500">默认公共频道，历史消息从 REST API 拉取。</div>
                </div>

                <div className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-4">
                  <div className="text-xs uppercase tracking-[0.22em] text-slate-400">Actions</div>
                  <div className="mt-3 space-y-2">
                    <button
                      type="button"
                      onClick={() => void fetchMessages(auth.token)}
                      className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-700 transition hover:border-teal-500 hover:text-teal-700"
                    >
                      刷新历史消息
                    </button>
                    <button
                      type="button"
                      onClick={() => void fetchOnlineUsers(auth.token)}
                      className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-700 transition hover:border-teal-500 hover:text-teal-700"
                    >
                      刷新在线用户
                    </button>
                    <button
                      type="button"
                      onClick={handleLogout}
                      className="w-full rounded-2xl bg-amber-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-amber-600"
                    >
                      退出登录
                    </button>
                  </div>
                </div>
              </div>
            </aside>

            <section className="rounded-[2rem] border border-white/70 bg-white/85 p-5 shadow-xl shadow-slate-300/30 backdrop-blur">
              <div className="flex flex-col gap-3 border-b border-slate-200 pb-4 sm:flex-row sm:items-end sm:justify-between">
                <div>
                  <div className="text-xs uppercase tracking-[0.22em] text-slate-400">Message Stream</div>
                  <h2 className="mt-2 text-2xl font-black text-slate-900">历史消息</h2>
                </div>
                <div className="text-sm text-slate-500">
                  {lastRefreshAt ? `最近刷新：${formatTime(lastRefreshAt)}` : '尚未加载'}
                </div>
              </div>

              <div className="mt-5 space-y-4">
                {messagesError ? (
                  <div className="rounded-2xl bg-rose-50 px-4 py-3 text-sm text-rose-700">{messagesError}</div>
                ) : null}
                {silentRefreshFailed ? (
                  <div className="rounded-2xl bg-amber-50 px-4 py-3 text-sm text-amber-700">
                    自动刷新失败，当前仍显示最近一次成功加载的历史消息。
                  </div>
                ) : null}

                <div className="flex items-center justify-between rounded-2xl bg-slate-950 px-4 py-3 text-sm text-white">
                  <div className="font-semibold"># general</div>
                  <div className="text-slate-300">{messageCountLabel}</div>
                </div>

                <div ref={messageListRef} className="min-h-[420px] max-h-[520px] space-y-3 overflow-y-auto rounded-[1.75rem] bg-slate-100/80 p-4">
                  {messagesLoading ? (
                    <div className="rounded-2xl border border-dashed border-slate-300 px-4 py-8 text-center text-sm text-slate-500">
                      正在加载频道历史消息...
                    </div>
                  ) : null}

                  {!messagesLoading && messages.length === 0 ? (
                    <div className="rounded-2xl border border-dashed border-slate-300 px-4 py-8 text-center text-sm text-slate-500">
                      当前频道还没有历史消息。
                    </div>
                  ) : null}

                  {messages.map((message) => {
                    const isMine = message.senderId === auth.user.id
                    return (
                      <article
                        key={message.messageId}
                        className={`max-w-3xl rounded-[1.4rem] px-4 py-3 shadow-sm ${
                          isMine
                            ? 'ml-auto bg-slate-950 text-white'
                            : 'bg-white text-slate-900'
                        }`}
                      >
                        <div className={`flex items-center justify-between gap-3 text-xs ${isMine ? 'text-slate-300' : 'text-slate-500'}`}>
                          <span className="font-semibold">{message.senderUsername}</span>
                          <span>{formatTime(message.createdAt)}</span>
                        </div>
                        <p className={`mt-2 text-sm leading-7 ${isMine ? 'text-slate-100' : 'text-slate-700'}`}>
                          {message.content}
                        </p>
                      </article>
                    )
                  })}
                </div>

                <div className="rounded-[1.75rem] border border-slate-200 bg-white p-4">
                  <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                    <div>
                      <div className="text-xs uppercase tracking-[0.2em] text-slate-400">Composer</div>
                      <div className="mt-1 text-sm text-slate-600">
                        {imReady
                          ? '已配置浏览器适配层地址；当前仍优先使用 HTTP 调试发送，后续可以切到实时收发。'
                          : '当前后端是 Netty TCP，自定义协议不能被浏览器直接连接；这里通过 HTTP 调试接口先打通浏览器发送闭环。'}
                      </div>
                    </div>
                    <div className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${
                      imReady ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'
                    }`}>
                      {imReady ? 'Adapter configured' : 'HTTP debug mode'}
                    </div>
                  </div>

                  <div className="mt-4 flex flex-col gap-3 sm:flex-row">
                    <textarea
                      className="min-h-28 flex-1 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-teal-500 focus:bg-white focus:ring-4 focus:ring-teal-500/10 disabled:cursor-not-allowed disabled:opacity-70"
                      value={composerText}
                      onChange={(e) => setComposerText(e.target.value)}
                      onKeyDown={handleComposerKeyDown}
                      placeholder="输入消息，当前会通过 HTTP 调试接口发送。"
                      disabled={sendLoading}
                    />
                    <button
                      type="button"
                      disabled={sendLoading}
                      onClick={() => void handleSendMessage()}
                      className="rounded-2xl bg-slate-950 px-6 py-3 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                      {sendLoading ? '发送中...' : '发送'}
                    </button>
                  </div>
                  <div className="mt-3 text-xs text-slate-400">发送快捷键：`Ctrl+Enter` / `Cmd+Enter`</div>
                  {sendError ? <div className="mt-3 rounded-2xl bg-rose-50 px-4 py-3 text-sm text-rose-700">{sendError}</div> : null}
                </div>
              </div>
            </section>

            <aside className="space-y-5">
              <section className="rounded-[2rem] border border-white/70 bg-white/85 p-5 shadow-xl shadow-slate-300/30 backdrop-blur">
                <div className="text-xs uppercase tracking-[0.22em] text-slate-400">Presence Snapshot</div>
                <h2 className="mt-2 text-2xl font-black text-slate-900">频道成员视图</h2>
                <p className="mt-2 text-sm leading-6 text-slate-600">
                  当前优先展示后端真实在线 IM 会话；如果还没有用户完成 Netty 鉴权，会退回到历史消息推断的占位视图。
                </p>
                {onlineUsersError ? (
                  <div className="mt-4 rounded-2xl bg-amber-50 px-4 py-3 text-sm text-amber-700">{onlineUsersError}</div>
                ) : null}
                <div className="mt-4 space-y-3">
                  {onlineUsersLoading ? (
                    <div className="rounded-2xl border border-dashed border-slate-300 px-4 py-6 text-center text-sm text-slate-500">
                      正在读取在线用户...
                    </div>
                  ) : null}
                  {!onlineUsersLoading && onlineUsers.length > 0
                    ? onlineUsers.map((user) => (
                        <div key={user.userId} className="flex items-center justify-between rounded-2xl bg-slate-100 px-4 py-3">
                          <div className="font-semibold text-slate-800">{user.username}</div>
                          <div className={`rounded-full px-3 py-1 text-xs font-semibold ${
                            user.userId === auth.user.id
                              ? 'bg-emerald-100 text-emerald-700'
                              : 'bg-teal-100 text-teal-700'
                          }`}>
                            {user.userId === auth.user.id ? '当前登录' : 'IM 在线'}
                          </div>
                        </div>
                      ))
                    : null}
                  {!onlineUsersLoading && onlineUsers.length === 0
                    ? fallbackParticipantNames.map((name) => (
                        <div key={name} className="flex items-center justify-between rounded-2xl bg-slate-100 px-4 py-3">
                          <div className="font-semibold text-slate-800">{name}</div>
                          <div className={`rounded-full px-3 py-1 text-xs font-semibold ${
                            name === (auth.user.nickname || auth.user.username)
                              ? 'bg-emerald-100 text-emerald-700'
                              : 'bg-slate-200 text-slate-600'
                          }`}>
                            {name === (auth.user.nickname || auth.user.username) ? '当前登录' : '历史消息'}
                          </div>
                        </div>
                      ))
                    : null}
                </div>
              </section>

              <section className="rounded-[2rem] border border-slate-900/10 bg-slate-950 p-5 text-white shadow-xl shadow-slate-400/30">
                <div className="text-xs uppercase tracking-[0.22em] text-amber-300">Frontend Gap</div>
                <h2 className="mt-2 text-2xl font-black">下一步接入点</h2>
                <ul className="mt-4 space-y-3 text-sm leading-6 text-slate-300">
                  <li>增加浏览器到 Netty 的网关适配层，替代旧的 WebSocket 直连假设。</li>
                  <li>补一个在线用户接口，让右侧成员栏改为真实 presence 数据。</li>
                  <li>把消息发送器接到适配层，再做 ACK、重连和未读态展示。</li>
                </ul>
              </section>
            </aside>
          </section>
        )}
      </section>
    </main>
  )
}
