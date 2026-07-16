import { ElMessageBox } from 'element-plus'

// 与后台设计系统对齐的基础配置
const baseOptions = {
  confirmButtonText: '确定',
  cancelButtonText: '取消',
  showCancelButton: true,
  closeOnClickModal: false,
  closeOnPressEscape: false,
  roundButton: true,
  draggable: true,
}

/**
 * 普通操作确认弹窗（品牌色主按钮）
 * @param {string} message 提示内容
 * @param {string} [title='操作确认'] 标题
 * @returns {Promise<boolean>} 确定返回 true，取消/关闭返回 false
 */
export async function confirmAction(message, title = '操作确认') {
  try {
    await ElMessageBox.confirm(message, title, {
      ...baseOptions,
      type: 'warning',
      customClass: 'pf-confirm',
    })
    return true
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') console.error('[confirmAction]', e)
    return false
  }
}

/**
 * 危险 / 删除操作确认弹窗（红色确认按钮，警示更醒目）
 * @param {string} message 提示内容
 * @param {string} [title='删除确认'] 标题
 * @returns {Promise<boolean>} 确定返回 true，取消/关闭返回 false
 */
export async function confirmDanger(message, title = '删除确认') {
  try {
    await ElMessageBox.confirm(message, title, {
      ...baseOptions,
      type: 'warning',
      customClass: 'pf-confirm pf-confirm--danger',
    })
    return true
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') console.error('[confirmDanger]', e)
    return false
  }
}
