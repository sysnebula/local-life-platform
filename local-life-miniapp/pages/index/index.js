const api = require('../../utils/api')

Page({
  data: {
    seckills: [
      { id:1, shopName:'蜀九香火锅', title:'100元代金券', price:'9.9', orig:'100', sold:'1832', stock:'168' },
      { id:2, shopName:'蜀九香火锅', title:'双人火锅套餐券', price:'88', orig:'198', sold:'1286', stock:'214' },
      { id:3, shopName:'星巴克', title:'任意饮品券', price:'19.9', orig:'38', sold:'3520', stock:'480' },
      { id:4, shopName:'肯德基', title:'藤椒鸡腿堡券', price:'9.9', orig:'19', sold:'856', stock:'144' }
    ],
    notes: [
      { id:1, avatar:'李', userName:'美食猎人', shopName:'蜀九香火锅（三里屯店）', time:'2天前', title:'🔥 京城必打卡的重庆火锅！', content:'这家店的九宫格锅底真的太正宗了！牛油香气浓郁，辣度也恰到好处。必点雪花肥牛和虾滑，食材新鲜分量足...', images:[] },
      { id:2, avatar:'张', userName:'吃货小张', shopName:'蜀九香火锅（三里屯店）', time:'5天前', title:'双人套餐超划算', content:'团了88的双人券来的，性价比很高！推荐番茄鸳鸯锅，番茄汤底可以直接喝...', images:[] },
      { id:3, avatar:'王', userName:'咖啡控小王', shopName:'星巴克臻选（国贸店）', time:'3天前', title:'☕ 国贸新开的臻选店，环境绝了', content:'周末来国贸逛街发现的，这家臻选店装修很有格调。窗边的位置可以看到CBD天际线~', images:[] }
    ]
  },

  goSeckill(e) {
    wx.showToast({ title: '⚡ 进入秒杀', icon: 'none' })
  },

  goAllSeckill() {
    wx.showToast({ title: '查看全部秒杀券', icon: 'none' })
  },

  goExplore() {
    wx.navigateTo({ url: '/pages/explore/explore' })
  },

  goWriteNote() {
    if (!wx.getStorageSync('token')) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      wx.switchTab({ url: '/pages/profile/profile' })
      return
    }
    wx.navigateTo({ url: '/pages/explore/explore' })
  },

  goNote(e) {
    const id = e.currentTarget.dataset.id
    wx.showToast({ title: '查看笔记详情', icon: 'none' })
  }
})
