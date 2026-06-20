Page({
    data: {
        total: 0,
        shopItems: [
            {
                shopId: 1, shopName: '🍲 蜀九香火锅（三里屯店）', items: [
                    {dishId: 1, name: '九宫格牛油锅底', price: '68', number: 1, image: '🍲'},
                    {dishId: 3, name: '极品雪花肥牛', price: '88', number: 2, image: '🥩'},
                    {dishId: 4, name: '手工虾滑', price: '42', number: 1, image: '🦐'}]
            },
            {
                shopId: 2,
                shopName: '☕ 星巴克臻选（国贸店）',
                items: [{dishId: 5, name: '抹茶星冰乐', price: '38', number: 1, image: '☕'}]
            }
        ]
    },
    calcTotal() {
        let t = 0
        this.data.shopItems.forEach(s => s.items.forEach(i => t += parseInt(i.price) * i.number))
        this.setData({total: t})
    },
    onShow() {
        this.calcTotal()
    },
    changeQty(e) {
        const {item, delta} = e.currentTarget.dataset
        item.number += delta
        if (item.number <= 0) {
            this.data.shopItems.forEach(s => {
                s.items = s.items.filter(i => i !== item)
            })
            this.setData({shopItems: this.data.shopItems.filter(s => s.items.length > 0)})
        } else this.setData({shopItems: this.data.shopItems})
        this.calcTotal()
    },
    clearCart() {
        this.setData({shopItems: [], total: 0});
        wx.showToast({title: '已清空', icon: 'none'})
    },
    placeOrder() {
        wx.showToast({title: '下单成功！', icon: 'none'})
    }
})
