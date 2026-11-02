<template>
  <div class="cap-page">
    <div class="cap-container">

      <div class="cap-crumb">
        <span class="crumb-link" @click="$router.push('/dashboard')">工作台</span>
        <span class="crumb-sep">/</span>
        <span class="crumb-now">能力中心</span>
      </div>

      <header class="cap-header">
        <h1 class="cap-title">能力中心</h1>
        <p class="cap-sub">把常做的文档任务做成一条指令：选技能直接开始，选模板直接建文档。</p>
      </header>

      <el-tabs v-model="tab" class="cap-tabs">
        <el-tab-pane label="技能" name="skills">
          <div class="cap-chips">
            <button
                v-for="cat in SKILL_CATEGORIES"
                :key="cat"
                class="cap-chip"
                :class="{ active: activeCat === cat }"
                @click="activeCat = cat"
            >{{ cat }}</button>
          </div>

          <div class="cap-grid">
            <article class="cap-card" v-for="skill in filteredSkills" :key="skill.id" @click="useSkill(skill)">
              <div class="cap-top">
                <h3 class="cap-name">{{ skill.name }}</h3>
                <span class="cap-cat">{{ skill.category }}</span>
              </div>
              <p class="cap-desc">{{ skill.desc }}</p>
              <span class="cap-go">开始 →</span>
            </article>
          </div>
        </el-tab-pane>

        <el-tab-pane label="模板" name="templates">
          <p class="cap-note">模板会直接在云端建一份新文档并打开编辑器，正文骨架已经写好。</p>

          <div class="cap-grid">
            <article class="cap-card" v-for="tpl in TEMPLATES" :key="tpl.id" @click="useTemplate(tpl)">
              <div class="cap-top">
                <h3 class="cap-name">{{ tpl.name }}</h3>
                <span class="cap-cat">{{ tpl.category }}</span>
              </div>
              <p class="cap-desc">{{ tpl.desc }}</p>
              <span class="cap-go" :class="{ 'is-busy': creatingId === tpl.id }">
                {{ creatingId === tpl.id ? '正在创建…' : '用这个模板 →' }}
              </span>
            </article>
          </div>
        </el-tab-pane>
      </el-tabs>

      <div class="cap-foot">
        <span>技能与模板都在本地定义，可随时增删：</span>
        <code>src/constants/capabilities.js</code>
      </div>
    </div>
  </div>
</template>

<script setup>

/**
 * 能力中心页面：按分类展示技能与模板，一键发起任务或创建文档。
 */
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { SKILLS, TEMPLATES, SKILL_CATEGORIES } from '../../constants/capabilities'
import { docApi } from '../../api/document'

const router = useRouter()
const tab = ref('skills')
const activeCat = ref('全部')
const creatingId = ref('')

const filteredSkills = computed(() =>
    activeCat.value === '全部' ? SKILLS : SKILLS.filter((skill) => skill.category === activeCat.value)
)

// 技能：带着写好的指令进入对话模式（与工作台输入框同一条链路）
const useSkill = (skill) => {
  sessionStorage.setItem('global_ai_task', skill.prompt)
  router.push('/editor/chat-mode')
}

// 模板：真实创建一份文档，再进编辑器
const useTemplate = async (tpl) => {
  if (creatingId.value) return
  creatingId.value = tpl.id
  try {
    const res = await docApi.createDoc({ title: tpl.title, content: tpl.content, category: '模板' })
    const id = res?.data?.id
    if (!id) throw new Error('创建文档失败')
    sessionStorage.setItem('currentDocName', tpl.title)
    ElMessage.success('已用模板创建文档')
    router.push(`/editor/${id}`)
  } catch (e) {
    console.error('模板创建失败', e)
    ElMessage.error('模板创建失败，请稍后再试')
  } finally {
    creatingId.value = ''
  }
}
</script>

<style scoped>
.cap-page {
  height: 100vh;
  background: var(--pd-bg);
  overflow-y: auto;
}

.cap-container {
  width: 100%;
  max-width: 1050px;
  margin: 0 auto;
  padding: 32px 32px 64px;
}

/* 面包屑：只给层级，不给图标 */
.cap-crumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12.5px;
  color: var(--pd-text-3);
  margin-bottom: 18px;
}
.crumb-link { color: var(--pd-text-2); cursor: pointer; transition: color var(--pd-dur-fast) var(--pd-ease); }
.crumb-link:hover { color: var(--pd-primary-text); }
.crumb-sep { color: var(--pd-border-strong); }
.crumb-now { color: var(--pd-text-3); }

.cap-header { margin-bottom: 20px; }
.cap-title {
  margin: 0 0 8px;
  font-size: 26px;
  font-weight: 600;
  letter-spacing: -0.01em;
  color: var(--pd-text);
}
.cap-sub { margin: 0; font-size: 13.5px; color: var(--pd-text-2); }

/* 分类胶囊 */
.cap-chips { display: flex; flex-wrap: wrap; gap: 8px; margin: 4px 0 20px; }
.cap-chip {
  padding: 5px 12px;
  border-radius: var(--pd-r-pill);
  border: 1px solid var(--pd-border);
  background: transparent;
  color: var(--pd-text-2);
  font-family: inherit;
  font-size: 12.5px;
  cursor: pointer;
  transition: border-color var(--pd-dur-fast) var(--pd-ease), color var(--pd-dur-fast) var(--pd-ease),
    background var(--pd-dur-fast) var(--pd-ease);
}
.cap-chip:hover { border-color: var(--pd-primary-hover); color: var(--pd-primary-text); }
.cap-chip.active {
  background: var(--pd-primary-weak);
  border-color: color-mix(in oklab, var(--pd-primary) 45%, transparent);
  color: var(--pd-primary-text);
}

.cap-note { margin: 4px 0 20px; font-size: 13px; color: var(--pd-text-3); }

.cap-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

/* 卡片：整卡可点，底部一行动作是唯一强调 */
.cap-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 18px 20px 16px;
  background: var(--pd-surface);
  border: 1px solid var(--pd-border);
  border-radius: var(--pd-r-lg);
  box-shadow: var(--pd-shadow-sm);
  cursor: pointer;
  transition: border-color var(--pd-dur-fast) var(--pd-ease), box-shadow var(--pd-dur) var(--pd-ease),
    transform var(--pd-dur) var(--pd-ease);
}
.cap-card:hover {
  border-color: var(--pd-border-strong);
  box-shadow: var(--pd-shadow-md);
  transform: translateY(-2px);
}

.cap-top { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.cap-name { margin: 0; font-size: 15px; font-weight: 600; color: var(--pd-text); }
.cap-cat {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: var(--pd-r-sm);
  background: var(--pd-surface-muted);
  color: var(--pd-text-3);
  font-size: 11.5px;
}
.cap-desc { margin: 0; font-size: 13px; line-height: 1.6; color: var(--pd-text-2); }
.cap-go {
  margin-top: 4px;
  font-size: 12.5px;
  color: var(--pd-primary-text);
  transition: color var(--pd-dur-fast) var(--pd-ease);
}
.cap-card:hover .cap-go { color: var(--pd-primary-hover); }
.cap-go.is-busy { color: var(--pd-text-3); }

.cap-foot {
  margin-top: 28px;
  padding-top: 16px;
  border-top: 1px solid var(--pd-border);
  font-size: 12px;
  color: var(--pd-text-3);
}
.cap-foot code {
  font-family: var(--pd-font-mono);
  color: var(--pd-text-2);
}
</style>
