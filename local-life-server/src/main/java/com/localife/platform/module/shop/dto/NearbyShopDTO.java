package com.localife.platform.module.shop.dto;

import lombok.Data;

/**
 * 附近店铺搜索请求
 */
@Data
public class NearbyShopDTO {

    /**
     * 经度
     */
    private Double longitude;
    /**
     * 纬度
     */
    private Double latitude;
    /**
     * 搜索半径(km)，默认5km
     */
    private Double radius;
}
