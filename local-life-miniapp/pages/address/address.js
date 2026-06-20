Page({
    data: {
        showForm: false, editId: null,
        form: {consignee: '', phone: '', detail: '', label: ''},
        addresses: [
            {
                id: 1,
                consignee: '李先生',
                phone: '138****1234',
                province: '北京市',
                city: '朝阳区',
                district: '三里屯',
                detail: '工体北路甲2号 3-1806',
                label: '家',
                isDefault: true
            },
            {
                id: 2,
                consignee: '李先生',
                phone: '138****1234',
                province: '北京市',
                city: '朝阳区',
                district: '望京',
                detail: '望京SOHO T1 22层',
                label: '公司',
                isDefault: false
            }
        ]
    },
    editAddr(e) {
        const id = e.currentTarget.dataset.id
        if (id) {
            const addr = this.data.addresses.find(a => a.id === id)
            this.setData({editId: id, form: {...addr}, showForm: true})
        } else {
            this.setData({editId: null, form: {consignee: '', phone: '', detail: '', label: ''}, showForm: true})
        }
    },
    hideForm() {
        this.setData({showForm: false})
    },
    onField(e) {
        const f = this.data.form;
        f[e.currentTarget.dataset.field] = e.detail.value;
        this.setData({form: f})
    },
    saveAddr() {
        this.setData({showForm: false});
        wx.showToast({title: '保存成功', icon: 'success'})
    },
    setDefault(e) {
        const addrs = this.data.addresses;
        addrs.forEach(a => a.isDefault = false)
        const addr = addrs.find(a => a.id === e.currentTarget.dataset.id);
        if (addr) addr.isDefault = true
        this.setData({addresses: addrs})
    },
    delAddr(e) {
        this.setData({addresses: this.data.addresses.filter(a => a.id !== e.currentTarget.dataset.id)})
        wx.showToast({title: '已删除', icon: 'none'})
    },
    goBack() {
        wx.navigateBack()
    }
})
