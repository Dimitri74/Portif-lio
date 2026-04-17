'use client'

import { useState, useRef, useEffect, useCallback } from 'react'
import { MessageCircle, X, Send, Bot, User, Wifi, WifiOff } from 'lucide-react'
import { iaApi } from '@/lib/api'
import { formatTime } from '@/lib/utils'
import FlorindaLogo from '@/components/layout/FlorindaLogo'

interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: string
  loading?: boolean
}

const WELCOME_MSG: Message = {
  id: 'welcome',
  role: 'assistant',
  content:
    'Olá! Sou a Florinda, sua assistente virtual! 🍽️ Como posso ajudar você hoje? Posso consultar o status de pedidos, tirar dúvidas sobre o cardápio e muito mais!',
  timestamp: new Date().toISOString(),
}

const SUGGESTIONS = [
  'Qual é o prazo de entrega?',
  'Quais formas de pagamento são aceitas?',
  'Como rastrear meu pedido?',
  'Quais restaurantes estão abertos?',
]

let sessionId = ''
function getSessionId() {
  if (!sessionId) {
    sessionId = `sessao-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`
  }
  return sessionId
}

export default function ChatWidget() {
  const [open, setOpen] = useState(false)
  const [messages, setMessages] = useState<Message[]>([WELCOME_MSG])
  const [input, setInput] = useState('')
  const [isTyping, setIsTyping] = useState(false)
  const [iaOnline, setIaOnline] = useState<boolean | null>(null)
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  // Verifica status da IA ao abrir
  useEffect(() => {
    if (open && iaOnline === null) {
      iaApi.health()
        .then(() => setIaOnline(true))
        .catch(() => setIaOnline(false))
    }
    if (open) {
      setTimeout(() => inputRef.current?.focus(), 100)
    }
  }, [open, iaOnline])

  // Scroll to bottom
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, isTyping])

  const sendMessage = useCallback(async (text: string) => {
    const userText = text.trim()
    if (!userText || isTyping) return

    const userMsg: Message = {
      id: Date.now().toString(),
      role: 'user',
      content: userText,
      timestamp: new Date().toISOString(),
    }

    setMessages((prev) => [...prev, userMsg])
    setInput('')
    setIsTyping(true)

    try {
      const response = await iaApi.chat({
        pergunta: userText,
        sessaoId: getSessionId(),
      })

      const assistantMsg: Message = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: response.resposta,
        timestamp: response.timestamp ?? new Date().toISOString(),
      }
      setMessages((prev) => [...prev, assistantMsg])
    } catch (err: unknown) {
      const isTimeout = err instanceof Error && (err.name === 'AbortError' || err.message.includes('504') || err.message.includes('503'))
      const errMsg: Message = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: isTimeout
          ? 'O modelo de IA está carregando. Isso pode levar alguns segundos na primeira mensagem. Tente novamente!'
          : 'Não foi possível conectar ao serviço de IA. Verifique se o ms-ia-suporte está rodando na porta 8083.',
        timestamp: new Date().toISOString(),
      }
      setMessages((prev) => [...prev, errMsg])
    } finally {
      setIsTyping(false)
    }
  }, [isTyping])

  function handleKey(e: React.KeyboardEvent) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      sendMessage(input)
    }
  }

  return (
    <>
      {/* Floating button */}
      <button
        onClick={() => setOpen((v) => !v)}
        className={`
          fixed bottom-6 right-6 z-50
          w-14 h-14 rounded-full shadow-xl
          flex items-center justify-center
          transition-all duration-300
          ${open ? 'bg-gray-700 rotate-90' : 'bg-red-600 hover:bg-red-700'}
        `}
        aria-label={open ? 'Fechar chat' : 'Abrir chat com IA'}
      >
        {open ? (
          <X size={22} className="text-white" />
        ) : (
          <MessageCircle size={22} className="text-white" />
        )}

        {/* Unread dot */}
        {!open && (
          <span className="absolute -top-0.5 -right-0.5 w-3 h-3 bg-orange-400 rounded-full border-2 border-white" />
        )}
      </button>

      {/* Chat window */}
      {open && (
        <div
          className="
            fixed bottom-24 right-6 z-50
            w-80 md:w-96 h-[480px]
            bg-white rounded-2xl shadow-2xl border border-gray-100
            flex flex-col overflow-hidden
            animate-fade-in
          "
          role="dialog"
          aria-label="Chat com IA Florinda"
        >
          {/* Header */}
          <div className="flex items-center gap-3 px-4 py-3 bg-gradient-to-r from-red-600 to-orange-500 text-white">
            <FlorindaLogo size={34} />
            <div className="flex-1">
              <p className="font-bold text-sm leading-tight">Florinda IA</p>
              <div className="flex items-center gap-1">
                {iaOnline === null ? (
                  <span className="text-xs text-red-100">Verificando...</span>
                ) : iaOnline ? (
                  <>
                    <Wifi size={11} className="text-green-300" />
                    <span className="text-xs text-green-200">Online</span>
                  </>
                ) : (
                  <>
                    <WifiOff size={11} className="text-red-300" />
                    <span className="text-xs text-red-200">Offline</span>
                  </>
                )}
              </div>
            </div>
            <button
              onClick={() => setOpen(false)}
              className="p-1.5 rounded-lg text-white/70 hover:text-white hover:bg-white/20 transition-colors"
              aria-label="Fechar chat"
            >
              <X size={18} />
            </button>
          </div>

          {/* Messages */}
          <div className="flex-1 overflow-y-auto p-4 space-y-3 scrollbar-none">
            {messages.map((msg) => (
              <div
                key={msg.id}
                className={`flex gap-2 ${msg.role === 'user' ? 'flex-row-reverse' : ''}`}
              >
                {/* Avatar */}
                <div
                  className={`w-7 h-7 rounded-full flex items-center justify-center shrink-0 ${
                    msg.role === 'assistant'
                      ? 'bg-red-100'
                      : 'bg-orange-100'
                  }`}
                >
                  {msg.role === 'assistant' ? (
                    <Bot size={14} className="text-red-600" />
                  ) : (
                    <User size={14} className="text-orange-600" />
                  )}
                </div>

                {/* Bubble */}
                <div
                  className={`max-w-[75%] ${
                    msg.role === 'user' ? 'items-end' : 'items-start'
                  } flex flex-col gap-0.5`}
                >
                  <div
                    className={`px-3 py-2 rounded-2xl text-sm leading-relaxed ${
                      msg.role === 'user'
                        ? 'bg-red-600 text-white rounded-tr-sm'
                        : 'bg-gray-100 text-gray-800 rounded-tl-sm'
                    }`}
                  >
                    {msg.content}
                  </div>
                  <span className="text-xs text-gray-400">
                    {formatTime(msg.timestamp)}
                  </span>
                </div>
              </div>
            ))}

            {/* Typing indicator */}
            {isTyping && (
              <div className="flex gap-2">
                <div className="w-7 h-7 rounded-full bg-red-100 flex items-center justify-center">
                  <Bot size={14} className="text-red-600" />
                </div>
                <div className="bg-gray-100 rounded-2xl rounded-tl-sm px-4 py-3">
                  <div className="flex gap-1 items-center">
                    <span className="typing-dot" />
                    <span className="typing-dot" />
                    <span className="typing-dot" />
                  </div>
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {/* Suggestions */}
          {messages.length <= 1 && (
            <div className="px-4 pb-2 flex gap-2 overflow-x-auto scrollbar-none">
              {SUGGESTIONS.map((s) => (
                <button
                  key={s}
                  onClick={() => sendMessage(s)}
                  className="shrink-0 text-xs bg-red-50 text-red-700 hover:bg-red-100 px-3 py-1.5 rounded-full font-medium transition-colors border border-red-100 whitespace-nowrap"
                >
                  {s}
                </button>
              ))}
            </div>
          )}

          {/* Input */}
          <div className="p-3 border-t border-gray-100 flex gap-2">
            <input
              ref={inputRef}
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKey}
              placeholder="Digite sua mensagem..."
              className="flex-1 bg-gray-50 border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-transparent"
              disabled={isTyping}
              maxLength={2000}
            />
            <button
              onClick={() => sendMessage(input)}
              disabled={!input.trim() || isTyping}
              className="w-9 h-9 rounded-xl bg-red-600 hover:bg-red-700 disabled:opacity-40 disabled:cursor-not-allowed flex items-center justify-center text-white transition-colors shrink-0"
              aria-label="Enviar"
            >
              <Send size={16} />
            </button>
          </div>
        </div>
      )}
    </>
  )
}
