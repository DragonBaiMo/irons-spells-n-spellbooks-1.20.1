<script setup lang="ts">
import { ref, computed, onMounted, onErrorCaptured } from 'vue';
import {
  NLayout, NLayoutSider, NLayoutContent,
  NInput, NList, NListItem, NThing, NTag, NEmpty, NSpin,
  NCard, NGrid, NGi, NInputNumber, NSwitch, NSpace, NDivider,
  NButton, NTooltip, NCode, useMessage, NAlert
} from 'naive-ui';
import { 
  SearchOutline, Star, StarOutline, Flash, 
  CopyOutline, CheckmarkCircleOutline, SettingsOutline 
} from '@vicons/ionicons5';

// --- Error Handling ---
const renderError = ref<string | null>(null);
onErrorCaptured((err) => {
  console.error("Render Error:", err);
  renderError.value = String(err);
  return false; // prevent propagation
});

// --- Types ---
interface Param {
  key: string;
  type: 'INT' | 'DOUBLE' | 'FLOAT' | 'STRING' | 'LONG' | 'BOOLEAN';
  defaultValue: any;
  description: string;
  aliases: string[];
}

interface Skill {
  commandId: string;
  fullId: string;
  displayName: string;
  description: string;
  icon: string; // unused in old code but good to have
  params: Param[];
}

// --- State ---
const skills = ref<Skill[]>([]);
const loading = ref(true);
const searchQuery = ref('');
const filterFav = ref(false);
const selectedSkill = ref<Skill | null>(null);
const favorites = ref<string[]>(JSON.parse(localStorage.getItem('favorites') || '[]'));

// Skill Notes/Aliases (user custom names)
const skillNotes = ref<Record<string, string>>(JSON.parse(localStorage.getItem('skillNotes') || '{}'));
const editingNote = ref(false);
const noteInput = ref('');

// Config State
const caster = ref('@s');
const level = ref(1);
const target = ref('');

// Toggle State
const consumeMana = ref(false);
const triggerCooldown = ref(false);
const playEffects = ref(true);
const bypassConditions = ref(true);
const showCastBar = ref(false);

// Param Overrides
const overrides = ref<Record<string, any>>({});

// --- Computed ---
const filteredSkills = computed(() => {
  const q = searchQuery.value.toLowerCase();
  return skills.value.filter(s => {
    const matchesSearch = s.displayName.toLowerCase().includes(q) || 
                          s.commandId.toLowerCase().includes(q) || 
                          s.fullId.toLowerCase().includes(q);
    const matchesFav = filterFav.value ? favorites.value.includes(s.fullId) : true;
    return matchesSearch && matchesFav;
  });
});

const generatedJson = computed(() => {
  if (!selectedSkill.value) return '{}';
  
  const obj: any = {};
  
  // Global Overrides (only if different from default/standard)
  if (consumeMana.value !== false) obj.consumeMana = true;
  if (triggerCooldown.value !== false) obj.triggerCooldown = true;
  if (playEffects.value !== true) obj.playEffects = false;
  if (bypassConditions.value !== true) obj.bypassConditions = false;
  if (showCastBar.value !== false) obj.showCastBar = true;

  // Skill Params
  for (const [key, val] of Object.entries(overrides.value)) {
    // Only include if it's actually set (though logic below ensures we only add to overrides if user checks "Customize")
    // We convert types here just in case, though v-model.number helps
    obj[key] = val;
  }
  
  return JSON.stringify(obj, null, 2); // Pretty print for display, but maybe compact for copy?
});

const generatedJsonCompact = computed(() => {
    try {
        return JSON.stringify(JSON.parse(generatedJson.value));
    } catch {
        return '{}';
    }
});

const generatedCommand = computed(() => {
  if (!selectedSkill.value) return '';
  let cmd = `/iss cast ${caster.value} ${selectedSkill.value.commandId} ${level.value}`;
  if (target.value) {
    cmd += ` ${target.value}`;
  }
  // Only append JSON if it's not empty object
  const json = generatedJsonCompact.value;
  if (json !== '{}') {
    cmd += ` ${json}`;
  }
  return cmd;
});

// --- Actions ---
onMounted(async () => {
  try {
    const res = await fetch('skills.json');
    if (!res.ok) throw new Error('Failed to load skills');
    skills.value = await res.json();
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
});

function selectSkill(skill: Skill) {
  console.log('Selected skill:', skill.displayName);
  selectedSkill.value = skill;
  // Reset specific configs if needed? Or keep them? 
  // Keep Global configs, reset Params
  overrides.value = {};
}

function toggleFavorite(id: string, e?: Event) {
  e?.stopPropagation();
  if (favorites.value.includes(id)) {
    favorites.value = favorites.value.filter(f => f !== id);
  } else {
    favorites.value.push(id);
  }
  localStorage.setItem('favorites', JSON.stringify(favorites.value));
}

function startEditNote() {
  if (selectedSkill.value) {
    noteInput.value = skillNotes.value[selectedSkill.value.fullId] || '';
    editingNote.value = true;
  }
}

function saveNote() {
  if (selectedSkill.value) {
    if (noteInput.value.trim()) {
      skillNotes.value[selectedSkill.value.fullId] = noteInput.value.trim();
    } else {
      delete skillNotes.value[selectedSkill.value.fullId];
    }
    localStorage.setItem('skillNotes', JSON.stringify(skillNotes.value));
  }
  editingNote.value = false;
}

function cancelEditNote() {
  editingNote.value = false;
  noteInput.value = '';
}

function toggleParamOverride(param: Param, active: boolean) {
  if (active) {
    overrides.value[param.key] = param.defaultValue;
  } else {
    delete overrides.value[param.key];
  }
}

// Use message here (safe because MainLayout will be child of NMessageProvider)
const message = useMessage();

function copyToClipboard(text: string) {
  navigator.clipboard.writeText(text).then(() => {
    message.success('已复制到剪贴板');
  }).catch(() => {
    message.error('复制失败');
  });
}
</script>

<template>
    <NLayout class="app-layout" has-sider>
      <!-- Sidebar -->
      <NLayoutSider
        width="320"
        bordered
        collapse-mode="width"
        :native-scrollbar="false"
        class="sidebar"
      >
        <div class="sidebar-header">
          <h2 class="app-title">
            <span class="icon"><Flash /></span> 指令生成器
          </h2>
          <NInput v-model:value="searchQuery" placeholder="搜索技能 ID 或名称..." clearable>
            <template #prefix>
              <SearchOutline class="icon-small" />
            </template>
          </NInput>
          <div class="filter-bar">
            <NButton 
              size="small" 
              quaternary 
              :type="filterFav ? 'warning' : 'default'"
              @click="filterFav = !filterFav"
            >
              <template #icon>
                <Star v-if="filterFav" />
                <StarOutline v-else />
              </template>
              {{ filterFav ? '已显示收藏' : '显示全部' }}
            </NButton>
            <NTag size="small" checkable>{{ filteredSkills.length }} 个技能</NTag>
          </div>
        </div>
        
        <div v-if="loading" class="loading-state">
          <NSpin size="medium" />
        </div>
        
                <NList hoverable clickable v-else>
                    <NListItem 
                        v-for="skill in filteredSkills"
                        :key="skill.fullId"
                        class="skill-list-item"
                    >
                        <div 
                            class="skill-inner" 
                            :class="{ 'selected': selectedSkill?.fullId === skill.fullId }"
                            @click="selectSkill(skill)"
                        >
                            <NThing>
                                <template #header>
                                    <div class="list-item-header">
                                        <div class="skill-name-wrapper">
                                          <span v-if="skillNotes[skill.fullId]" class="skill-note">{{ skillNotes[skill.fullId] }}</span>
                                          <span class="skill-name" :class="{ 'has-note': skillNotes[skill.fullId] }">{{ skill.displayName }}</span>
                                        </div>
                                    </div>
                                </template>
                                <template #description>
                                    <span class="skill-id">{{ skill.commandId }}</span>
                                </template>
                                <template #header-extra>
                                    <NButton 
                                        text 
                                        class="fav-btn"
                                        :class="{ active: favorites.includes(skill.fullId) }"
                                        @click.stop="toggleFavorite(skill.fullId)"
                                    >
                                        <template #icon>
                                            <Star v-if="favorites.includes(skill.fullId)" />
                                            <StarOutline v-else />
                                        </template>
                                    </NButton>
                                </template>
                            </NThing>
                        </div>
                    </NListItem>
                    <div v-if="filteredSkills.length === 0" class="empty-list">
                        <NEmpty description="未找到匹配的技能" />
                    </div>
                </NList>
      </NLayoutSider>

      <!-- Main Content -->
      <NLayoutContent class="main-content" :native-scrollbar="false">
        <div v-if="renderError" class="error-state" style="padding: 20px;">
            <NAlert title="渲染错误" type="error">
                {{ renderError }}
            </NAlert>
        </div>
        
        <div v-else-if="!selectedSkill" class="empty-state">
           <div class="empty-content">
             <Flash class="empty-icon" />
             <h2>请选择一个技能</h2>
             <p>在左侧列表中点击技能以开始配置</p>
           </div>
        </div>
        
        <div v-else class="detail-view">
          <!-- Header -->
          <header class="detail-header">
             <div class="header-main">
                 <template v-if="skillNotes[selectedSkill.fullId]">
                   <h1 class="note-title">{{ skillNotes[selectedSkill.fullId] }}</h1>
                   <h2 class="original-title">{{ selectedSkill.displayName }}</h2>
                 </template>
                 <h1 v-else>{{ selectedSkill.displayName }}</h1>
                 <NTag type="info" size="small" bordered>{{ selectedSkill.fullId }}</NTag>
             </div>
             <NSpace>
               <NTooltip trigger="hover">
                 <template #trigger>
                   <NButton circle secondary @click="startEditNote">
                     <template #icon>📝</template>
                   </NButton>
                 </template>
                 备注
               </NTooltip>
               <NButton circle secondary type="warning" @click="toggleFavorite(selectedSkill.fullId)">
                  <template #icon>
                      <Star v-if="favorites.includes(selectedSkill.fullId)" />
                      <StarOutline v-else />
                  </template>
               </NButton>
             </NSpace>
          </header>
          
          <!-- Note Edit Modal -->
          <NCard v-if="editingNote" title="编辑备注" size="small" class="note-edit-card">
            <NSpace vertical>
              <NInput v-model:value="noteInput" placeholder="输入备注名称（留空清除备注）" />
              <NSpace justify="end">
                <NButton size="small" @click="cancelEditNote">取消</NButton>
                <NButton size="small" type="primary" @click="saveNote">保存</NButton>
              </NSpace>
            </NSpace>
          </NCard>
          
          <div class="content-grid">
             <!-- Output (moved to top) -->
             <NCard title="生成结果" size="small" class="output-card">
                <NSpace vertical>
                    <div class="code-block-wrapper">
                        <div class="code-header">
                            <span>完整指令</span>
                            <NButton size="tiny" secondary type="primary" @click="copyToClipboard(generatedCommand)">
                                <template #icon><CopyOutline /></template> 复制
                            </NButton>
                        </div>
                        <div class="code-box">{{ generatedCommand }}</div>
                    </div>
                    
                    <div class="code-block-wrapper">
                         <div class="code-header">
                            <span>参数 JSON</span>
                            <NButton size="tiny" secondary type="primary" @click="copyToClipboard(generatedJsonCompact)">
                                <template #icon><CopyOutline /></template> 复制
                            </NButton>
                        </div>
                        <div class="code-box json">{{ generatedJson }}</div>
                    </div>
                </NSpace>
             </NCard>
             <!-- Description -->
             <NCard title="技能说明" size="small" class="desc-card">
                 <div class="md-content" v-html="selectedSkill.description ? selectedSkill.description.replace(/\n/g, '<br>') : '暂无说明'"></div>
             </NCard>
             
             <!-- Base Config -->
             <NGrid x-gap="12" y-gap="12" cols="1 600:2">
                <NGi>
                    <NCard title="施法配置" size="small">
                        <template #header-extra><SettingsOutline class="card-icon"/></template>
                        <NSpace vertical>
                            <div class="form-item">
                                <label>施法者 (Caster)</label>
                                <NInput v-model:value="caster" placeholder="@s" />
                            </div>
                            <div class="form-item">
                                <label>等级 (Level)</label>
                                <NInputNumber v-model:value="level" :min="1" />
                            </div>
                            <div class="form-item">
                                <label>目标 (Target)</label>
                                <NInput v-model:value="target" placeholder="留空为默认" />
                            </div>
                        </NSpace>
                    </NCard>
                </NGi>
                <NGi>
                    <NCard title="通用控制" size="small">
                        <template #header-extra><SettingsOutline class="card-icon"/></template>
                        <NSpace vertical>
                            <div class="switch-row">
                                <span>消耗蓝量</span>
                                <NSwitch v-model:value="consumeMana" />
                            </div>
                            <div class="switch-row">
                                <span>触发冷却</span>
                                <NSwitch v-model:value="triggerCooldown" />
                            </div>
                            <div class="switch-row">
                                <span>播放特效</span>
                                <NSwitch v-model:value="playEffects" />
                            </div>
                            <div class="switch-row">
                                <span>绕过条件</span>
                                <NSwitch v-model:value="bypassConditions" />
                            </div>
                            <div class="switch-row">
                                <span>显示施法条</span>
                                <NSwitch v-model:value="showCastBar" />
                            </div>
                        </NSpace>
                    </NCard>
                </NGi>
             </NGrid>
             
             <!-- Params -->
             <NCard title="技能参数 (Overrides)" size="small" class="params-card">
                 <template #header-extra>
                     <NTag type="success" size="small" v-if="selectedSkill.params && selectedSkill.params.length > 0">
                        {{ selectedSkill.params.length }} 个可用参数
                     </NTag>
                 </template>
                 
                 <div v-if="!selectedSkill.params || selectedSkill.params.length === 0" class="no-params">
                     此技能没有可配置的额外参数。
                 </div>
                 
                 <div v-else class="params-grid">
                     <div v-for="param in selectedSkill.params" :key="param.key" class="param-item">
                         <div class="param-header">
                             <div class="param-label">
                                 <span class="key">{{ param.key }}</span>
                                 <span class="type">{{ param.type }}</span>
                             </div>
                             <NSwitch 
                                size="small"
                                :value="param.key in overrides"
                                @update:value="(v) => toggleParamOverride(param, v)"
                             >
                                <template #checked>自定义</template>
                                <template #unchecked>默认</template>
                             </NSwitch>
                         </div>
                         
                         <div class="param-input-area">
                             <NInputNumber 
                                v-if="['INT', 'DOUBLE', 'FLOAT', 'LONG'].includes(param.type)"
                                v-model:value="overrides[param.key]"
                                :disabled="!(param.key in overrides)"
                                placeholder="默认值"
                                :step="param.type === 'INT' ? 1 : 0.1"
                             />
                             <NSwitch
                                v-else-if="param.type === 'BOOLEAN'"
                                v-model:value="overrides[param.key]"
                                :disabled="!(param.key in overrides)"
                             />
                             <NInput 
                                v-else
                                v-model:value="overrides[param.key]"
                                :disabled="!(param.key in overrides)"
                                placeholder="Value"
                             />
                             <div class="default-val">默认值: {{ param.defaultValue }}</div>
                         </div>
                         <div class="param-desc">{{ param.description }}</div>
                     </div>
                 </div>
             </NCard>
          </div>
        </div>
      </NLayoutContent>
    </NLayout>
</template>

<style scoped>
.app-layout {
  height: 100vh;
}

.sidebar {
  background-color: rgb(16, 16, 20);
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.09);
}

.app-title {
  margin: 0 0 16px 0;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(to right, #63e2b7, #2a947d);
  -webkit-background-clip: text;
  color: transparent;
}
.app-title .icon {
    color: #63e2b7;
}

.filter-bar {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-state, .empty-list {
    padding: 32px;
    display: flex;
    justify-content: center;
}

.list-item-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.skill-name-wrapper {
    display: flex;
    flex-direction: column;
}

.skill-note {
    font-weight: 600;
    font-size: 14px;
    color: #63e2b7;
}

.skill-name {
    font-weight: 500;
    font-size: 14px;
}

.skill-name.has-note {
    font-size: 12px;
    color: rgba(255,255,255,0.5);
    font-weight: normal;
}

.skill-id {
    font-size: 12px;
    color: rgba(255,255,255,0.5);
}

.fav-btn {
    color: rgba(255,255,255,0.3);
}
.fav-btn.active {
    color: #f2c97d;
}

.skill-list-item {
    padding: 0 !important; /* Override NListItem padding to let inner div handle it */
}

.skill-inner {
    padding: 12px 20px;
    cursor: pointer;
    transition: background-color 0.2s;
}

.skill-inner:hover {
    background-color: rgba(255, 255, 255, 0.05);
}

.skill-inner.selected {
    background-color: rgba(99, 226, 183, 0.15);
}

/* Main Content */
.main-content {
    background-color: #101014;
}

.empty-state {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: rgba(255,255,255,0.3);
}
.empty-content {
    text-align: center;
}
.empty-icon {
    font-size: 64px;
    margin-bottom: 16px;
    opacity: 0.2;
}

.detail-view {
    padding: 24px;
    max-width: 1200px;
    margin: 0 auto;
}

.detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}

.header-main h1 {
    margin: 0 0 8px 0;
    font-size: 24px;
}

.header-main .note-title {
    margin: 0 0 4px 0;
    font-size: 24px;
    color: #63e2b7;
}

.header-main .original-title {
    margin: 0 0 8px 0;
    font-size: 14px;
    color: rgba(255,255,255,0.5);
    font-weight: normal;
}

.note-edit-card {
    margin-bottom: 16px;
}

.content-grid {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.switch-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 4px 0;
}

.form-item {
    margin-bottom: 8px;
}
.form-item label {
    display: block;
    margin-bottom: 4px;
    font-size: 12px;
    color: rgba(255,255,255,0.6);
}

/* Params Grid */
.params-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 16px;
}

.param-item {
    background: rgba(255,255,255,0.03);
    border-radius: 8px;
    padding: 12px;
    border: 1px solid rgba(255,255,255,0.05);
}

.param-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.param-label {
    display: flex;
    flex-direction: column;
}

.param-label .key {
    font-family: monospace;
    color: #63e2b7;
    font-weight: 600;
}
.param-label .type {
    font-size: 10px;
    color: rgba(255,255,255,0.4);
}

.default-val {
    font-size: 11px;
    color: rgba(255,255,255,0.3);
    margin-top: 4px;
}

.param-desc {
    margin-top: 8px;
    font-size: 12px;
    color: rgba(255,255,255,0.7);
    line-height: 1.4;
}

.code-block-wrapper {
    background: #000;
    border-radius: 6px;
    overflow: hidden;
    border: 1px solid rgba(255,255,255,0.1);
}

.code-header {
    padding: 6px 12px;
    background: rgba(255,255,255,0.05);
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 12px;
    color: rgba(255,255,255,0.6);
}

.code-box {
    padding: 12px;
    font-family: 'JetBrains Mono', monospace;
    font-size: 13px;
    color: #a5b4fc;
    word-break: break-all;
    white-space: pre-wrap;
}

.card-icon {
    font-size: 16px;
    opacity: 0.7;
}

.md-content {
    font-size: 13px;
    line-height: 1.6;
    color: rgba(255,255,255,0.8);
}
</style>
