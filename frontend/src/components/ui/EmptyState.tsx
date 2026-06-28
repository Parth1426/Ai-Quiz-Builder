interface EmptyStateProps {
  icon?: React.ReactNode
  title: string
  description?: string
  action?: React.ReactNode
}

export default function EmptyState({ icon, title, description, action }: EmptyStateProps) {
  return (
    <div className="card p-12 text-center">
      {icon && <div className="mx-auto mb-4 text-surface-400 flex justify-center">{icon}</div>}
      <h3 className="text-lg font-semibold text-surface-900 mb-1">{title}</h3>
      {description && <p className="text-sm text-surface-500 max-w-md mx-auto mb-4">{description}</p>}
      {action}
    </div>
  )
}
