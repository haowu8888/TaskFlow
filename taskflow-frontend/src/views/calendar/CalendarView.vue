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
    <TaskCreateModal v-model="showCreate" :initial-due-date="createDate" @created="handleTaskCreated" />
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
import TaskCreateModal from '@/components/task/TaskCreateModal.vue'
import { useWorkspaceStore } from '@/stores/workspace'
import { useTaskStore } from '@/stores/task'
import { taskApi } from '@/api/task'
import type { Task } from '@/types'

const workspaceStore = useWorkspaceStore()
const taskStore = useTaskStore()
const { t } = useI18n()
const calendarRef = ref<InstanceType<typeof FullCalendar>>()
const currentView = ref('dayGridMonth')
const showDetail = ref(false)
const showCreate = ref(false)
const selectedTaskId = ref(0)
const createDate = ref('')

const priorityColors: Record<string, string> = {
  URGENT: '#f53f3f',
  HIGH: '#ff7d00',
  MEDIUM: '#165DFF',
  LOW: '#86909c'
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
  dateClick: handleDateClick,
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
      backgroundColor: priorityColors[task.priority] || '#165DFF',
      borderColor: priorityColors[task.priority] || '#165DFF',
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
      const res = await taskApi.update(taskId, { dueDate: newDate })
      taskStore.updateTaskInList(taskId, res.data)
      taskStore.notifyTaskChange()
    } catch {
      info.revert()
    }
  }
}

function handleDateClick(info: any) {
  createDate.value = info.dateStr?.split('T')[0] || ''
  showCreate.value = true
}

function handleTaskCreated() {
  showCreate.value = false
  refetchEvents()
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
watch(() => taskStore.taskEventVersion, () => refetchEvents())
</script>

<style lang="scss" scoped>
.page-header { margin-bottom: 20px; }
.page-title { font-size: 24px; font-weight: 700; color: var(--tf-text-primary); }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.calendar-card {
  :deep(.fc) {
    font-family: inherit;
    .fc-toolbar-title { font-size: 18px; font-weight: 600; color: var(--tf-text-primary); }
    .fc-button-primary {
      background-color: var(--tf-color-primary);
      border-color: var(--tf-color-primary);
      border-radius: var(--tf-radius-sm);
      font-weight: 500;
      &:hover { opacity: 0.9; }
      &:not(:disabled).fc-button-active {
        background-color: var(--tf-color-primary);
        border-color: var(--tf-color-primary);
      }
    }
    .fc-event {
      border-radius: 5px; padding: 2px 6px; font-size: 12px; cursor: pointer;
      border: none; font-weight: 500;
      transition: transform var(--tf-transition-fast), box-shadow var(--tf-transition-fast);
      &:hover {
        transform: scale(1.02);
        box-shadow: 0 2px 6px rgba(0,0,0,0.2);
      }
    }
    .fc-daygrid-day {
      transition: background var(--tf-transition-fast);
      &:hover { background-color: var(--tf-bg-card-hover); }
    }
    .fc-col-header-cell { color: var(--tf-text-secondary); font-weight: 600; }
    .fc-day-today { background: var(--tf-color-primary-lighter) !important; }
    .fc-scrollgrid { border-color: var(--tf-border-color-lighter); }
    td, th { border-color: var(--tf-border-color-lighter); }
  }
}
</style>
