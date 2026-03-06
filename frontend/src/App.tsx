import { FormEvent, useEffect, useState } from 'react'

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

const AUTH_STORAGE_KEY = 'talkhub_auth'
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
const wsUrl = import.meta.env.VITE_WS_URL ?? 'ws://localhost:8080/ws'

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

export default function App() {
  const [auth, setAuth] = useState<AuthState | null>(null)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    setAuth(readAuthFromStorage())
  }, [])

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
  }

  return (
    <main className="min-h-screen bg-[radial-gradient(circle_at_20%_10%,_rgba(15,118,110,0.2),_transparent_45%),radial-gradient(circle_at_90%_90%,_rgba(249,115,22,0.15),_transparent_35%),linear-gradient(180deg,_#f8fafc_0%,_#e2e8f0_100%)] text-ink">
      <section className="mx-auto flex min-h-screen max-w-5xl flex-col justify-center px-6 py-16">
        <div className="mb-6 inline-flex w-fit items-center rounded-full border border-surge/20 bg-white/70 px-4 py-2 text-sm font-medium tracking-wide text-surge shadow-sm backdrop-blur">
          TalkHub / Stage 1 Login
        </div>
        <div className="grid gap-8 lg:grid-cols-[1.25fr_0.95fr]">
          <div className="space-y-6">
            <h1 className="max-w-3xl text-5xl font-black tracking-tight sm:text-6xl">Login is now live for MVP.</h1>
            <p className="max-w-2xl text-lg leading-8 text-slate-600">
              Backend endpoint <code className="rounded bg-slate-100 px-1">POST /api/auth/login</code> issues JWT.
              Frontend persists token in local storage and restores session after refresh.
            </p>
            <div className="rounded-2xl border border-white/60 bg-white/80 p-4 text-sm shadow-sm">
              <div className="font-semibold text-slate-900">Runtime</div>
              <div className="mt-2 space-y-2 text-slate-700">
                <div className="break-all rounded-xl bg-slate-100 px-3 py-2 font-mono">API: {apiBaseUrl}</div>
                <div className="break-all rounded-xl bg-slate-100 px-3 py-2 font-mono">WS: {wsUrl}</div>
              </div>
            </div>
          </div>

          <div className="rounded-3xl border border-white/60 bg-white/80 p-6 shadow-xl shadow-slate-300/30 backdrop-blur">
            {!auth ? (
              <form onSubmit={handleLogin} className="space-y-4">
                <h2 className="text-2xl font-bold">登录</h2>
                <div>
                  <label className="mb-1 block text-sm font-medium text-slate-700">用户名</label>
                  <input
                    className="w-full rounded-xl border border-slate-300 bg-white px-3 py-2 text-slate-900 outline-none ring-surge/30 focus:ring-2"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    placeholder="例如：alice"
                    required
                  />
                </div>
                <div>
                  <label className="mb-1 block text-sm font-medium text-slate-700">密码</label>
                  <input
                    type="password"
                    className="w-full rounded-xl border border-slate-300 bg-white px-3 py-2 text-slate-900 outline-none ring-surge/30 focus:ring-2"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="输入密码"
                    required
                  />
                </div>
                {error ? <div className="rounded-xl bg-rose-50 px-3 py-2 text-sm text-rose-700">{error}</div> : null}
                <button
                  type="submit"
                  className="w-full rounded-xl bg-surge px-4 py-2 font-semibold text-white transition hover:bg-teal-700 disabled:cursor-not-allowed disabled:opacity-60"
                  disabled={loading}
                >
                  {loading ? '登录中...' : '登录'}
                </button>
                <p className="text-xs text-slate-500">仅支持已初始化账号登录（管理员账号来自环境变量）。</p>
              </form>
            ) : (
              <div className="space-y-4">
                <h2 className="text-2xl font-bold">已登录</h2>
                <div className="rounded-xl bg-slate-100 px-3 py-2 text-sm">
                  <div>id: {auth.user.id}</div>
                  <div>username: {auth.user.username}</div>
                  <div>nickname: {auth.user.nickname || '-'}</div>
                </div>
                <div className="text-sm text-slate-600">
                  token 已保存到 <code className="rounded bg-slate-200 px-1">{AUTH_STORAGE_KEY}</code>
                </div>
                <button
                  type="button"
                  onClick={handleLogout}
                  className="w-full rounded-xl bg-ember px-4 py-2 font-semibold text-white transition hover:bg-orange-600"
                >
                  退出登录
                </button>
              </div>
            )}
          </div>
        </div>
      </section>
    </main>
  )
}
