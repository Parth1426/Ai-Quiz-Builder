import type { Difficulty } from '@/types'

interface DifficultyBadgeProps {
  difficulty: Difficulty
}

export default function DifficultyBadge({ difficulty }: DifficultyBadgeProps) {
  const className =
    difficulty === 'EASY' ? 'badge-easy' : difficulty === 'MEDIUM' ? 'badge-medium' : 'badge-hard'
  return <span className={className}>{difficulty}</span>
}
