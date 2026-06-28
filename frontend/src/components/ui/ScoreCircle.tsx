interface ScoreCircleProps {
  percentage: number
  size?: number
  thickness?: number
  showGrade?: boolean
  grade?: string
}

export default function ScoreCircle({
  percentage,
  size = 160,
  thickness = 12,
  showGrade = true,
  grade,
}: ScoreCircleProps) {
  const radius = (size - thickness) / 2
  const circumference = 2 * Math.PI * radius
  const dashOffset = circumference * (1 - percentage / 100)

  const color =
    percentage >= 80
      ? 'text-emerald-500'
      : percentage >= 60
      ? 'text-amber-500'
      : percentage >= 40
      ? 'text-orange-500'
      : 'text-rose-500'

  return (
    <div className="relative inline-flex items-center justify-center" style={{ width: size, height: size }}>
      <svg width={size} height={size} className="-rotate-90">
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          stroke="currentColor"
          className="text-surface-200"
          strokeWidth={thickness}
          fill="none"
        />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          stroke="currentColor"
          className={`${color} transition-all duration-1000 ease-out`}
          strokeWidth={thickness}
          fill="none"
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={dashOffset}
        />
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center">
        <div className="text-3xl font-bold text-surface-900">{percentage.toFixed(0)}%</div>
        {showGrade && grade && (
          <div className="text-xs font-semibold text-surface-500 uppercase tracking-wider">
            Grade {grade}
          </div>
        )}
      </div>
    </div>
  )
}
