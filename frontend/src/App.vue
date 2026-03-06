<template>
  <div class="min-h-screen bg-tech-bg text-tech-text p-6 md:p-12 font-sans selection:bg-tech-accent selection:text-white">
    <div class="max-w-6xl mx-auto space-y-12">
      <!-- Header -->
      <header class="space-y-4 animate-fade-in-down">
        <div class="flex items-center gap-3">
          <div class="w-2 h-8 bg-tech-accent rounded-full shadow-[0_0_15px_rgba(59,130,246,0.5)]"></div>
          <h1 class="text-3xl md:text-4xl font-bold tracking-tight text-white">LLM 算力评估器</h1>
        </div>
        <p class="text-tech-muted max-w-2xl text-lg">
          基于真实模型参数的显存与算力精准评估系统
        </p>
      </header>

      <main class="grid lg:grid-cols-12 gap-8">
        <!-- Input Form -->
        <section class="lg:col-span-4 space-y-6">
          <div class="bg-tech-card border border-tech-border rounded-xl p-6 shadow-xl backdrop-blur-sm relative overflow-hidden group">
            <div class="absolute inset-0 bg-gradient-to-br from-tech-accent/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
            
            <form @submit.prevent="onSubmit" class="space-y-6 relative z-10">
              <div class="space-y-2">
                <label class="text-sm font-medium text-tech-muted uppercase tracking-wider">目标模型 (Target Model)</label>
                <input 
                  v-model="form.model" 
                  class="w-full bg-tech-bg border border-tech-border rounded-lg px-4 py-3 text-sm focus:ring-2 focus:ring-tech-accent focus:border-transparent outline-none transition-all placeholder:text-tech-border/50"
                  placeholder="例如: cyankiwi/Qwen3.5-27B-AWQ-BF16-INT8"
                  required 
                />
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div class="space-y-2">
                  <label class="text-xs font-medium text-tech-muted uppercase tracking-wider">输入长度 (Input Tokens)</label>
                  <input 
                    v-model.number="form.avgInputTokens" 
                    type="number" 
                    min="1"
                    class="w-full bg-tech-bg border border-tech-border rounded-lg px-4 py-3 text-sm focus:ring-2 focus:ring-tech-accent focus:border-transparent outline-none transition-all font-mono"
                    required 
                  />
                </div>
                <div class="space-y-2">
                  <label class="text-xs font-medium text-tech-muted uppercase tracking-wider">输出长度 (Output Tokens)</label>
                  <input 
                    v-model.number="form.avgOutputTokens" 
                    type="number" 
                    min="1"
                    class="w-full bg-tech-bg border border-tech-border rounded-lg px-4 py-3 text-sm focus:ring-2 focus:ring-tech-accent focus:border-transparent outline-none transition-all font-mono"
                    required 
                  />
                </div>
              </div>

              <div class="space-y-2">
                <label class="text-sm font-medium text-tech-muted uppercase tracking-wider">场景描述 (Context)</label>
                <textarea 
                  v-model="form.scenarioDescription" 
                  class="w-full bg-tech-bg border border-tech-border rounded-lg px-4 py-3 text-sm focus:ring-2 focus:ring-tech-accent focus:border-transparent outline-none transition-all min-h-[120px] resize-none"
                  placeholder="描述您的业务场景（如：高并发实时对话、离线批处理分析...）"
                  required 
                />
              </div>

              <button 
                type="submit" 
                :disabled="loading"
                class="w-full bg-tech-accent hover:bg-blue-600 text-white font-medium py-3.5 rounded-lg transition-all duration-300 transform active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 shadow-lg shadow-tech-accent/20 group"
              >
                <span v-if="loading" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                <span>{{ loading ? '正在分析...' : '开始评估' }}</span>
                <svg v-if="!loading" xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 group-hover:translate-x-1 transition-transform" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6" />
                </svg>
              </button>
            </form>
          </div>

          <!-- Error Message -->
          <div v-if="error" class="bg-red-500/10 border border-red-500/20 text-red-400 px-4 py-3 rounded-lg text-sm flex items-start gap-3 animate-fade-in">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <p>{{ error }}</p>
          </div>
        </section>

        <!-- Visualization -->
        <section class="lg:col-span-8 space-y-6">
          <div v-if="result" class="space-y-6 animate-fade-in-up">
            <!-- Stats Grid -->
            <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
              <div class="bg-tech-card border border-tech-border/50 rounded-xl p-4 group hover:border-tech-accent/50 transition-colors">
                <div class="text-xs text-tech-muted uppercase tracking-wider mb-1">推理模型 (Runtime Model)</div>
                <div class="font-mono text-sm text-white truncate" :title="result.fixedRuntimeModel">{{ result.fixedRuntimeModel }}</div>
              </div>
              <div class="bg-tech-card border border-tech-border/50 rounded-xl p-4 group hover:border-tech-accent/50 transition-colors">
                <div class="text-xs text-tech-muted uppercase tracking-wider mb-1">参数量 (Parameters)</div>
                <div class="font-mono text-xl text-white">
                  {{ result.modelProfile.modelParamsB }}B
                  <span class="text-xs text-tech-muted ml-1">Total</span>
                </div>
              </div>
              <div class="bg-tech-card border border-tech-border/50 rounded-xl p-4 group hover:border-tech-accent/50 transition-colors">
                <div class="text-xs text-tech-muted uppercase tracking-wider mb-1">激活参数 (Activated)</div>
                <div class="font-mono text-xl text-white">
                  {{ result.modelProfile.activatedParamsB }}B
                  <span class="text-xs text-tech-muted ml-1">Active</span>
                </div>
              </div>
              <div class="bg-tech-card border border-tech-border/50 rounded-xl p-4 group hover:border-tech-accent/50 transition-colors">
                <div class="text-xs text-tech-muted uppercase tracking-wider mb-1">量化 (Quantization)</div>
                <div class="font-mono text-lg text-white">
                  {{ result.modelProfile.quantizationPrecision }}
                  <span class="text-xs text-tech-accent bg-tech-accent/10 px-1.5 py-0.5 rounded ml-2">x{{ result.modelProfile.quantizationFactor }}</span>
                </div>
              </div>
              <div class="bg-tech-card border border-tech-border/50 rounded-xl p-4 group hover:border-tech-accent/50 transition-colors">
                <div class="text-xs text-tech-muted uppercase tracking-wider mb-1">目标速度 (Target Speed)</div>
                <div class="font-mono text-xl text-tech-accent">{{ result.targetTokensPerSecond }} <span class="text-sm text-tech-muted">T/s</span></div>
              </div>
              <div class="bg-tech-card border border-tech-border/50 rounded-xl p-4 group hover:border-tech-accent/50 transition-colors">
                <div class="text-xs text-tech-muted uppercase tracking-wider mb-1">上下文系数 (Context Coeff)</div>
                <div class="font-mono text-xl text-white">{{ result.contextCoefficient }}</div>
              </div>
            </div>

            <!-- Chart -->
            <div class="bg-tech-card border border-tech-border rounded-xl p-1 shadow-xl h-[500px] relative">
              <v-chart class="w-full h-full" :option="chartOption" autoresize />
            </div>
          </div>

          <!-- Empty State -->
          <div v-else class="h-full min-h-[400px] flex flex-col items-center justify-center text-tech-muted border-2 border-dashed border-tech-border/30 rounded-xl bg-tech-card/30">
            <div class="w-16 h-16 bg-tech-border/20 rounded-full flex items-center justify-center mb-4">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 opacity-50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
            </div>
            <p class="text-sm">输入参数以可视化容量指标 (Enter parameters to visualize capacity metrics)</p>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import axios from 'axios'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { LineChart } from 'echarts/charts'

use([CanvasRenderer, GridComponent, TooltipComponent, LegendComponent, LineChart])

type CurvePoint = {
  concurrency: number
  vramGb: number
  computeTflops: number
}

type FormData = {
  model: string
  avgInputTokens: number
  avgOutputTokens: number
  scenarioDescription: string
}

type Result = {
  fixedRuntimeModel: string
  modelProfile: {
    modelParamsB: number
    activatedParamsB: number
    quantizationPrecision: string
    quantizationFactor: number
  }
  targetTokensPerSecond: number
  contextCoefficient: number
  points: CurvePoint[]
}

const form = ref<FormData>({
  model: 'cyankiwi/Qwen3.5-27B-AWQ-BF16-INT8',
  avgInputTokens: 1024,
  avgOutputTokens: 256,
  scenarioDescription: '实时客服对话，要求低延迟并支持高并发'
})

const loading = ref(false)
const error = ref('')
const result = ref<Result | null>(null)

const chartOption = computed(() => {
  if (!result.value) {
    return {}
  }
  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#1e293b',
      borderColor: '#334155',
      textStyle: { color: '#e2e8f0' },
      axisPointer: { type: 'cross', label: { backgroundColor: '#3b82f6' } }
    },
    legend: {
      data: ['显存需求 (VRAM GB)', '算力需求 (TFLOPS)'],
      textStyle: { color: '#94a3b8' },
      bottom: 10
    },
    grid: {
      top: 40,
      right: 50,
      bottom: 60,
      left: 60,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      name: '并发数 (Concurrency)',
      nameLocation: 'middle',
      nameGap: 30,
      nameTextStyle: { color: '#94a3b8' },
      data: result.value.points.map((p) => p.concurrency),
      axisLine: { lineStyle: { color: '#334155' } },
      axisLabel: { color: '#94a3b8' }
    },
    yAxis: [
      {
        type: 'value',
        name: 'VRAM (GB)',
        nameTextStyle: { color: '#94a3b8' },
        splitLine: { lineStyle: { color: '#334155', type: 'dashed', opacity: 0.3 } },
        axisLabel: { color: '#94a3b8' }
      },
      {
        type: 'value',
        name: 'TFLOPS',
        nameTextStyle: { color: '#94a3b8' },
        splitLine: { show: false },
        axisLabel: { color: '#94a3b8' }
      }
    ],
    series: [
      {
        name: '显存需求 (VRAM GB)',
        type: 'line',
        smooth: 0.4,
        showSymbol: false,
        lineStyle: { width: 3, color: '#3b82f6' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(59,130,246,0.3)' },
              { offset: 1, color: 'rgba(59,130,246,0.01)' }
            ]
          }
        },
        data: result.value.points.map((p) => p.vramGb)
      },
      {
        name: '算力需求 (TFLOPS)',
        type: 'line',
        smooth: 0.4,
        showSymbol: false,
        yAxisIndex: 1,
        lineStyle: { width: 3, color: '#10b981' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(16,185,129,0.3)' },
              { offset: 1, color: 'rgba(16,185,129,0.01)' }
            ]
          }
        },
        data: result.value.points.map((p) => p.computeTflops)
      }
    ]
  }
})

async function onSubmit() {
  if (typeof form.value.avgInputTokens !== 'number' || form.value.avgInputTokens <= 0) {
    error.value = 'Input tokens must be a positive integer'
    return
  }
  if (typeof form.value.avgOutputTokens !== 'number' || form.value.avgOutputTokens <= 0) {
    error.value = 'Output tokens must be a positive integer'
    return
  }
  
  loading.value = true
  error.value = ''
  try {
    const base = import.meta.env.VITE_API_BASE ?? ''
    const endpoint = base ? `${base}/api/v1/capacity/estimate` : '/api/v1/capacity/estimate'
    const { data } = await axios.post<Result>(endpoint, form.value)
    result.value = data
  } catch (e: any) {
    result.value = null
    const detail = e?.response?.data?.message || e?.message
    error.value = detail ? `Estimation Failed: ${detail}` : 'Failed to connect to backend service'
  } finally {
    loading.value = false
  }
}
</script>

<style>
@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.animate-fade-in-down { animation: fadeInDown 0.6s ease-out forwards; }
.animate-fade-in-up { animation: fadeInUp 0.6s ease-out forwards; }
.animate-fade-in { animation: fadeIn 0.4s ease-out forwards; }
</style>
