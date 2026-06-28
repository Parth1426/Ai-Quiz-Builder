import { NavLink, Outlet } from 'react-router-dom'
import { Brain, Home, Sparkles, History, Trophy, Github } from 'lucide-react'
import { motion } from 'framer-motion'

const navigation = [
  { name: 'Home', href: '/', icon: Home },
  { name: 'Generate', href: '/generate', icon: Sparkles },
  { name: 'History', href: '/history', icon: History },
  { name: 'Leaderboard', href: '/leaderboard', icon: Trophy },
]

export default function Layout() {
  return (
    <div className="min-h-screen flex flex-col">
      <header className="sticky top-0 z-30 border-b border-surface-200/70 bg-white/85 backdrop-blur-md">
        <div className="mx-auto max-w-6xl px-4 sm:px-6 lg:px-8">
          <div className="flex h-16 items-center justify-between">
            <NavLink to="/" className="flex items-center gap-2.5 group">
              <div className="rounded-lg bg-gradient-to-br from-brand-500 to-purple-600 p-2 shadow-lg shadow-brand-500/30 transition-transform group-hover:scale-105">
                <Brain className="h-5 w-5 text-white" />
              </div>
              <div className="flex flex-col leading-tight">
                <span className="text-base font-bold text-surface-900">QuizForge AI</span>
                <span className="text-[10px] uppercase tracking-wider font-semibold text-surface-500">
                  Knowledge Quiz Builder
                </span>
              </div>
            </NavLink>

            <nav className="hidden md:flex items-center gap-1">
              {navigation.map((item) => (
                <NavLink
                  key={item.href}
                  to={item.href}
                  end={item.href === '/'}
                  className={({ isActive }) =>
                    `flex items-center gap-2 px-3.5 py-2 rounded-lg text-sm font-semibold transition-colors ${
                      isActive
                        ? 'text-brand-700 bg-brand-50'
                        : 'text-surface-600 hover:text-surface-900 hover:bg-surface-100'
                    }`
                  }
                >
                  <item.icon className="h-4 w-4" />
                  {item.name}
                </NavLink>
              ))}
            </nav>

            <a
              href="https://github.com"
              target="_blank"
              rel="noopener noreferrer"
              className="hidden md:inline-flex p-2 rounded-lg text-surface-500 hover:text-surface-900 hover:bg-surface-100 transition-colors"
              aria-label="GitHub repository"
            >
              <Github className="h-5 w-5" />
            </a>
          </div>

          <nav className="md:hidden flex items-center gap-1 pb-3 -mx-1 overflow-x-auto">
            {navigation.map((item) => (
              <NavLink
                key={item.href}
                to={item.href}
                end={item.href === '/'}
                className={({ isActive }) =>
                  `flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-semibold whitespace-nowrap ${
                    isActive ? 'text-brand-700 bg-brand-50' : 'text-surface-600 hover:bg-surface-100'
                  }`
                }
              >
                <item.icon className="h-3.5 w-3.5" />
                {item.name}
              </NavLink>
            ))}
          </nav>
        </div>
      </header>

      <main className="flex-1">
        <motion.div
          key={location.pathname}
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.25, ease: 'easeOut' }}
          className="mx-auto max-w-6xl px-4 sm:px-6 lg:px-8 py-8"
        >
          <Outlet />
        </motion.div>
      </main>

      <footer className="border-t border-surface-200/70 bg-white/60 backdrop-blur">
        <div className="mx-auto max-w-6xl px-4 sm:px-6 lg:px-8 py-6 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-surface-500">
          <div>
            &copy; {new Date().getFullYear()} QuizForge AI — Professional Edition
          </div>
          <div className="flex items-center gap-4">
            <span>Spring Boot + React</span>
            <span>·</span>
            <span>AI-Powered</span>
          </div>
        </div>
      </footer>
    </div>
  )
}
