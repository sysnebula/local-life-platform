Page({
    data: {
        tab: 'all',
        filteredOrders: [],
        orders: [
            {
                id: 1,
                shopName: '🍲 蜀九香火锅（三里屯店）',
                type: 'takeout',
                status: 1,
                statusText: '🔄 商家已接单',
                amount: '291.00',
                items: [{name: '九宫格牛油锅底', number: 1, price: '68'}, {name: '雪花肥牛', number: 2, price: '176'}]
            },
            {
                id: 2,
                shopName: '🍲 蜀九香火锅（三里屯店）',
                type: 'voucher',
                status: 3,
                statusText: '✅ 已使用',
                amount: '9.90',
                items: [{name: '100元代金券（秒杀）', number: 1, price: '9.90'}]
            },
            {
                id: 3,
                shopName: '☕ 星巴克',
                type: 'takeout',
                status: 3,
                statusText: '✅ 已完成',
                amount: '41.00',
                items: [{name: '抹茶星冰乐', number: 1, price: '38'}]
            }
        ]
    },
    onShow() {
        const tab = this.data.tab
        this.setData({filteredOrders: this.data.orders.filter(o => tab === 'all' ? true : tab === 'doing' ? o.status <= 2 : o.status >= 3)})
    },
    filter(e) {
        const tab = e.currentTarget.dataset.tab
        this.setData({
            tab,
            filteredOrders: this.data.orders.filter(o => tab === 'all' ? true : tab === 'doing' ? o.status <= 2 : o.status >= 3)
        })
    },
    cancel(e) {
        const orders = this.data.orders;
        const o = orders.find(x => x.id === e.currentTarget.dataset.id)
        if (o) {
            o.status = 4;
            o.statusText = '❌ 已取消';
            this.setData({
                orders,
                filteredOrders: orders.filter(o => this.data.tab === 'all' ? true : this.data.tab === 'doing' ? o.status <= 2 : o.status >= 3)
            })
        }
        wx.showToast({title: '已取消', icon: 'none'})
    },
    remind() {
        wx.showToast({title: '已催单！', icon: 'none'})
    },
    reorder() {
        wx.showToast({title: '已加入购物车', icon: 'none'})
    }
})
