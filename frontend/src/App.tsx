const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
const wsUrl = import.meta.env.VITE_WS_URL ?? 'ws://localhost:8080/ws'

export default function App() {
  return (
    <main className="min-h-screen bg-[radial-gradient(circle_at_top,_rgba(15,118,110,0.18),_transparent_40%),linear-gradient(180deg,_#f8fafc_0%,_#e2e8f0_100%)] text-ink">
      <section className="mx-auto flex min-h-screen max-w-5xl flex-col justify-center px-6 py-16">
        <div className="mb-8 inline-flex w-fit items-center rounded-full border border-surge/20 bg-white/70 px-4 py-2 text-sm font-medium tracking-wide text-surge shadow-sm backdrop-blur">
          TalkHub / Stage 1 Bootstrap
        </div>
        <div className="grid gap-8 lg:grid-cols-[1.35fr_0.9fr]">
          <div className="space-y-6">
            <h1 className="max-w-3xl text-5xl font-black tracking-tight sm:text-6xl">
              Real-time chat foundation for the first delivery slice.
            </h1>
            <p className="max-w-2xl text-lg leading-8 text-slate-600">
              Frontend has been initialized with React, Vite, and Tailwind. The next stage will attach login,
              JWT, WebSocket connectivity, and persisted channel messaging.
            </p>
          </div>
          <div className="rounded-3xl border border-white/60 bg-white/80 p-6 shadow-xl shadow-slate-300/30 backdrop-blur">
            <h2 className="text-xl font-bold">Runtime configuration</h2>
            <div className="mt-4 space-y-4 text-sm text-slate-600">
              <div>
                <div className="font-semibold text-slate-900">API Base URL</div>
                <div className="mt-1 break-all rounded-xl bg-slate-100 px-3 py-2 font-mono">{apiBaseUrl}</div>
              </div>
              <div>
                <div className="font-semibold text-slate-900">WebSocket URL</div>
                <div className="mt-1 break-all rounded-xl bg-slate-100 px-3 py-2 font-mono">{wsUrl}</div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
  )
}

