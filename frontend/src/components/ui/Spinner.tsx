import { Loader2 } from 'lucide-react'

interface SpinnerProps {
  size?: 'sm' | 'md' | 'lg' | 'xl'
  label?: string
  className?: string
}

const sizes = {
  sm: 'h-4 w-4',
  md: 'h-6 w-6',
  lg: 'h-8 w-8',
  xl: 'h-12 w-12',
}

export default function Spinner({ size = 'md', label, className = '' }: SpinnerProps) {
  return (
    <div className={`inline-flex items-center gap-3 text-surface-600 ${className}`}>
      <Loader2 className={`${sizes[size]} animate-spin text-brand-600`} />
      {label && <span className="text-sm font-medium">{label}</span>}
    </div>
  )
}
