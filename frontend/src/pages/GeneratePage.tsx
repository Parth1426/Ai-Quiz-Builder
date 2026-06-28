import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
import { motion } from 'framer-motion'
import { Sparkles, Loader2, Tag, BarChart3, Lightbulb } from 'lucide-react'
import toast from 'react-hot-toast'
import { QuizApi, StatsApi } from '@/api/client'
import type { Difficulty, GenerateQuizRequest } from '@/types'

const DIFFICULTIES: { value: Difficulty; label: string; description: string }[] = [
  { value: 'EASY', label: 'Easy', description: 'Surface-level facts and intro concepts' },
  { value: 'MEDIUM', label: 'Medium', description: 'Deeper understanding and reasoning' },
  { value: 'HARD', label: 'Hard', description: 'Advanced and nuanced questions' },
]

const QUICK_TOPICS = [
  'Photosynthesis',
  'Neural Networks',
  'Ancient Rome',
  'Solar System',
  'World War 2',
  'Java Programming',
  'Spring Boot',
  'Mathematics',
  'Human Body',
  'Geography',
]

export default function GeneratePage() {
  const navigate = useNavigate()
  const [topic, setTopic] = useState('')
  const [questionCount, setQuestionCount] = useState(5)
  const [difficulty, setDifficulty] = useState<Difficulty>('MEDIUM')
  const [category, setCategory] = useState<string>('')

  const { data: curatedTopics } = useQuery({
    queryKey: ['curated-topics'],
    queryFn: StatsApi.curatedTopics,
  })

  const { data: categories } = useQuery({
    queryKey: ['categories'],
    queryFn: StatsApi.categories,
  })

  const generateMutation = useMutation({
    mutationFn: (req: GenerateQuizRequest) => QuizApi.generate(req),
    onSuccess: (quiz) => {
      toast.success(`Quiz on "${quiz.topic}" generated!`)
      navigate(`/quiz/${quiz.id}`)
    },
    onError: (error: Error) => {
      toast.error(error.message || 'Failed to generate quiz')
    },
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!topic.trim()) {
      toast.error('Please enter a topic')
      return
    }
    generateMutation.mutate({
      topic: topic.trim(),
      questionCount,
      difficulty,
      category: category || undefined,
    })
  }

  const isLoading = generateMutation.isPending

  return (
    <div className="max-w-3xl mx-auto space-y-8">
      <div className="text-center">
        <motion.div
          initial={{ scale: 0.9, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          className="inline-flex p-3 rounded-2xl bg-gradient-to-br from-brand-500 to-purple-600 shadow-lg shadow-brand-500/30 mb-4"
        >
          <Sparkles className="h-7 w-7 text-white" />
        </motion.div>
        <h1 className="text-3xl sm:text-4xl font-extrabold text-surface-900 mb-2">
          Generate a Quiz
        </h1>
        <p className="text-surface-600">
          Enter any topic and we'll build a tailored multiple-choice quiz for you.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="card p-6 sm:p-8 space-y-6">
        <div>
          <label htmlFor="topic" className="label">
            Quiz Topic <span className="text-rose-500">*</span>
          </label>
          <input
            id="topic"
            type="text"
            value={topic}
            onChange={(e) => setTopic(e.target.value)}
            placeholder='e.g. "Photosynthesis", "Neural Networks", "Ancient Rome"'
            className="input"
            disabled={isLoading}
            maxLength={200}
            required
          />
          {curatedTopics && curatedTopics.length > 0 && (
            <div className="mt-3">
              <div className="text-xs font-semibold text-surface-500 uppercase tracking-wider mb-2 flex items-center gap-1.5">
                <Lightbulb className="h-3.5 w-3.5" />
                Quick suggestions
              </div>
              <div className="flex flex-wrap gap-2">
                {QUICK_TOPICS.filter((t) => curatedTopics.includes(t))
                  .slice(0, 8)
                  .map((t) => (
                    <button
                      key={t}
                      type="button"
                      onClick={() => setTopic(t)}
                      disabled={isLoading}
                      className="px-3 py-1.5 text-xs font-semibold rounded-full bg-surface-100 hover:bg-brand-100 hover:text-brand-700 transition-colors text-surface-700 border border-surface-200 hover:border-brand-300"
                    >
                      {t}
                    </button>
                  ))}
              </div>
            </div>
          )}
        </div>

        <div>
          <label className="label flex items-center gap-1.5">
            <BarChart3 className="h-4 w-4" />
            Difficulty
          </label>
          <div className="grid grid-cols-3 gap-2">
            {DIFFICULTIES.map((d) => (
              <button
                key={d.value}
                type="button"
                onClick={() => setDifficulty(d.value)}
                disabled={isLoading}
                className={`p-3 rounded-lg border-2 text-left transition-all ${
                  difficulty === d.value
                    ? 'border-brand-500 bg-brand-50/60 ring-2 ring-brand-500/20'
                    : 'border-surface-200 bg-white hover:border-surface-300'
                }`}
              >
                <div className="font-semibold text-sm text-surface-900">{d.label}</div>
                <div className="text-xs text-surface-500 mt-0.5 leading-tight">{d.description}</div>
              </button>
            ))}
          </div>
        </div>

        <div className="grid sm:grid-cols-2 gap-4">
          <div>
            <label htmlFor="count" className="label">
              Number of Questions
            </label>
            <div className="flex items-center gap-4">
              <input
                id="count"
                type="range"
                min={3}
                max={10}
                value={questionCount}
                onChange={(e) => setQuestionCount(Number(e.target.value))}
                disabled={isLoading}
                className="flex-1 accent-brand-600"
              />
              <div className="w-12 text-center text-lg font-bold text-brand-700 bg-brand-50 rounded-md py-1">
                {questionCount}
              </div>
            </div>
          </div>

          <div>
            <label htmlFor="category" className="label flex items-center gap-1.5">
              <Tag className="h-4 w-4" />
              Category (optional)
            </label>
            <select
              id="category"
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              disabled={isLoading}
              className="input"
            >
              <option value="">Auto-detect</option>
              {(categories ?? []).map((c) => (
                <option key={c} value={c}>
                  {c.charAt(0).toUpperCase() + c.slice(1)}
                </option>
              ))}
            </select>
          </div>
        </div>

        <button
          type="submit"
          disabled={isLoading || !topic.trim()}
          className="btn-primary w-full py-3 text-base"
        >
          {isLoading ? (
            <>
              <Loader2 className="h-5 w-5 animate-spin" />
              Generating with AI...
            </>
          ) : (
            <>
              <Sparkles className="h-5 w-5" />
              Generate Quiz
            </>
          )}
        </button>
      </form>

      <div className="text-center text-xs text-surface-500">
        Quizzes are generated using AI with automatic fallback to a curated knowledge base.
      </div>
    </div>
  )
}
