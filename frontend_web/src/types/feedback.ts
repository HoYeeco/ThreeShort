/**
 * 反馈建议类型定义
 */

// 反馈类型枚举
export enum FeedbackType {
  SUGGESTION = 'SUGGESTION',      // 建议
  COMPLAINT = 'COMPLAINT',        // 投诉
  LEARNING_FEEDBACK = 'LEARNING_FEEDBACK', // 学习反馈
  OTHER = 'OTHER'                 // 其他
}

// 反馈状态枚举
export enum FeedbackStatus {
  PENDING = 'PENDING',       // 待处理
  PROCESSING = 'PROCESSING', // 处理中
  RESOLVED = 'RESOLVED',     // 已解决
  CLOSED = 'CLOSED'          // 已关闭
}

// 反馈建议实体
export interface FeedbackSuggestion {
  id: number
  userId: number
  type: FeedbackType
  title: string
  content: string
  attachmentFiles: string[]
  attachmentFileList?: string[]  // 后端返回的解析后的附件URL列表
  status: FeedbackStatus
  handlerId?: number
  handlerReply?: string
  handleTime?: string
  createdTime: string
  updatedTime: string
}

// 反馈建议提交表单
export interface FeedbackSubmitForm {
  type: FeedbackType
  title: string
  content: string
  attachmentFileIds?: number[]
}

// 反馈建议查询参数
export interface FeedbackQueryForm {
  page: number
  size: number
  keyword?: string
  type?: FeedbackType
  status?: FeedbackStatus
  userId?: number
  handlerId?: number
}

// 反馈建议处理表单
export interface FeedbackHandleForm {
  status: FeedbackStatus
  handlerReply: string
}

// 反馈建议列表响应
export interface FeedbackListResponse {
  records: FeedbackSuggestion[]
  total: number
  size: number
  current: number
  pages: number
}

// 文件上传响应
export interface FileUploadResponse {
  fileId: number
  originalName: string
  fileUrl: string
  fileSize: number
  fileType: string
}

// 反馈类型选项
export const FeedbackTypeOptions = [
  { value: FeedbackType.SUGGESTION, label: '建议' },
  { value: FeedbackType.COMPLAINT, label: '投诉' },
  { value: FeedbackType.LEARNING_FEEDBACK, label: '学习反馈' },
  { value: FeedbackType.OTHER, label: '其他' }
]

// 反馈状态选项
export const FeedbackStatusOptions = [
  { value: FeedbackStatus.PENDING, label: '待处理' },
  { value: FeedbackStatus.PROCESSING, label: '处理中' },
  { value: FeedbackStatus.RESOLVED, label: '已解决' },
  { value: FeedbackStatus.CLOSED, label: '已关闭' }
] 