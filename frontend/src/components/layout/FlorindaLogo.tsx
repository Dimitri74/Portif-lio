export default function FlorindaLogo({ size = 40 }: { size?: number }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 80 80"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-label="Florinda Eats"
      suppressHydrationWarning
    >
      {/* Fundo circular */}
      <circle cx="40" cy="40" r="40" fill="#DC2626" />

      {/* Rosto */}
      <circle cx="40" cy="30" r="16" fill="#FBBF24" />

      {/* Cabelo */}
      <path
        d="M24 26 Q26 12 40 10 Q54 12 56 26 Q52 18 40 18 Q28 18 24 26Z"
        fill="#92400E"
      />

      {/* Coque do cabelo */}
      <ellipse cx="40" cy="11" rx="8" ry="6" fill="#92400E" />
      <ellipse cx="32" cy="14" rx="4" ry="3" fill="#92400E" />
      <ellipse cx="48" cy="14" rx="4" ry="3" fill="#92400E" />

      {/* Olhos */}
      <ellipse cx="34" cy="28" rx="2.5" ry="3" fill="#1C1917" />
      <ellipse cx="46" cy="28" rx="2.5" ry="3" fill="#1C1917" />
      <circle cx="35" cy="27" r="1" fill="white" />
      <circle cx="47" cy="27" r="1" fill="white" />

      {/* Boca sorrindo */}
      <path
        d="M34 35 Q40 40 46 35"
        stroke="#92400E"
        strokeWidth="1.5"
        strokeLinecap="round"
        fill="none"
      />

      {/* Bochechas */}
      <circle cx="30" cy="33" r="3" fill="#FCA5A5" opacity="0.6" />
      <circle cx="50" cy="33" r="3" fill="#FCA5A5" opacity="0.6" />

      {/* Corpo / avental */}
      <path
        d="M26 46 Q22 56 20 68 L60 68 Q58 56 54 46 Q50 44 40 44 Q30 44 26 46Z"
        fill="#F97316"
      />

      {/* Detalhe do avental */}
      <path
        d="M30 44 L32 56 L40 58 L48 56 L50 44"
        fill="white"
        opacity="0.3"
      />

      {/* Colher / utensílio */}
      <rect x="55" y="38" width="3" height="20" rx="1.5" fill="#E5E7EB" />
      <ellipse cx="56.5" cy="37" rx="3" ry="4" fill="#E5E7EB" />
    </svg>
  )
}
