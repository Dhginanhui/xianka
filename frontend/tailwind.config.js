/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'tech-bg': '#0f172a',
        'tech-card': '#1e293b',
        'tech-border': '#334155',
        'tech-accent': '#3b82f6',
        'tech-text': '#e2e8f0',
        'tech-muted': '#94a3b8'
      },
      fontFamily: {
        mono: ['JetBrains Mono', 'Menlo', 'Monaco', 'Consolas', 'monospace'],
        sans: ['Inter', 'system-ui', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
