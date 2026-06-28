import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { motion } from 'framer-motion'
import {
  CheckCircle2,
  XCircle,
  Clock,
  RefreshCw,
  ArrowRight,
  Trophy,
  Home,
} from 'lucide-react'
import { AttemptApi } from '@/api/client'
import Spinner from '@/components/ui/Spinner'
import ScoreCircle from '@/components/ui/ScoreCircle'

export default function ResultPage() {
  const { attemptId = '' } = useParams<{ attemptId: string }>()
  const navigate = useNavigate()

  const { data: attempt, isLoading, error } = useQuery({
    queryKey: ['attempt', attemptId],
    queryFn: () => AttemptApi.get(attemptId),
    enabled: Boolean(attemptId),
  })

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-24">
        <Spinner size="xl" label="Loading results..." />
      </div>
    )
  }

  if (error || !attempt) {
    return (
      <div className="card p-8 text-center">
        <h2 className="text-lg font-semibold">Result not found</h2>
        <button onClick={() => navigate('/')} className="btn-primary mt-4">
          Go Home
        </button>
      </div>
    )
  }

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="card p-6 sm:p-8 text-center"
      >
        <div className="inline-flex p-3 rounded-2xl bg-gradient-to-br from-amber-400 to-orange-500 shadow-lg shadow-orange-500/30 mb-4">
          <Trophy className="h-7 w-7 text-white" />
        </div>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-surface-900 mb-1">
          Quiz Complete!
        </h1>
        <p className="text-surface-600 mb-6">
          {attempt.userName ? `Great work, ${attempt.userName}!` : 'Here\'s how you did:'}
        </p>

        <div className="flex justify-center mb-6">
          <ScoreCircle percentage={attempt.percentage} grade={attempt.grade} size={180} />
        </div>

        <div className="grid grid-cols-3 gap-3 max-w-md mx-auto">
          <Stat label="Score" value={`${attempt.score}/${attempt.totalQuestions}`} />
          <Stat label="Correct" value={attempt.score.toString()} accent="emerald" />
          <Stat
            label="Time"
            value={formatTime(attempt.timeTakenSeconds ?? 0)}
            accent="brand"
          />
        </div>

        <div className="text-xs text-surface-500 mt-4 flex items-center justify-center gap-1.5">
          <Clock className="h-3 w-3" />
          {attempt.quizTopic} · {new Date(attempt.completedAt).toLocaleString()}
        </div>
      </motion.div>

      <div className="card p-6 sm:p-7">
        <h2 className="text-lg font-bold text-surface-900 mb-5">Question Breakdown</h2>
        <div className="space-y-4">
          {attempt.questionResults.map((q, idx) => (
            <motion.div
              key={q.questionId}
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: idx * 0.04 }}
              className={`border-2 rounded-lg p-4 sm:p-5 ${
                q.isCorrect ? 'border-emerald-200 bg-emerald-50/30' : 'border-rose-200 bg-rose-50/30'
              }`}
            >
              <div className="flex items-start gap-3 mb-3">
                <div className="shrink-0 mt-0.5">
                  {q.isCorrect ? (
                    <CheckCircle2 className="h-5 w-5 text-emerald-600" />
                  ) : (
                    <XCircle className="h-5 w-5 text-rose-600" />
                  )}
                </div>
                <div className="flex-1">
                  <div className="text-xs uppercase tracking-wider font-semibold text-surface-500 mb-1">
                    Question {idx + 1}
                  </div>
                  <p className="font-semibold text-surface-900 leading-relaxed">{q.questionText}</p>
                </div>
              </div>

              <div className="space-y-1.5 ml-8">
                {q.options.map((o) => {
                  const isCorrect = o.isCorrect
                  const wasSelected = o.wasSelected
                  return (
                    <div
                      key={o.id}
                      className={`flex items-center gap-2 px-3 py-2 rounded-md text-sm ${
                        isCorrect
                          ? 'bg-emerald-100 text-emerald-900 font-semibold'
                          : wasSelected
                          ? 'bg-rose-100 text-rose-900'
                          : 'bg-white text-surface-600'
                      }`}
                    >
                      <span
                        className={`h-6 w-6 rounded text-xs font-bold flex items-center justify-center ${
                          isCorrect
                            ? 'bg-emerald-600 text-white'
                            : wasSelected
                            ? 'bg-rose-600 text-white'
                            : 'bg-surface-200 text-surface-700'
                        }`}
                      >
                        {o.optionLabel}
                      </span>
                      <span className="flex-1">{o.optionText}</span>
                      {isCorrect && <CheckCircle2 className="h-4 w-4 text-emerald-600" />}
                      {!isCorrect && wasSelected && <XCircle className="h-4 w-4 text-rose-600" />}
                    </div>
                  )
                })}
              </div>

              {q.explanation && (
                <div className="ml-8 mt-3 p-3 rounded-md bg-white border border-surface-200">
                  <div className="text-xs font-bold text-surface-500 uppercase tracking-wider mb-1">
                    Explanation
                  </div>
                  <p className="text-sm text-surface-700 leading-relaxed">{q.explanation}</p>
                </div>
              )}
            </motion.div>
          ))}
        </div>
      </div>

      <div className="flex items-center justify-center gap-3 flex-wrap">
        <button onClick={() => navigate('/')} className="btn-secondary">
          <Home className="h-4 w-4" />
          Home
        </button>
        <button onClick={() => navigate(`/quiz/${attempt.quizId}`)} className="btn-secondary">
          <RefreshCw className="h-4 w-4" />
          Retake
        </button>
        <button onClick={() => navigate('/generate')} className="btn-primary">
          New Quiz
          <ArrowRight className="h-4 w-4" />
        </button>
      </div>
    </div>
  )
}

function Stat({
  label,
  value,
  accent,
}: {
  label: string
  value: string
  accent?: 'emerald' | 'brand'
}) {
  const cls =
    accent === 'emerald'
      ? 'text-emerald-700 bg-emerald-50'
      : accent === 'brand'
      ? 'text-brand-700 bg-brand-50'
      : 'text-surface-900 bg-surface-50'
  return (
    <div className={`p-3 rounded-lg ${cls}`}>
      <div className="text-xs uppercase tracking-wider font-semibold opacity-70">{label}</div>
      <div className="text-lg font-bold">{value}</div>
    </div>
  )
}

function formatTime(seconds: number) {
  if (!seconds) return '—'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}
