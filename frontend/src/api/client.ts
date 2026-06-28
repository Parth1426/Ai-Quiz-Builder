import axios, { AxiosError } from 'axios'
import type {
  ApiResponse,
  AttemptResult,
  GenerateQuizRequest,
  LeaderboardEntry,
  PageResponse,
  Quiz,
  SubmitAttemptRequest,
  SystemStats,
} from '@/types'

const API_BASE = '/api/v1'

const client = axios.create({
  baseURL: API_BASE,
  timeout: 60_000,
  headers: { 'Content-Type': 'application/json' },
})

client.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse<unknown>>) => {
    const message = error.response?.data?.error ?? error.message ?? 'Unknown error'
    return Promise.reject(new Error(message))
  },
)

function unwrap<T>(response: { data: ApiResponse<T> }): T {
  if (!response.data.success || response.data.data === undefined) {
    throw new Error(response.data.error ?? 'Request failed')
  }
  return response.data.data
}

export const QuizApi = {
  generate: async (request: GenerateQuizRequest): Promise<Quiz> => {
    const response = await client.post<ApiResponse<Quiz>>('/quizzes/generate', request)
    return unwrap(response)
  },

  getQuiz: async (quizId: string): Promise<Quiz> => {
    const response = await client.get<ApiResponse<Quiz>>(`/quizzes/${quizId}`)
    return unwrap(response)
  },

  list: async (page: number, size: number): Promise<PageResponse<Quiz>> => {
    const response = await client.get<ApiResponse<PageResponse<Quiz>>>(
      `/quizzes?page=${page}&size=${size}`,
    )
    return unwrap(response)
  },

  recent: async (limit = 5): Promise<Quiz[]> => {
    const response = await client.get<ApiResponse<Quiz[]>>(`/quizzes/recent?limit=${limit}`)
    return unwrap(response)
  },

  topics: async (): Promise<string[]> => {
    const response = await client.get<ApiResponse<string[]>>('/quizzes/topics')
    return unwrap(response)
  },

  delete: async (quizId: string): Promise<void> => {
    await client.delete(`/quizzes/${quizId}`)
  },
}

export const AttemptApi = {
  submit: async (request: SubmitAttemptRequest): Promise<AttemptResult> => {
    const response = await client.post<ApiResponse<AttemptResult>>('/attempts/submit', request)
    return unwrap(response)
  },

  get: async (attemptId: string): Promise<AttemptResult> => {
    const response = await client.get<ApiResponse<AttemptResult>>(`/attempts/${attemptId}`)
    return unwrap(response)
  },

  leaderboard: async (limit = 10): Promise<LeaderboardEntry[]> => {
    const response = await client.get<ApiResponse<LeaderboardEntry[]>>(
      `/attempts/leaderboard?limit=${limit}`,
    )
    return unwrap(response)
  },
}

export const StatsApi = {
  getStats: async (): Promise<SystemStats> => {
    const response = await client.get<ApiResponse<SystemStats>>('/stats')
    return unwrap(response)
  },

  curatedTopics: async (): Promise<string[]> => {
    const response = await client.get<ApiResponse<string[]>>('/stats/topics')
    return unwrap(response)
  },

  categories: async (): Promise<string[]> => {
    const response = await client.get<ApiResponse<string[]>>('/stats/categories')
    return unwrap(response)
  },
}
