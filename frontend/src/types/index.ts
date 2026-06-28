export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD'
export type QuizStatus = 'GENERATING' | 'READY' | 'FAILED'

export interface Option {
  id: string
  optionLabel: string
  optionText: string
}

export interface Question {
  id: string
  questionText: string
  questionIndex: number
  difficulty?: Difficulty
  explanation?: string
  options: Option[]
}

export interface Quiz {
  id: string
  topic: string
  category?: string
  difficulty: Difficulty
  status: QuizStatus
  aiProvider?: string
  createdAt: string
  questions: Question[]
  questionCount: number
  totalAttempts?: number
  averageScore?: number
}

export interface GenerateQuizRequest {
  topic: string
  questionCount: number
  difficulty: Difficulty
  category?: string
}

export interface AnswerEntry {
  questionId: string
  selectedOptionId?: string
  selectedLabel?: string
}

export interface SubmitAttemptRequest {
  quizId: string
  userName?: string
  sessionId?: string
  timeTakenSeconds?: number
  answers: AnswerEntry[]
}

export interface OptionResult {
  id: string
  optionLabel: string
  optionText: string
  isCorrect: boolean
  wasSelected: boolean
}

export interface QuestionResult {
  questionId: string
  questionText: string
  selectedLabel?: string
  selectedOptionText?: string
  correctLabel?: string
  correctOptionText?: string
  isCorrect: boolean
  explanation?: string
  options: OptionResult[]
}

export interface AttemptResult {
  attemptId: string
  quizId: string
  quizTopic: string
  userName?: string
  score: number
  totalQuestions: number
  percentage: number
  grade: string
  timeTakenSeconds?: number
  completedAt: string
  questionResults: QuestionResult[]
}

export interface LeaderboardEntry {
  rank: number
  attemptId: string
  userName?: string
  quizTopic: string
  score: number
  totalQuestions: number
  percentage: number
  grade: string
  timeTakenSeconds?: number
  completedAt: string
}

export interface ApiResponse<T> {
  success: boolean
  message?: string
  data?: T
  error?: string
  timestamp: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
}

export interface SystemStats {
  totalQuizzes: number
  totalAttempts: number
  availableTopics: number
  knowledgeBaseSize: number
  activeProvider: string
  availableCategories: string[]
}
