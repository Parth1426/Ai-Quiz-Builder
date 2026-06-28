import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery, useQueryClient, useMutation } from '@tanstack/react-query'
import { motion } from 'framer-motion'
import { History, Sparkles, Trash2, Eye, BookOpenText } from 'lucide-react'
import toast from 'react-hot-toast'
import { QuizApi } from '@/api/client'
import Spinner from '@/components/ui/Spinner'
import EmptyState from '@/components/ui/EmptyState'
import DifficultyBadge from '@/components/ui/DifficultyBadge'

export default function HistoryPage() {
  const [page, setPage] = useState(0)
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['quiz-list', page],
    queryFn: () => QuizApi.list(page, 12),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string) => QuizApi.delete(id),
    onSuccess: () => {
      toast.success('Quiz deleted')
      queryClient.invalidateQueries({ queryKey: ['quiz-list'] })
      queryClient.invalidateQueries({ queryKey: ['recent-quizzes'] })
      queryClient.invalidateQueries({ queryKey: ['stats'] })
    },
    onError: () => toast.error('Failed to delete quiz'),
  })

  const handleDelete = (id: string, topic: string) => {
    if (window.confirm(`Delete the quiz on "${topic}"?`)) {
      deleteMutation.mutate(id)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-2xl font-bold text-surface-900 flex items-center gap-2">
            <History className="h-6 w-6 text-brand-600" /> Quiz Library
          </h1>
          <p className="text-sm text-surface-600 mt-0.5">
            Browse, take, or delete previously generated quizzes.
          </p>
        </div>
        <Link to="/generate" className="btn-primary">
          <Sparkles className="h-4 w-4" />
          Generate New
        </Link>
      </div>

      {isLoading ? (
        <Spinner size="lg" label="Loading quizzes..." />
      ) : !data || data.content.length === 0 ? (
        <EmptyState
          icon={<BookOpenText className="h-12 w-12" />}
          title="No quizzes yet"
          description="Generate your first AI-powered quiz to get started."
          action={
            <Link to="/generate" className="btn-primary">
              <Sparkles className="h-4 w-4" />
              Generate Quiz
            </Link>
          }
        />
      ) : (
        <>
          <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {data.content.map((quiz, idx) => (
              <motion.div
                key={quiz.id}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: idx * 0.04 }}
                className="card p-5 flex flex-col"
              >
                <div className="flex items-start justify-between gap-2 mb-3">
                  <h3 className="font-semibold text-surface-900 leading-snug">{quiz.topic}</h3>
                  <DifficultyBadge difficulty={quiz.difficulty} />
                </div>
                <div className="flex flex-wrap items-center gap-2 text-xs text-surface-500 mb-4">
                  <span>{quiz.questionCount} questions</span>
                  <span>·</span>
                  <span>{quiz.totalAttempts ?? 0} attempts</span>
                  {quiz.averageScore !== null && quiz.averageScore !== undefined && (
                    <>
                      <span>·</span>
                      <span>avg {quiz.averageScore.toFixed(1)}%</span>
                    </>
                  )}
                </div>
                {quiz.category && (
                  <span className="badge bg-surface-100 text-surface-700 mb-3 self-start">
                    {quiz.category}
                  </span>
                )}
                <div className="text-[11px] text-surface-400 mb-4">
                  Created {new Date(quiz.createdAt).toLocaleDateString()}
                  {quiz.aiProvider ? ` · ${quiz.aiProvider}` : ''}
                </div>
                <div className="mt-auto flex items-center gap-2">
                  <Link
                    to={`/quiz/${quiz.id}`}
                    className="btn-primary flex-1 py-1.5 text-sm"
                  >
                    <Eye className="h-4 w-4" />
                    Take Quiz
                  </Link>
                  <button
                    onClick={() => handleDelete(quiz.id, quiz.topic)}
                    disabled={deleteMutation.isPending}
                    className="btn-ghost text-rose-600 hover:bg-rose-50 px-2.5 py-1.5"
                    aria-label="Delete quiz"
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>
              </motion.div>
            ))}
          </div>

          {data.totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 mt-6">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={data.first}
                className="btn-secondary text-sm py-1.5"
              >
                Previous
              </button>
              <span className="text-sm text-surface-600 px-3">
                Page {page + 1} of {data.totalPages}
              </span>
              <button
                onClick={() => setPage((p) => p + 1)}
                disabled={data.last}
                className="btn-secondary text-sm py-1.5"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
