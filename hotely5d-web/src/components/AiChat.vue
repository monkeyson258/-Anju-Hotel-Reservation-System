<template>
  <div class="ai-chat-wrapper">
    <transition name="chat-fade">
      <div v-if="visible" class="ai-chat-panel">
        <div class="chat-header">
          <div class="header-info">
            <i class="el-icon-service"></i>
            <span>智能客服</span>
            <el-tag size="mini" :type="lastSource === 'deepseek' ? 'success' : 'info'" effect="plain">
              {{ lastSource === 'deepseek' ? 'DeepSeek' : '在线' }}
            </el-tag>
          </div>
          <div class="header-actions">
            <i class="el-icon-delete" title="清空对话" @click="clearChat"></i>
            <i class="el-icon-close close-btn" @click="visible = false"></i>
          </div>
        </div>
        <div class="chat-body" ref="chatBody">
          <div v-for="(msg, index) in messages" :key="index" :class="['chat-msg', msg.role]">
            <div class="msg-avatar">
              <i :class="msg.role === 'user' ? 'el-icon-user' : 'el-icon-service'"></i>
            </div>
            <div class="msg-content">
              <div class="msg-text" v-html="formatMsg(msg.content)"></div>
              <div v-if="msg.source === 'deepseek'" class="msg-badge">由 DeepSeek AI 生成</div>
            </div>
          </div>
          <div v-if="loading" class="chat-msg assistant">
            <div class="msg-avatar"><i class="el-icon-service"></i></div>
            <div class="msg-content">
              <div class="msg-text typing"><span></span><span></span><span></span></div>
              <div class="msg-hint">AI 正在思考，请稍候...</div>
            </div>
          </div>
        </div>
        <div class="chat-quick" v-if="showQuickQuestions">
          <el-tag
            v-for="q in quickQuestions"
            :key="q"
            size="small"
            effect="plain"
            class="quick-tag"
            @click="sendQuick(q)"
          >{{ q }}</el-tag>
        </div>
        <div class="chat-footer">
          <el-input
            v-model="inputText"
            placeholder="问问房型、价格、预订方式..."
            size="small"
            @keyup.enter.native="sendMessage"
            :disabled="loading"
          >
            <el-button slot="append" icon="el-icon-s-promotion" @click="sendMessage" :loading="loading">发送</el-button>
          </el-input>
        </div>
      </div>
    </transition>
    <div class="ai-chat-trigger" @click="toggleChat">
      <i class="el-icon-chat-dot-round"></i>
      <span v-if="!visible" class="trigger-label">智能客服</span>
    </div>
  </div>
</template>

<script>
import { chatAPI } from '@/api/ai'

export default {
  name: 'AiChat',
  props: {
    greeting: {
      type: String,
      default: '您好！我是斌安居酒店智能客服，已接入 DeepSeek AI。我可以根据酒店实时房态，为您解答房型价格、预订方式、酒店设施等问题，有什么可以帮您？'
    }
  },
  data() {
    return {
      visible: false,
      inputText: '',
      loading: false,
      messages: [],
      lastSource: 'local',
      quickQuestions: ['有哪些房型？', '现在能预订吗？', '酒店在哪里？', '有什么设施？', '最新公告']
    }
  },
  computed: {
    showQuickQuestions() {
      return this.messages.filter(m => m.role === 'user').length === 0
    }
  },
  methods: {
    toggleChat() {
      this.visible = !this.visible
      if (this.visible && this.messages.length === 0) {
        this.messages.push({ role: 'assistant', content: this.greeting, isGreeting: true })
      }
      this.$nextTick(() => this.scrollToBottom())
    },
    clearChat() {
      this.$confirm('确定清空当前对话吗？', '提示', { type: 'warning' }).then(() => {
        this.messages = [{ role: 'assistant', content: this.greeting, isGreeting: true }]
        this.lastSource = 'local'
      }).catch(() => {})
    },
    formatMsg(text) {
      if (!text) return ''
      return text
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/\n/g, '<br>')
    },
    sendQuick(q) {
      this.inputText = q
      this.sendMessage()
    },
    buildHistory() {
      return this.messages
        .filter(m => !m.isGreeting && m.role && m.content)
        .slice(-10)
        .map(m => ({
          role: m.role === 'assistant' ? 'assistant' : 'user',
          content: m.content
        }))
    },
    async sendMessage() {
      const text = this.inputText.trim()
      if (!text || this.loading) return
      this.messages.push({ role: 'user', content: text })
      this.inputText = ''
      this.loading = true
      this.$nextTick(() => this.scrollToBottom())
      try {
        const { data } = await chatAPI({
          message: text,
          history: this.buildHistory().slice(0, -1)
        })
        this.lastSource = data.source || 'local'
        this.messages.push({
          role: 'assistant',
          content: data.reply,
          source: data.source
        })
      } catch (e) {
        this.messages.push({
          role: 'assistant',
          content: '抱歉，服务暂时繁忙，请稍后再试。您也可以直接前往「预约入住」页面浏览房型。'
        })
      } finally {
        this.loading = false
        this.$nextTick(() => this.scrollToBottom())
      }
    },
    scrollToBottom() {
      const body = this.$refs.chatBody
      if (body) body.scrollTop = body.scrollHeight
    }
  }
}
</script>

<style lang="less" scoped>
.ai-chat-wrapper {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 2000;
}

.ai-chat-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #c9a962, #a88b4a);
  color: #141820;
  padding: 12px 20px;
  border-radius: 28px;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(201, 169, 98, 0.45);
  font-weight: 500;
  transition: all 0.3s;
  font-size: 14px;

  i { font-size: 22px; }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(201, 169, 98, 0.55);
  }
}

.ai-chat-panel {
  position: absolute;
  right: 0;
  bottom: 60px;
  width: 400px;
  height: 540px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: linear-gradient(135deg, #141820, #2a3142);
  color: #fff;
  border-bottom: 1px solid rgba(201, 169, 98, 0.2);

  .header-info {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 12px;

    i {
      cursor: pointer;
      font-size: 16px;
      opacity: 0.85;
      &:hover { opacity: 1; }
    }

    .close-btn { font-size: 18px; }
  }
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7fa;
}

.chat-msg {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;

  &.user {
    flex-direction: row-reverse;
    .msg-content { align-items: flex-end; }
    .msg-text { background: linear-gradient(135deg, #c9a962, #a88b4a); color: #141820; border-radius: 12px 2px 12px 12px; }
    .msg-avatar { background: linear-gradient(135deg, #c9a962, #a88b4a); color: #141820; }
  }

  &.assistant {
    .msg-text { background: #fff; color: #333; border-radius: 2px 12px 12px 12px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
    .msg-avatar { background: #67c23a; }
  }
}

.msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  font-size: 16px;
}

.msg-content { display: flex; flex-direction: column; max-width: 78%; }

.msg-text {
  padding: 10px 14px;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
}

.msg-badge, .msg-hint {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
  padding-left: 4px;
}

.typing span {
  display: inline-block;
  width: 6px;
  height: 6px;
  background: #999;
  border-radius: 50%;
  margin: 0 2px;
  animation: blink 1.4s infinite both;
  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}

@keyframes blink {
  0%, 80%, 100% { opacity: 0.3; }
  40% { opacity: 1; }
}

.chat-quick {
  padding: 0 16px 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  background: #f5f7fa;

  .quick-tag {
    cursor: pointer;
    &:hover { color: #409eff; border-color: #409eff; }
  }
}

.chat-footer {
  padding: 12px 16px;
  border-top: 1px solid #eee;
  background: #fff;
}

.chat-fade-enter-active, .chat-fade-leave-active {
  transition: all 0.3s;
}
.chat-fade-enter, .chat-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
