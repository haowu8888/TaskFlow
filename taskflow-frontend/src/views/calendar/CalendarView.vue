<template>
  <div class="calendar-view">
    <div class="page-header flex-between">
      <h2 class="page-title">{{ $t('calendar.title') }}</h2>
      <div class="calendar-toolbar">
        <el-radio-group v-model="currentView" size="small" @change="changeView">
          <el-radio-button value="dayGridMonth">{{ $t('calendar.month') }}</el-radio-button>
          <el-radio-button value="timeGridWeek">{{ $t('calendar.week') }}</el-radio-button>
          <el-radio-button value="timeGridDay">{{ $t('calendar.day') }}</el-radio-button>
        </el-radio-group>
      </div>
    </div>
    <el-card shadow="never" class="calendar-card">
      <FullCalendar ref="calendarRef" :options="calendarOptions" />
    </el-card>
    <TaskDetailDrawer v-model="showDetail" :task-id="selectedTaskId" @updated="refetchEvents" @deleted="refetchEvents" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import type { CalendarOptions, EventClickArg } from '@fullcalendar/core'
import TaskDetailDrawer from '@/components/task/TaskDetailDrawer.vue'
import { useWorkspaceStore } from '@/stores/workspace'
import { taskApi } from '@/api/task'
import type { Task } from '@/types'

const workspaceStore = useWorkspaceStore()
const { t } = useI18n()
const calendarRef = ref<InstanceType<typeof FullCalendar>>()
const currentView = ref('dayGridMonth')
const showDetail = ref(false)
const selectedTaskId = ref(0)

const priorityColors: Record<string, string> = {
  URGENT: '#F56C6C',
  HIGH: '#E6A23C',
  MEDIUM: '#409EFF',
  LOW: '#909399'
}

const calendarOptions = computed<CalendarOptions>(() => ({
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: currentView.value,
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: ''
  },
  editable: true,
  selectable: true,
  height: 'calc(100vh - 200px)',
  events: fetchEvents,
  eventClick: handleEventClick,
  eventDrop: handleEventDrop,
  eventDidMount: (info) => {
    info.el.title = info.event.title
  }
}))

async function fetchEvents(fetchInfo: any, successCallback: Function, failureCallback: Function) {
  if (!workspaceStore.currentWorkspace) {
    successCallback([])
    return
  }
  try {
    const start = fetchInfo.startStr.split('T')[0]
    const end = fetchInfo.endStr.split('T')[0]
    const res = await taskApi.getCalendarTasks(workspaceStore.currentWorkspace.id, start, end)
    const events = res.data.map((task: Task) => ({
      id: String(task.id),
      title: task.title,
      start: task.startDate || task.dueDate,
      end: task.dueDate,
      backgroundColor: priorityColors[task.priority] || '#409EFF',
      borderColor: priorityColors[task.priority] || '#409EFF',
      extendedProps: { task }
    }))
    successCallback(events)
  } catch (err) {
    failureCallback(err)
  }
}

function handleEventClick(info: EventClickArg) {
  selectedTaskId.value = Number(info.event.id)
  showDetail.value = true
}

async function handleEventDrop(info: any) {
  const taskId = Number(info.event.id)
  const newDate = info.event.startStr?.split('T')[0]
  if (taskId && newDate) {
    try {
      await taskApi.update(taskId, { dueDate: newDate })
    } catch {
      info.revert()
    }
  }
}

function changeView(view: string) {
  const calendarApi = calendarRef.value?.getApi()
  if (calendarApi) {
    calendarApi.changeView(view)
  }
}

function refetchEvents() {
  const calendarApi = calendarRef.value?.getApi()
  if (calendarApi) {
    calendarApi.refetchEvents()
  }
}

watch(() => workspaceStore.currentWorkspace, () => refetchEvents())
</script>

<style lang="scss" scoped>
.page-header { margin-bottom: 16px; }
.page-title { font-size: 24px; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.calendar-card {
  :deep(.fc) {
    font-family: inherit;
    .fc-toolbar-title { font-size: 18px; }
    .fc-button-primary {
      background-color: #409EFF;
      border-color: #409EFF;
      &:hover { background-color: #66b1ff; border-color: #66b1ff; }
    }
    .fc-event { border-radius: 4px; padding: 2px 4px; font-size: 12px; cursor: pointer; }
    .fc-daygrid-day:hover { background-color: #f5f7fa; }
  }
}
</style>
