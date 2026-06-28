import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { motion } from 'framer-motion'
import {
  Brain,
  Sparkles,
  Zap,
  Shield,
  BarChart3,
  Library,
  ArrowRight,
  Cpu,
  Trophy,
} from 'lucide-react'
import { QuizApi, StatsApi } from '@/api/client'
import Spinner from '@/components/ui/Spinner'
import DifficultyBadge from '@/components/ui/DifficultyBadge'

export default function HomePage() {
  const { data: stats } = useQuery({
    queryKey: ['stats'],
    queryFn: StatsApi.getStats,
  })

  const { data: recent, isLoading: recentLoading } = useQuery({
    queryKey: ['recent-quizzes'],
    queryFn: () => QuizApi.recent(4),
  })

  return (
    <div className="space-y-16">
      <section className="text-center space-y-6">
        <motion.div
          initial={{ scale: 0.9, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ duration: 0.4 }}
          className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-brand-50 border border-brand-200 text-brand-700 text-xs font-semibold"
        >
          <Sparkles className="h-3.5 w-3.5" />
          AI-Powered Quiz Generation
        </motion.div>

        <h1 className="text-4xl sm:text-6xl font-extrabold tracking-tight">
          <span className="text-surface-900">Master Anything With </span>
          <span className="bg-gradient-to-r from-brand-600 via-purple-600 to-fuchsia-600 bg-clip-text text-transparent">
            Smart Quizzes
          </span>
        </h1>

        <p className="max-w-2xl mx-auto text-base sm:text-lg text-surface-600 leading-relaxed">
          Generate intelligent, professionally-crafted multiple-choice quizzes on any topic in
          seconds. Powered by AI with a curated knowledge base — works perfectly even offline.
        </p>

        <div className="flex flex-col sm:flex-row gap-3 justify-center pt-2">
          <Link to="/generate" className="btn-primary">
            <Sparkles className="h-5 w-5" />
            Generate Your First Quiz
          </Link>
          <Link to="/history" className="btn-secondary">
            Browse Past Quizzes
            <ArrowRight className="h-4 w-4" />
          </Link>
        </div>
      </section>

      {stats && (
        <section className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <StatCard
            icon={<Library className="h-5 w-5" />}
            label="Total Quizzes"
            value={stats.totalQuizzes.toString()}
          />
          <StatCard
            icon={<Trophy className="h-5 w-5" />}
            label="Attempts Recorded"
            value={stats.totalAttempts.toString()}
          />
          <StatCard
            icon={<Brain className="h-5 w-5" />}
            label="Curated Topics"
            value={stats.knowledgeBaseSize.toString()}
          />
          <StatCard
            icon={<Cpu className="h-5 w-5" />}
            label="AI Provider"
            value={stats.activeProvider.replace(/-v\d+$/, '')}
            small
          />
        </section>
      )}

      <section className="grid md:grid-cols-3 gap-6">
        <FeatureCard
          icon={<Sparkles className="h-6 w-6" />}
          title="AI-Generated Questions"
          description="Each quiz is uniquely crafted using AI with smart fallback to a curated knowledge base for offline reliability."
          color="brand"
        />
        <FeatureCard
          icon={<Zap className="h-6 w-6" />}
          title="Lightning Fast"
          description="Get a professional 5-question quiz on any topic in seconds. Caching and concurrency keep response times snappy."
          color="amber"
        />
        <FeatureCard
          icon={<Shield className="h-6 w-6" />}
          title="Works Offline"
          description="Firewall-friendly. A built-in trained model ensures the app keeps generating quality questions even without external APIs."
          color="emerald"
        />
        <FeatureCard
          icon={<BarChart3 className="h-6 w-6" />}
          title="Detailed Analytics"
          description="Track scores, view explanations for every answer, and see how you stack up against others on the leaderboard."
          color="purple"
        />
        <FeatureCard
          icon={<Brain className="h-6 w-6" />}
          title="Smart Topic Matching"
          description="Fuzzy keyword matching pulls the best curated questions for your topic, with templated synthesis when needed."
          color="pink"
        />
        <FeatureCard
          icon={<Library className="h-6 w-6" />}
          title="Multiple Difficulties"
          description="Choose Easy, Medium, or Hard. Each question is graded and explanations help you actually learn."
          color="sky"
        />
      </section>

      <section>
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-xl font-bold text-surface-900">Recent Quizzes</h2>
          <Link
            to="/history"
            className="text-sm font-semibold text-brand-600 hover:text-brand-700 inline-flex items-center gap-1"
          >
            View all <ArrowRight className="h-4 w-4" />
          </Link>
        </div>

        {recentLoading ? (
          <Spinner label="Loading recent quizzes..." />
        ) : recent && recent.length > 0 ? (
          <div className="grid sm:grid-cols-2 gap-4">
            {recent.map((quiz) => (
              <Link
                key={quiz.id}
                to={`/quiz/${quiz.id}`}
                className="card p-5 hover:shadow-md hover:border-brand-200 transition-all group"
              >
                <div className="flex items-start justify-between gap-3 mb-3">
                  <h3 className="font-semibold text-surface-900 group-hover:text-brand-700 transition-colors">
                    {quiz.topic}
                  </h3>
                  <DifficultyBadge difficulty={quiz.difficulty} />
                </div>
                <div className="flex items-center gap-3 text-xs text-surface-500">
                  <span>{quiz.questionCount} questions</span>
                  <span>·</span>
                  <span>{quiz.totalAttempts ?? 0} attempts</span>
                  {quiz.category && (
                    <>
                      <span>·</span>
                      <span>{quiz.category}</span>
                    </>
                  )}
                </div>
              </Link>
            ))}
          </div>
        ) : (
          <div className="card p-10 text-center text-surface-500 text-sm">
            No quizzes yet. <Link to="/generate" className="text-brand-600 font-semibold">Create the first one!</Link>
          </div>
        )}
      </section>
    </div>
  )
}

function StatCard({
  icon,
  label,
  value,
  small,
}: {
  icon: React.ReactNode
  label: string
  value: string
  small?: boolean
}) {
  return (
    <div className="card p-5">
      <div className="flex items-center gap-2 text-surface-400 mb-2">{icon}<span className="text-xs uppercase tracking-wider font-semibold">{label}</span></div>
      <div className={`font-bold text-surface-900 ${small ? 'text-lg' : 'text-2xl'}`}>{value}</div>
    </div>
  )
}

const colors = {
  brand: 'bg-brand-50 text-brand-700',
  amber: 'bg-amber-50 text-amber-700',
  emerald: 'bg-emerald-50 text-emerald-700',
  purple: 'bg-purple-50 text-purple-700',
  pink: 'bg-pink-50 text-pink-700',
  sky: 'bg-sky-50 text-sky-700',
}

function FeatureCard({
  icon,
  title,
  description,
  color,
}: {
  icon: React.ReactNode
  title: string
  description: string
  color: keyof typeof colors
}) {
  return (
    <div className="card p-6 hover:shadow-md transition-shadow">
      <div className={`inline-flex p-2.5 rounded-lg mb-4 ${colors[color]}`}>{icon}</div>
      <h3 className="font-bold text-surface-900 mb-1.5">{title}</h3>
      <p className="text-sm text-surface-600 leading-relaxed">{description}</p>
    </div>
  )
}
