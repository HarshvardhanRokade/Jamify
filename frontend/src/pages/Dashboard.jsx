import { useNavigate } from 'react-router-dom'
import { useEffect } from 'react'
import { motion } from 'framer-motion'
import useUserStore from '../stores/useUserStore'
import { authApi } from '../api/auth'

export default function Dashboard() {
  const { user, isAuthenticated, clearUser } = useUserStore()
  const navigate = useNavigate()

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/')
    }
  }, [isAuthenticated])

  const handleLogout = async () => {
    await authApi.logout()
    clearUser()
    navigate('/')
  }

  if (!user) return null

  return (
    <div className="min-h-screen bg-spotify-dark">
      
      {/* Navbar */}
      <nav className="flex items-center justify-between px-8 py-4 
                      border-b border-spotify-hover">
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 bg-spotify-green rounded-full" />
          <span className="text-white font-bold text-lg">Jamify</span>
        </div>

        <div className="flex items-center gap-4">
          {/* User avatar and name */}
          <div className="flex items-center gap-3">
            {user.avatarUrl ? (
              <img
                src={user.avatarUrl}
                alt={user.displayName}
                className="w-8 h-8 rounded-full object-cover"
              />
            ) : (
              <div className="w-8 h-8 rounded-full bg-spotify-green flex 
                              items-center justify-center text-black font-bold text-sm">
                {user.displayName?.charAt(0).toUpperCase()}
              </div>
            )}
            <span className="text-white text-sm font-medium">
              {user.displayName}
            </span>
            {user.isPremium && (
              <span className="text-xs bg-spotify-green text-black 
                               font-bold px-2 py-0.5 rounded-full">
                Premium
              </span>
            )}
          </div>

          <button
            onClick={handleLogout}
            className="text-spotify-text text-sm hover:text-white transition-colors"
          >
            Log out
          </button>
        </div>
      </nav>

      {/* Main content */}
      <main className="px-8 py-8 max-w-7xl mx-auto">
        
        {/* Welcome section */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-10"
        >
          <h1 className="text-3xl font-bold text-white mb-1">
            Good {getTimeOfDay()}, {user.displayName.split(' ')[0]} 👋
          </h1>
          <p className="text-spotify-text">
            What do you want to listen to today?
          </p>
        </motion.div>

        {/* Quick actions */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-10"
        >
          {[
            {
              icon: '🎵',
              title: 'Start a Jam',
              desc: 'Host a public listening room',
              color: 'from-green-900 to-spotify-card',
              action: () => {}
            },
            {
              icon: '🔍',
              title: 'Discover Rooms',
              desc: 'Join an active jam session',
              color: 'from-purple-900 to-spotify-card',
              action: () => {}
            },
            {
              icon: '📊',
              title: 'Your Stats',
              desc: 'Spotify Wrapped on demand',
              color: 'from-blue-900 to-spotify-card',
              action: () => {}
            },
          ].map((item) => (
            <motion.button
              key={item.title}
              whileHover={{ scale: 1.02, y: -2 }}
              whileTap={{ scale: 0.98 }}
              onClick={item.action}
              className={`bg-gradient-to-br ${item.color} p-6 rounded-2xl 
                         text-left border border-spotify-hover hover:border-spotify-green
                         transition-all duration-200`}
            >
              <span className="text-3xl mb-3 block">{item.icon}</span>
              <h3 className="text-white font-bold text-lg mb-1">{item.title}</h3>
              <p className="text-spotify-text text-sm">{item.desc}</p>
            </motion.button>
          ))}
        </motion.div>

        {/* Active rooms placeholder */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
        >
          <h2 className="text-white font-bold text-xl mb-4">
            Active Rooms
          </h2>
          
          {/* Empty state */}
          <div className="bg-spotify-card rounded-2xl p-16 flex flex-col 
                          items-center justify-center border border-spotify-hover">
            <span className="text-5xl mb-4">🎧</span>
            <h3 className="text-white font-bold text-lg mb-2">
              No active rooms yet
            </h3>
            <p className="text-spotify-text text-sm text-center mb-6">
              Be the first to start a jam and invite others to listen along
            </p>
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              className="bg-spotify-green text-black font-bold px-6 py-3 
                         rounded-full hover:bg-green-400 transition-colors"
            >
              Start a Jam
            </motion.button>
          </div>
        </motion.div>
      </main>
    </div>
  )
}

function getTimeOfDay() {
  const hour = new Date().getHours()
  if (hour < 12) return 'morning'
  if (hour < 17) return 'afternoon'
  return 'evening'
}