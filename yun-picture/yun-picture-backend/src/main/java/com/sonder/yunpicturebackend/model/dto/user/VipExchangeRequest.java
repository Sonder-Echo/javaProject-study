package com.sonder.yunpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 会员兑换请求类
 */
@Data
public class VipExchangeRequest implements Serializable {
    private static final long serialVersionUID = 984165981616651316L;

    /**
     * 兑换码
     */
    private String vipCode;
}
