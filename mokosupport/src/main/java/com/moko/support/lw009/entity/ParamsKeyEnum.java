package com.moko.support.lw009.entity;


import java.io.Serializable;

public enum ParamsKeyEnum implements Serializable {
    KEY_REQUEST_UNLOCK(0xF9),
    KEY_REQUEST_UNLOCK_COMPLETE(0xFA),
    KEY_FIRMWARE_INFO(0x1E),
    KEY_RAED_DEVICE_WORK_MODE(0x02),
    KEY_SET_DEVICE_WORK_MODE(0x82),
    KEY_READ_REGION(0x0E),
    KEY_READ_HEART_INTERVAL(0x1B),
    KEY_SET_HEART_INTERVAL(0x9B),
    KEY_READ_DETECTION_ALGORITHMS(0x23),
    KEY_SET_DETECTION_ALGORITHMS(0xA3),
    KEY_READ_DETECTION_SENSITIVITY(0x1C),
    KEY_SET_DETECTION_SENSITIVITY(0x9C),
    KEY_READ_DEV_EUI(0x05),
    KEY_SET_DEV_EUI(0x85),
    KEY_READ_APP_EUI(0x06),
    KEY_SET_APP_EUI(0x86),
    KEY_READ_NWK_S_KEY(0x07),
    KEY_SET_NWK_S_KEY(0x87),
    KEY_READ_APP_S_KEY(0x08),
    KEY_SET_APP_S_KEY(0x88),
    KEY_READ_APP_KEY(0x09),
    KEY_SET_APP_KEY(0x89),
    KEY_READ_DEV_ADD_R(0x04),
    KEY_SET_DEV_ADD_R(0x84),
    KEY_REBOOT_DEVICE(0x33),
    KEY_UPLINK_TEST(0x31),
    KEY_READ_LORA_MODE(0x0D),
    KEY_SET_LORA_MODE(0x8D),
    KEY_SET_LOCAL_TIME(0x83),
    KEY_SET_STOP_DETECTION(0xB0),
    KEY_READ_STOP_DETECTION(0x35),
    KEY_SET_DETECTION_DURATION(0xB3),
    KEY_READ_DETECTION_DURATION(0x39),
    KEY_SET_SELF_CALIBRATION(0xB5),
    KEY_READ_SELF_CALIBRATION(0x3A),
    KEY_SET_SELF_CALIBRATION_TRIGGER(0xB6),
    KEY_READ_SELF_CALIBRATION_TRIGGER(0x3B),
    KEY_SET_SELF_CALIBRATION_DELAY(0xB7),
    KEY_READ_SELF_CALIBRATION_DELAY(0x3C),
    KEY_READ_PACKING_TIMES(0x22),
    KEY_READ_DEVICE_WORKING_TIME(0x1F),
    KEY_READ_SN_NUMBER(0x25),
    KEY_SET_LOG_ENABLE(0xB1),
    KEY_READ_DEVICE_LOG(0x20),
    KEY_RESET(0x9A),

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
