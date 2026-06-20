const api = require('../../utils/api')

Page({
    data: {
        categories: ['美食', '饮品', '娱乐', '酒店', '全部'],
        icons: {'美食': '🍜', '饮品': '🧋', '娱乐': '🎮', '酒店': '🏨', '全部': '⋯'},
        seckills: [
            {
                id: 1,
                name: '蜀九香火锅',
                price: '¥9.9',
                desc: '100元代金券',
                color: 'linear-gradient(135deg,#FF6B35,#F7931E)'
            },
            {
                id: 2,
                name: '星巴克',
                price: '¥19.9',
                desc: '任意饮品券',
                color: 'linear-gradient(135deg,#E91E63,#9C27B0)'
            },
            {id: 3, name: '肯德基', price: '¥29.9', desc: '全家桶券', color: 'linear-gradient(135deg,#4CAF50,#009688)'}
        ],
        shops: [
            {id: 1, name: '蜀九香火锅（三里屯店）', addr: '工体北路甲2号', tags: ['🔥人气', '团购券'], icon: '🍲'},
            {id: 2, name: '星巴克臻选（国贸店）', addr: '建国门外大街1号', tags: ['新店', '秒杀中'], icon: '☕'},
            {id: 3, name: '肯德基（望京店）', addr: '望京街9号', tags: ['外卖', '满减'], icon: '🍗'}
        ]
    },
    onTapCat(e) {
        wx.showToast({title: '筛选: ' + e.currentTarget.dataset.name, icon: 'none'})
    },
    goShop(e) {
        wx.navigateTo({url: '/pages/shop-detail/shop-detail?id=' + e.currentTarget.dataset.id})
    }
})
