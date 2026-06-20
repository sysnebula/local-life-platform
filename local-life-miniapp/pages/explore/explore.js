Page({
    data: {
        showPublish: false,
        notes: [
            {
                id: 1,
                userName: '美食达人小李',
                shopName: '蜀九香火锅',
                time: '2天前',
                title: '🔥 京城必打卡的重庆火锅！',
                content: '这家店的九宫格锅底真的很正宗！牛油香气浓郁，辣度也恰到好处。',
                color: '#FF6B35'
            },
            {
                id: 2,
                userName: '咖啡爱好者小王',
                shopName: '星巴克臻选',
                time: '3天前',
                title: '☕ 新开臻选店，环境绝了',
                content: '周末来国贸逛街发现的，装修很有格调。窗边的位置可以看到CBD天际线~',
                color: '#E91E63'
            }
        ]
    },
    showPublish() {
        this.setData({showPublish: true})
    },
    hidePublish() {
        this.setData({showPublish: false})
    },
    publish() {
        this.setData({showPublish: false});
        wx.showToast({title: '发布成功！', icon: 'none'})
    }
})
