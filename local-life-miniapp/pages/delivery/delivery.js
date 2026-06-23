Page({
  data: {
    shops: [
      { id:1, icon:'🍲', name:'蜀九香火锅', area:'三里屯', isBrand:true, score:'4.8', monthSold:'3200', minOrder:'¥20', deliveryFee:'¥3', time:'30分钟', activities:['满30减5','新客立减8'] },
      { id:2, icon:'🍗', name:'肯德基', area:'望京', isNew:true, score:'4.5', monthSold:'8900', minOrder:'¥15', deliveryFee:'¥2', time:'25分钟', activities:['满25减4'] },
      { id:3, icon:'☕', name:'星巴克臻选', area:'国贸', isBrand:true, score:'4.6', monthSold:'5100', minOrder:'¥25', deliveryFee:'¥3', time:'20分钟', activities:['新客减6元'] },
      { id:4, icon:'🦆', name:'大董烤鸭', area:'工体', score:'4.7', monthSold:'1680', minOrder:'¥50', deliveryFee:'¥5', time:'40分钟', activities:['满100减15'] }
    ]
  },
  goShop(e) {
    wx.navigateTo({ url: '/pages/shop-detail/shop-detail?id=' + e.currentTarget.dataset.id })
  }
})
