Page({
  data: {
    seckills: [
      { id:1, name:'蜀九香火锅', title:'100元代金券', price:'9.9', orig:'100', sold:'1832', stock:'168' },
      { id:2, name:'星巴克', title:'任意饮品券', price:'19.9', orig:'38', sold:'3520', stock:'480' },
      { id:3, name:'肯德基', title:'藤椒鸡腿堡券', price:'9.9', orig:'19', sold:'856', stock:'144' }
    ],
    shops: [
      { id:1, icon:'🍲', name:'蜀九香火锅', area:'三里屯', isHot:true, canBook:true, score:'4.8', avgPrice:'¥128', distance:'1.2km', meal:'双人火锅套餐 ¥88', mealSold:'1200' },
      { id:3, icon:'☕', name:'星巴克臻选', area:'国贸', isHot:true, score:'4.6', avgPrice:'¥45', distance:'0.8km', meal:'任意饮品券 ¥19.9', mealSold:'3500' },
      { id:4, icon:'🦆', name:'大董烤鸭', area:'工体', canBook:true, score:'4.7', avgPrice:'¥198', distance:'2.1km', meal:'精品双人餐 ¥298', mealSold:'680' }
    ]
  },
  goShop(e) {
    wx.navigateTo({ url: '/pages/shop-detail/shop-detail?id=' + e.currentTarget.dataset.id })
  },
  goSeckill(e) {
    wx.navigateTo({ url: '/pages/shop-detail/shop-detail?id=' + e.currentTarget.dataset.shopid })
  }
})
