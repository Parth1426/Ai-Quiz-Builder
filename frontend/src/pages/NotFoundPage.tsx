import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Home, Compass } from 'lucide-react'

export default function NotFoundPage() {
  return (
    <div className="min-h-[60vh] flex items-center justify-center">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="text-center max-w-md"
      >
        <div className="text-7xl sm:text-8xl font-extrabold bg-gradient-to-br from-brand-500 to-purple-600 bg-clip-text text-transparent">
          404
        </div>
        <h1 className="mt-3 text-2xl font-bold text-surface-900">Page not found</h1>
        <p className="mt-2 text-surface-600">
          We couldn't find what you were looking for. The page may have been moved or no longer exists.
        </p>
        <div className="mt-6 flex items-center justify-center gap-3 flex-wrap">
          <Link to="/" className="btn-primary">
            <Home className="h-4 w-4" />
            Go Home
          </Link>
          <Link to="/generate" className="btn-secondary">
            <Compass className="h-4 w-4" />
            Generate Quiz
          </Link>
        </div>
      </motion.div>
    </div>
  )
}
