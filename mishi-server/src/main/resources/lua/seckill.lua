-- 秒杀库存扣减 Lua 脚本
-- KEYS[1] = voucher:stock:{voucherId}
-- 返回: 1=成功, 0=库存不足
local stock = redis.call('GET', KEYS[1])
if stock and tonumber(stock) > 0 then
    redis.call('DECR', KEYS[1])
    return 1
end
return 0
