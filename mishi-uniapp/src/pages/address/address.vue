<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import api from '../../utils/api.js'

const addresses = ref([])
const showForm = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({ contactName: '', phone: '', province: '', city: '', district: '', detail: '' })

onShow(() => fetch())

const fetch = async () => {
  try { const res = await api.getAddressListAPI(); addresses.value = res.data } catch (e) {}
}

const openAdd = () => {
  isEdit.value = false; editId.value = null
  form.value = { contactName: '', phone: '', province: '', city: '', district: '', detail: '' }
  showForm.value = true
}

const openEdit = (addr) => {
  isEdit.value = true; editId.value = addr.id
  form.value = { contactName: addr.contactName, phone: addr.phone, province: addr.province || '', city: addr.city || '', district: addr.district || '', detail: addr.detail }
  showForm.value = true
}

const doSave = async () => {
  if (!form.value.contactName || !form.value.phone || !form.value.detail) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' }); return
  }
  try {
    if (isEdit.value) {
      await api.editAddressAPI(editId.value, form.value)
    } else {
      await api.addAddressAPI(form.value)
    }
    showForm.value = false; fetch()
    uni.showToast({ title: isEdit.value ? '已更新' : '已添加', icon: 'success' })
  } catch (e) {}
}

const doDelete = (id) => {
  uni.showModal({ title: '确认删除', content: '删除后无法恢复', success: async (res) => {
    if (res.confirm) {
      try { await api.deleteAddressAPI(id); fetch(); uni.showToast({ title: '已删除', icon: 'success' }) } catch (e) {}
    }
  }})
}

const setDefault = async (id) => {
  try { await api.setDefaultAddressAPI(id); fetch(); uni.showToast({ title: '已设为默认', icon: 'success' }) } catch (e) {}
}
</script>

<template>
  <view class="page">
    <view v-if="addresses.length" class="list">
      <view v-for="a in addresses" :key="a.id" class="addr-card" :class="{ 'is-default': a.isDefault === 1 }">
        <view class="addr-info" @click="openEdit(a)">
          <text class="contact">{{ a.contactName }}  {{ a.phone }}</text>
          <text class="detail">{{ (a.province||'')+(a.city||'')+(a.district||'')+' '+a.detail }}</text>
          <text v-if="a.isDefault === 1" class="tag">默认</text>
        </view>
        <view class="addr-actions">
          <text v-if="a.isDefault !== 1" class="act" @click="setDefault(a.id)">设为默认</text>
          <text class="act del" @click="doDelete(a.id)">删除</text>
        </view>
      </view>
    </view>
    <view v-else class="empty">
      <text class="emp-icon">📍</text>
      <text class="emp-text">暂无收货地址</text>
    </view>

    <view class="bottom-bar">
      <button class="add-btn" @click="openAdd">+ 新增地址</button>
    </view>

    <view v-if="showForm" class="mask" @click="showForm=false">
      <view class="form-panel" @click.stop>
        <text class="fp-title">{{ isEdit ? '编辑地址' : '新增地址' }}</text>
        <input class="fp-input" v-model="form.contactName" placeholder="联系人姓名" />
        <input class="fp-input" v-model="form.phone" type="number" maxlength="11" placeholder="手机号码" />
        <view class="fp-row">
          <input class="fp-input flex" v-model="form.province" placeholder="省" />
          <input class="fp-input flex" v-model="form.city" placeholder="市" />
          <input class="fp-input flex" v-model="form.district" placeholder="区" />
        </view>
        <input class="fp-input" v-model="form.detail" placeholder="详细地址（门牌号等）" />
        <view class="fp-actions">
          <button class="fp-cancel" @click="showForm=false">取消</button>
          <button class="fp-save" @click="doSave">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page{min-height:100vh;background:#F8F9FA;padding-bottom:80px}
.list{padding:12px 16px}
.addr-card{background:#fff;border-radius:10px;padding:16px;margin-bottom:10px;position:relative}
.addr-card.is-default{border:1px solid #FF6B35}
.contact{display:block;font-size:15px;font-weight:600;color:#222;margin-bottom:4px}
.detail{display:block;font-size:13px;color:#666;line-height:1.5}
.tag{position:absolute;top:8px;right:8px;font-size:10px;color:#FF6B35;border:1px solid #FF6B35;border-radius:3px;padding:1px 6px}
.addr-actions{display:flex;justify-content:flex-end;gap:16px;margin-top:10px;padding-top:8px;border-top:1px solid #f5f5f5}
.act{font-size:12px;color:#999}.act.del{color:#E8702A}
.empty{text-align:center;padding-top:120px}.emp-icon{font-size:48px}.emp-text{display:block;font-size:14px;color:#bbb;margin-top:8px}
.bottom-bar{position:fixed;bottom:0;left:0;right:0;padding:10px 16px;background:#fff}
.add-btn{width:100%;background:#FF6B35;color:#fff;border:none;border-radius:22px;padding:10px;font-size:15px}
.mask{position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,.4);z-index:200;display:flex;align-items:center;justify-content:center}
.form-panel{background:#fff;border-radius:12px;padding:20px;width:320px;margin:0 20px}
.fp-title{display:block;font-size:16px;font-weight:600;color:#222;margin-bottom:16px}
.fp-input{width:100%;background:#F5F6F8;border:none;border-radius:8px;padding:10px 12px;font-size:14px;margin-bottom:10px}
.fp-row{display:flex;gap:6px}
.fp-input.flex{flex:1}
.fp-actions{display:flex;gap:8px;margin-top:10px}
.fp-cancel,.fp-save{flex:1;padding:10px;border-radius:20px;font-size:14px}
.fp-cancel{border:1px solid #ddd;background:#fff;color:#666}
.fp-save{background:#FF6B35;color:#fff;border:none}
</style>
