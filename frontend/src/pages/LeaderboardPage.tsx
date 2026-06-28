import { useQuery } from '@tanstack/react-query'
import { motion } from 'framer-motion'
import { Trophy, Medal, Award, Sparkles } from 'lucide-react'
import { Link } from 'react-router-dom'
import { AttemptApi } from '@/api/client'
import Spinner from '@/components/ui/Spinner'
import EmptyState from '@/components/ui/EmptyState'

export default function LeaderboardPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['leaderboard'],
    queryFn: () => AttemptApi.leaderboard(50),
  })

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-surface-900 flex items-center gap-2">
          <Trophy className="h-6 w-6 text-amber-500" /> Leaderboard
        </h1>
        <p className="text-sm text-surface-600 mt-0.5">Top quiz performers ranked by accuracy.</p>
      </div>

      {isLoading ? (
        <Spinner size="lg" label="Loading leaderboard..." />
      ) : !data || data.length === 0 ? (
        <EmptyState
          icon={<Trophy className="h-12 w-12" />}
          title="No attempts yet"
          description="Be the first one on the leaderboard — take a quiz!"
          action={
            <Link to="/generate" className="btn-primary">
              <Sparkles className="h-4 w-4" />
              Generate Quiz
            </Link>
          }
        />
      ) : (
        <div className="card overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-surface-50 border-b border-surface-200">
                <tr>
                  <th className="text-left py-3.5 px-4 font-semibold text-surface-600 text-xs uppercase tracking-wider">
                    Rank
                  </th>
                  <th className="text-left py-3.5 px-4 font-semibold text-surface-600 text-xs uppercase tracking-wider">
                    Player
                  </th>
                  <th className="text-left py-3.5 px-4 font-semibold text-surface-600 text-xs uppercase tracking-wider hidden sm:table-cell">
                    Topic
                  </th>
                  <th className="text-center py-3.5 px-4 font-semibold text-surface-600 text-xs uppercase tracking-wider">
                    Score
                  </th>
                  <th className="text-center py-3.5 px-4 font-semibold text-surface-600 text-xs uppercase tracking-wider">
                    Accuracy
                  </th>
                  <th className="text-center py-3.5 px-4 font-semibold text-surface-600 text-xs uppercase tracking-wider hidden md:table-cell">
                    Grade
                  </th>
                </tr>
              </thead>
              <tbody>
                {data.map((entry, idx) => (
                  <motion.tr
                    key={entry.attemptId}
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: idx * 0.03 }}
                    className="border-b border-surface-100 hover:bg-surface-50/60 transition-colors"
                  >
                    <td className="py-3 px-4">
                      <RankBadge rank={entry.rank} />
                    </td>
                    <td className="py-3 px-4">
                      <div className="font-semibold text-surface-900">
                        {entry.userName || 'Anonymous'}
                      </div>
                      <div className="text-xs text-surface-500 sm:hidden">{entry.quizTopic}</div>
                    </td>
                    <td className="py-3 px-4 text-surface-700 hidden sm:table-cell">
                      {entry.quizTopic}
                    </td>
                    <td className="py-3 px-4 text-center font-mono font-semibold">
                      {entry.score}/{entry.totalQuestions}
                    </td>
                    <td className="py-3 px-4 text-center">
                      <span className={`font-bold ${accuracyColor(entry.percentage)}`}>
                        {entry.percentage.toFixed(0)}%
                      </span>
                    </td>
                    <td className="py-3 px-4 text-center hidden md:table-cell">
                      <span className={`badge ${gradeBadge(entry.grade)}`}>{entry.grade}</span>
                    </td>
                  </motion.tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}

function RankBadge({ rank }: { rank: number }) {
  if (rank === 1) {
    return (
      <div className="inline-flex items-center gap-1.5 px-2 py-1 rounded-md bg-gradient-to-br from-amber-400 to-amber-600 text-white font-bold text-xs">
        <Trophy className="h-3.5 w-3.5" /> 1st
      </div>
    )
  }
  if (rank === 2) {
    return (
      <div className="inline-flex items-center gap-1.5 px-2 py-1 rounded-md bg-gradient-to-br from-slate-300 to-slate-500 text-white font-bold text-xs">
        <Medal className="h-3.5 w-3.5" /> 2nd
      </div>
    )
  }
  if (rank === 3) {
    return (
      <div className="inline-flex items-center gap-1.5 px-2 py-1 rounded-md bg-gradient-to-br from-orange-400 to-orange-600 text-white font-bold text-xs">
        <Award className="h-3.5 w-3.5" /> 3rd
      </div>
    )
  }
  return <span className="text-surface-600 font-mono font-semibold">#{rank}</span>
}

function accuracyColor(p: number) {
  if (p >= 80) return 'text-emerald-600'
  if (p >= 60) return 'text-amber-600'
  return 'text-rose-600'
}

function gradeBadge(g: string) {
  if (g.startsWith('A')) return 'bg-emerald-100 text-emerald-700'
  if (g === 'B') return 'bg-sky-100 text-sky-700'
  if (g === 'C') return 'bg-amber-100 text-amber-700'
  return 'bg-rose-100 text-rose-700'
}
