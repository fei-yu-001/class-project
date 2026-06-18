<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

interface Petal {
  x: number
  y: number
  size: number
  speedY: number
  speedX: number
  rotation: number
  rotationSpeed: number
  opacity: number
  color: string
  type: 'petal' | 'dandelion'
}

const canvasRef = ref<HTMLCanvasElement>()
let animId = 0

onMounted(() => {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const resize = () => {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
  }
  resize()
  window.addEventListener('resize', resize)

  const petals: Petal[] = []
  const count = 18

  const colors = [
    'rgba(255, 182, 193, ',
    'rgba(255, 218, 185, ',
    'rgba(221, 160, 221, ',
    'rgba(176, 224, 230, ',
    'rgba(255, 255, 224, ',
  ]

  for (let i = 0; i < count; i++) {
    petals.push({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      size: 3 + Math.random() * 6,
      speedY: 0.2 + Math.random() * 0.5,
      speedX: (Math.random() - 0.5) * 0.3,
      rotation: Math.random() * Math.PI * 2,
      rotationSpeed: (Math.random() - 0.5) * 0.008,
      opacity: 0.15 + Math.random() * 0.25,
      color: colors[Math.floor(Math.random() * colors.length)],
      type: Math.random() > 0.4 ? 'petal' : 'dandelion',
    })
  }

  const drawPetal = (p: Petal) => {
    ctx.save()
    ctx.translate(p.x, p.y)
    ctx.rotate(p.rotation)
    ctx.globalAlpha = p.opacity

    const gradient = ctx.createRadialGradient(0, 0, 0, 0, 0, p.size * 2)
    gradient.addColorStop(0, p.color + '0.6)')
    gradient.addColorStop(1, p.color + '0)')

    ctx.fillStyle = gradient
    ctx.beginPath()
    ctx.ellipse(0, 0, p.size, p.size * 0.6, 0, 0, Math.PI * 2)
    ctx.fill()

    ctx.strokeStyle = p.color + '0.3)'
    ctx.lineWidth = 0.5
    ctx.beginPath()
    ctx.moveTo(-p.size * 0.3, 0)
    ctx.quadraticCurveTo(0, -p.size * 0.5, p.size * 0.3, 0)
    ctx.stroke()

    ctx.restore()
  }

  const drawDandelion = (p: Petal) => {
    ctx.save()
    ctx.translate(p.x, p.y)
    ctx.rotate(p.rotation)
    ctx.globalAlpha = p.opacity * 0.7

    const seedCount = 6
    for (let i = 0; i < seedCount; i++) {
      const angle = (Math.PI * 2 / seedCount) * i
      const len = p.size * 1.5

      ctx.strokeStyle = 'rgba(255, 255, 240, 0.4)'
      ctx.lineWidth = 0.3
      ctx.beginPath()
      ctx.moveTo(0, 0)
      ctx.lineTo(Math.cos(angle) * len, Math.sin(angle) * len)
      ctx.stroke()

      ctx.fillStyle = 'rgba(255, 255, 255, 0.5)'
      ctx.beginPath()
      ctx.arc(Math.cos(angle) * len, Math.sin(angle) * len, 1, 0, Math.PI * 2)
      ctx.fill()
    }

    ctx.restore()
  }

  const animate = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    petals.forEach((p) => {
      p.y += p.speedY
      p.x += p.speedX + Math.sin(p.y * 0.003) * 0.15
      p.rotation += p.rotationSpeed

      if (p.y > canvas.height + 20) {
        p.y = -20
        p.x = Math.random() * canvas.width
      }
      if (p.x < -20) p.x = canvas.width + 20
      if (p.x > canvas.width + 20) p.x = -20

      if (p.type === 'petal') {
        drawPetal(p)
      } else {
        drawDandelion(p)
      }
    })

    animId = requestAnimationFrame(animate)
  }

  animate()

  onUnmounted(() => {
    cancelAnimationFrame(animId)
    window.removeEventListener('resize', resize)
  })
})
</script>

<template>
  <canvas
    ref="canvasRef"
    class="fixed inset-0 pointer-events-none"
    style="z-index: 1"
  />
</template>
