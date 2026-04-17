<template>
  <div class="bg-gray-900 rounded-xl p-5 border" :class="borderClass">
    <p class="text-sm text-gray-400 mb-1">{{ title }}</p>
    <p class="text-3xl font-bold" :class="textClass">{{ value }}</p>
    <p v-if="sub" class="text-xs text-gray-500 mt-1">{{ sub }}</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: String,
  value: [String, Number],
  sub: String,
  color: { type: String, default: 'blue' },
})

const colorMap = {
  blue:   { border: 'border-blue-800',   text: 'text-blue-400' },
  green:  { border: 'border-green-800',  text: 'text-green-400' },
  red:    { border: 'border-red-800',    text: 'text-red-400' },
  yellow: { border: 'border-yellow-800', text: 'text-yellow-400' },
  orange: { border: 'border-orange-800', text: 'text-orange-400' },
}

const borderClass = computed(() => colorMap[props.color]?.border ?? 'border-gray-700')
const textClass   = computed(() => colorMap[props.color]?.text   ?? 'text-white')
</script>
