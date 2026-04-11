/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  darkMode: ['class', '[data-theme="dark"]'],
  theme: {
    extend: {
      colors: {
        background: 'var(--bg)',
        foreground: 'var(--text)',
        card: 'var(--card)',
        muted: 'var(--muted)',
        border: 'var(--border)'
      }
    },
  },
  plugins: [],
}
