import request from '@/utils/request'
import type {
  FeedbackSuggestion,
  FeedbackSubmitForm,
  FeedbackQueryForm,
  FeedbackHandleForm,
  FeedbackListResponse,
  FileUploadResponse
} from '@/types/feedback'

/**
 * 提交反馈建议
 */
export const submitFeedback = (data: FeedbackSubmitForm) => {
  return request.post<number>('/feedback', data)
}

/**
 * 获取反馈建议列表
 */
export const getFeedbackList = (params: FeedbackQueryForm) => {
  return request.get<FeedbackListResponse>('/feedback/list', { params })
}

/**
 * 获取我的反馈建议列表
 */
export const getMyFeedbacks = (params: FeedbackQueryForm) => {
  return request.get<FeedbackListResponse>('/feedback/my', { params })
}

/**
 * 获取反馈建议详情
 */
export const getFeedbackDetail = (feedbackId: number) => {
  return request.get<FeedbackSuggestion>(`/feedback/${feedbackId}`)
}

/**
 * 处理反馈建议
 */
export const handleFeedback = (feedbackId: number, data: FeedbackHandleForm) => {
  return request.put<string>(`/feedback/${feedbackId}/handle`, data)
}

/**
 * 上传单个文件
 */
export const uploadFile = (file: File, businessType: string = 'FEEDBACK') => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('businessType', businessType)
  formData.append('userId', '1')
  
  return request.post<FileUploadResponse>('/files/upload', formData)
}

/**
 * 批量上传文件
 */
export const uploadFiles = (files: File[], businessType: string = 'FEEDBACK') => {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  formData.append('businessType', businessType)
  formData.append('userId', '1')
  
  return request.post<FileUploadResponse[]>('/files/upload/batch', formData)
}

/**
 * 删除文件
 */
export const deleteFile = (fileId: number) => {
  return request.delete<string>(`/files/${fileId}`)
} 