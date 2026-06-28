import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/layout/Layout'
import HomePage from './pages/HomePage'
import GeneratePage from './pages/GeneratePage'
import QuizPage from './pages/QuizPage'
import ResultPage from './pages/ResultPage'
import LeaderboardPage from './pages/LeaderboardPage'
import HistoryPage from './pages/HistoryPage'
import NotFoundPage from './pages/NotFoundPage'

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/generate" element={<GeneratePage />} />
        <Route path="/quiz/:quizId" element={<QuizPage />} />
        <Route path="/result/:attemptId" element={<ResultPage />} />
        <Route path="/history" element={<HistoryPage />} />
        <Route path="/leaderboard" element={<LeaderboardPage />} />
        <Route path="/404" element={<NotFoundPage />} />
        <Route path="*" element={<Navigate to="/404" replace />} />
      </Route>
    </Routes>
  )
}

export default App
