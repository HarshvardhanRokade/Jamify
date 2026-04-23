import { motion } from 'framer-motion'
import { useNavigate } from 'react-router-dom'
import useUserStore from '../stores/useUserStore'
import { useEffect } from 'react'

export default function Landing() {
  const { isAuthenticated } = useUserStore()
  const navigate = useNavigate()

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/dashboard')
    }
  }, [isAuthenticated])

  const handleLogin = () => {
    window.location.href = '/api/auth/login'
  }

  return (
    <div className="min-h-screen bg-spotify-dark flex flex-col">
      
      {/* Navbar */}
      <nav className="flex items-center justify-between px-8 py-6">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 bg-spotify-green rounded-full" />
          <span className="text-white font-bold text-xl">Jamify</span>
        </div>
        <button
          onClick={handleLogin}
          className="text-white text-sm font-medium hover:text-spotify-green transition-colors"
        >
          Log in
        </button>
      </nav>

      {/* Hero */}
      <div className="flex-1 flex flex-col items-center justify-center px-4 text-center">
        
        {/* Animated background blobs */}
        <div className="absolute inset-0 overflow-hidden pointer-events-none">
          <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-spotify-green opacity-5 rounded-full blur-3xl animate-pulse-slow" />
          <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-purple-500 opacity-5 rounded-full blur-3xl animate-pulse-slow" />
        </div>

        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="relative z-10 max-w-4xl"
        >
          {/* Badge */}
          <motion.div
            initial={{ opacity: 0, scale: 0.8 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ delay: 0.2 }}
            className="inline-flex items-center gap-2 bg-spotify-card border border-spotify-hover 
                       rounded-full px-4 py-2 mb-8"
          >
            <div className="w-2 h-2 bg-spotify-green rounded-full animate-pulse" />
            <span className="text-spotify-text text-sm">
              Public listening rooms, powered by Spotify
            </span>
          </motion.div>

          {/* Headline */}
          <h1 className="text-6xl md:text-8xl font-black text-white mb-6 leading-none tracking-tight">
            Music is better
            <br />
            <span className="text-spotify-green">together.</span>
          </h1>

          {/* Subheadline */}
          <p className="text-spotify-text text-xl md:text-2xl mb-12 max-w-2xl mx-auto leading-relaxed">
            Turn your Spotify session into a public jam. 
            Discover rooms, vibe with strangers, find your people.
          </p>

          {/* CTA */}
          <motion.button
            onClick={handleLogin}
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            className="inline-flex items-center gap-3 bg-spotify-green text-black 
                       font-bold text-lg px-10 py-4 rounded-full hover:bg-green-400 
                       transition-colors shadow-lg shadow-green-900/30"
          >
            <svg className="w-6 h-6" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 0C5.4 0 0 5.4 0 12s5.4 12 12 12 12-5.4 12-12S18.66 0 12 0zm5.521 17.34c-.24.359-.66.48-1.021.24-2.82-1.74-6.36-2.101-10.561-1.141-.418.122-.779-.179-.899-.539-.12-.421.18-.78.54-.9 4.56-1.021 8.52-.6 11.64 1.32.42.18.479.659.301 1.02zm1.44-3.3c-.301.42-.841.6-1.262.3-3.239-1.98-8.159-2.58-11.939-1.38-.479.12-1.02-.12-1.14-.6-.12-.48.12-1.021.6-1.141C9.6 9.9 15 10.561 18.72 12.84c.361.181.54.78.241 1.2zm.12-3.36C15.24 8.4 8.82 8.16 5.16 9.301c-.6.179-1.2-.181-1.38-.721-.18-.601.18-1.2.72-1.381 4.26-1.26 11.28-1.02 15.721 1.621.539.3.719 1.02.419 1.56-.299.421-1.02.599-1.559.3z"/>
            </svg>
            Start Jamming with Spotify
          </motion.button>

          {/* Features row */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.5 }}
            className="flex flex-wrap items-center justify-center gap-8 mt-16"
          >
            {[
              { icon: '🎵', label: 'Public jam rooms' },
              { icon: '🔍', label: 'Discover music' },
              { icon: '🗳️', label: 'Vote on tracks' },
              { icon: '💬', label: 'Live chat' },
            ].map((feature) => (
              <div
                key={feature.label}
                className="flex items-center gap-2 text-spotify-text"
              >
                <span className="text-xl">{feature.icon}</span>
                <span className="text-sm font-medium">{feature.label}</span>
              </div>
            ))}
          </motion.div>
        </motion.div>
      </div>

      {/* Footer */}
      <footer className="text-center py-6 text-spotify-text text-sm">
        Built with ♥ using Spotify API
      </footer>
    </div>
  )
}