import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
import { motion, AnimatePresence } from 'framer-motion'
import { ArrowLeft, ArrowRight, CheckCircle2, Clock, Send, AlertCircle, Trophy } from 'lucide-react'
import toast from 'react-hot-toast'
import { AttemptApi, QuizApi } from '@/api/client'
import type { AnswerEntry, SubmitAttemptRequest } from '@/types'
import Spinner from '@/components/ui/Spinner'
import DifficultyBadge from '@/components/ui/DifficultyBadge'

export default function QuizPage() {
  const { quizId = '' } = useParams<{ quizId: string }>()
  const navigate = useNavigate()
  const [currentIndex, setCurrentIndex] = useState(0)
  const [answers, setAnswers] = useState<Record<string, string>>({}) // questionId -> optionId
  const [userName, setUserName] = useState('')
  const [startTime] = useState(() => Date.now())
  const [elapsed, setElapsed] = useState(0)

  useEffect(() => {
    const id = setInterval(() => setElapsed(Math.floor((Date.now() - startTime) / 1000)), 1000)
    return () => clearInterval(id)
  }, [startTime])

  const { data: quiz, isLoading, error } = useQuery({
    queryKey: ['quiz', quizId],
    queryFn: () => QuizApi.getQuiz(quizId),
    enabled: Boolean(quizId),
  })

  const submitMutation = useMutation({
    mutationFn: (req: SubmitAttemptRequest) => AttemptApi.submit(req),
    onSuccess: (result) => {
      toast.success(`Quiz submitted! Score: ${result.score}/${result.totalQuestions}`)
      navigate(`/result/${result.attemptId}`)
    },
    onError: (err: Error) => toast.error(err.message || 'Failed to submit quiz'),
  })

  const totalQuestions = quiz?.questions?.length ?? 0
  const currentQuestion = quiz?.questions[currentIndex]
  const answeredCount = useMemo(() => Object.keys(answers).length, [answers])
  const allAnswered = totalQuestions > 0 && answeredCount === totalQuestions

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-24">
        <Spinner size="xl" label="Loading quiz..." />
      </div>
    )
  }

  if (error || !quiz) {
    return (
      <div className="card p-8 text-center">
        <AlertCircle className="h-10 w-10 text-rose-500 mx-auto mb-2" />
        <h2 className="text-lg font-semibold">Quiz not found</h2>
        <p className="text-sm text-surface-500 mt-1">It may have been deleted.</p>
        <button onClick={() => navigate('/history')} className="btn-primary mt-4">
          Browse Quizzes
        </button>
      </div>
    )
  }

  const selectOption = (questionId: string, optionId: string) => {
    setAnswers((prev) => ({ ...prev, [questionId]: optionId }))
  }

  const onNext = () => {
    if (currentIndex < totalQuestions - 1) {
      setCurrentIndex((i) => i + 1)
    }
  }

  const onPrev = () => {
    if (currentIndex > 0) {
      setCurrentIndex((i) => i - 1)
    }
  }

  const onSubmit = () => {
    if (!allAnswered) {
      const unanswered = totalQuestions - answeredCount
      toast.error(`${unanswered} question${unanswered === 1 ? '' : 's'} unanswered`)
      return
    }
    const entries: AnswerEntry[] = quiz.questions.map((q) => ({
      questionId: q.id,
      selectedOptionId: answers[q.id],
    }))
    submitMutation.mutate({
      quizId: quiz.id,
      userName: userName.trim() || undefined,
      timeTakenSeconds: Math.floor((Date.now() - startTime) / 1000),
      answers: entries,
    })
  }

  const progress = (answeredCount / totalQuestions) * 100

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="card p-5">
        <div className="flex items-start justify-between gap-4 flex-wrap">
          <div className="flex-1 min-w-[200px]">
            <div className="flex items-center gap-2 mb-1.5 flex-wrap">
              <h1 className="text-xl font-bold text-surface-900">{quiz.topic}</h1>
              <DifficultyBadge difficulty={quiz.difficulty} />
              {quiz.category && (
                <span className="badge bg-surface-100 text-surface-700">{quiz.category}</span>
              )}
            </div>
            <div className="text-xs text-surface-500 flex items-center gap-3">
              <span>{totalQuestions} questions</span>
              <span>·</span>
              <span className="flex items-center gap-1">
                <Clock className="h-3 w-3" />
                {formatTime(elapsed)}
              </span>
              {quiz.aiProvider && (
                <>
                  <span>·</span>
                  <span>via {quiz.aiProvider}</span>
                </>
              )}
            </div>
          </div>
          <input
            type="text"
            placeholder="Your name (optional)"
            value={userName}
            onChange={(e) => setUserName(e.target.value)}
            className="input max-w-[200px]"
            maxLength={100}
          />
        </div>
        <div className="mt-4">
          <div className="flex justify-between text-xs font-medium text-surface-500 mb-1">
            <span>
              Progress: {answeredCount}/{totalQuestions}
            </span>
            <span>{Math.round(progress)}%</span>
          </div>
          <div className="h-1.5 bg-surface-100 rounded-full overflow-hidden">
            <motion.div
              className="h-full bg-gradient-to-r from-brand-500 to-purple-600 rounded-full"
              initial={{ width: 0 }}
              animate={{ width: `${progress}%` }}
              transition={{ duration: 0.4 }}
            />
          </div>
        </div>
      </div>

      <div className="flex flex-wrap gap-1.5 justify-center">
        {quiz.questions.map((q, idx) => (
          <button
            key={q.id}
            onClick={() => setCurrentIndex(idx)}
            className={`h-8 w-8 text-xs font-semibold rounded-md transition-all ${
              idx === currentIndex
                ? 'bg-brand-600 text-white ring-2 ring-brand-300'
                : answers[q.id]
                ? 'bg-brand-100 text-brand-700 hover:bg-brand-200'
                : 'bg-surface-100 text-surface-500 hover:bg-surface-200'
            }`}
            aria-label={`Question ${idx + 1}`}
          >
            {idx + 1}
          </button>
        ))}
      </div>

      <AnimatePresence mode="wait">
        {currentQuestion && (
          <motion.div
            key={currentQuestion.id}
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -20 }}
            transition={{ duration: 0.2 }}
            className="card p-6 sm:p-8"
          >
            <div className="text-xs uppercase tracking-wider font-semibold text-brand-600 mb-2">
              Question {currentIndex + 1} of {totalQuestions}
            </div>
            <h2 className="text-lg sm:text-xl font-semibold text-surface-900 leading-relaxed mb-6">
              {currentQuestion.questionText}
            </h2>
            <div className="space-y-2.5">
              {currentQuestion.options.map((opt) => {
                const isSelected = answers[currentQuestion.id] === opt.id
                return (
                  <button
                    key={opt.id}
                    onClick={() => selectOption(currentQuestion.id, opt.id)}
                    className={`w-full text-left p-4 rounded-lg border-2 transition-all ${
                      isSelected
                        ? 'border-brand-500 bg-brand-50/70 ring-2 ring-brand-500/15'
                        : 'border-surface-200 bg-white hover:border-surface-300 hover:bg-surface-50'
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <div
                        className={`shrink-0 h-9 w-9 rounded-md flex items-center justify-center font-bold text-sm ${
                          isSelected
                            ? 'bg-brand-600 text-white'
                            : 'bg-surface-100 text-surface-600'
                        }`}
                      >
                        {opt.optionLabel}
                      </div>
                      <div className="flex-1 text-sm text-surface-800">{opt.optionText}</div>
                      {isSelected && (
                        <CheckCircle2 className="h-5 w-5 text-brand-600 shrink-0" />
                      )}
                    </div>
                  </button>
                )
              })}
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      <div className="flex items-center justify-between gap-3 flex-wrap">
        <button
          onClick={onPrev}
          disabled={currentIndex === 0}
          className="btn-secondary"
        >
          <ArrowLeft className="h-4 w-4" />
          Previous
        </button>

        {currentIndex < totalQuestions - 1 ? (
          <button
            onClick={onNext}
            disabled={!answers[currentQuestion?.id ?? '']}
            className="btn-primary"
          >
            Next
            <ArrowRight className="h-4 w-4" />
          </button>
        ) : (
          <button
            onClick={onSubmit}
            disabled={submitMutation.isPending || !allAnswered}
            className="btn-primary"
          >
            {submitMutation.isPending ? (
              <>
                <Spinner size="sm" />
                Submitting...
              </>
            ) : (
              <>
                <Send className="h-4 w-4" />
                Submit Quiz
              </>
            )}
          </button>
        )}
      </div>

      {!allAnswered && currentIndex === totalQuestions - 1 && (
        <div className="text-center text-xs text-amber-700 bg-amber-50 border border-amber-200 rounded-lg p-3 flex items-center justify-center gap-2">
          <Trophy className="h-4 w-4" />
          {totalQuestions - answeredCount} question{totalQuestions - answeredCount === 1 ? '' : 's'} remaining
        </div>
      )}
    </div>
  )
}

function formatTime(seconds: number) {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}
