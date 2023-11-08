package com.moko.support.lw009.entity;


import java.io.Serializable;

public enum ParamsKeyEnum implements Serializable {
    KEY_REQUEST_UNLOCK(0xF9),
    KEY_REQUEST_UNLOCK_COMPLETE(0xFA),

    ;

    private final int paramsKey;

    ParamsKeyEnum(int paramsKey) {
        this.paramsKey = paramsKey;
    }

    public int getParamsKey() {
        return paramsKey;
    }

    public static ParamsKeyEnum fromParamKey(int paramsKey) {
        for (ParamsKeyEnum paramsKeyEnum : ParamsKeyEnum.values()) {
            if (paramsKeyEnum.getParamsKey() == paramsKey) {
                return paramsKeyEnum;
            }
        }
        return null;
    }
}
