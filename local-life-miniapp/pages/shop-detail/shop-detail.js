Page({
    data: {
        tab: 0,
        vouchers: [
            {id: 1, title: '100元代金券', payValue: '9.9', type: 1, rule: '原价¥88·已售1832'},
            {id: 2, title: '双人火锅套餐券', payValue: '88', type: 0, rule: '原价¥198·已售1286'},
            {id: 3, title: '四人聚餐券', payValue: '168', type: 0, rule: '原价¥368·已售623'}
        ],
        menu: [
            {
                name: '🔥 招牌锅底',
                dishes: [{id: 1, name: '九宫格牛油锅底', price: '68', icon: '🍲'}, {
                    id: 2,
                    name: '番茄鸳鸯锅底',
                    price: '58',
                    icon: '🥘'
                }]
            },
            {
                name: '🥩 精品荤菜',
                dishes: [{id: 3, name: '极品雪花肥牛', price: '88', icon: '🥩'}, {
                    id: 4,
                    name: '手工虾滑',
                    price: '42',
                    icon: '🦐'
                }]
            }
        ],
        notes: [
            {
                id: 1,
                userName: '美食达人小李',
                time: '2天前',
                content: '这家店的九宫格锅底真的很正宗！牛油香气浓郁，辣度也恰到好处。'
            },
            {id: 2, userName: '吃货小张', time: '5天前', content: '团了88的双人券来的，性价比很高！推荐番茄鸳鸯锅。'}
        ]
    },
    switchTab(e) {
        this.setData({tab: parseInt(e.currentTarget.dataset.idx)})
    },
    buyVoucher(e) {
        wx.showToast({title: e.currentTarget.dataset.item.type === 1 ? '已进入秒杀！' : '已购买！', icon: 'none'})
    },
    addCart(e) {
        wx.showToast({title: e.currentTarget.dataset.item.name + ' 已加入购物车', icon: 'none'})
    }
})
