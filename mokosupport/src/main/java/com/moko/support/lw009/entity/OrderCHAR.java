package com.moko.support.lw009.entity;

import java.io.Serializable;
import java.util.UUID;

public enum OrderCHAR implements Serializable {
    // 180A
    CHAR_MODEL_NUMBER(UUID.fromString("00002A24-0000-1000-8000-00805F9B34FB")),
    CHAR_SERIAL_NUMBER(UUID.fromString("00002A25-0000-1000-8000-00805F9B34FB")),
    CHAR_FIRMWARE_REVISION(UUID.fromString("00002A26-0000-1000-8000-00805F9B34FB")),
    CHAR_HARDWARE_REVISION(UUID.fromString("00002A27-0000-1000-8000-00805F9B34FB")),
    CHAR_SOFTWARE_REVISION(UUID.fromString("00002A28-0000-1000-8000-00805F9B34FB")),
    CHAR_MANUFACTURER_NAME(UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")),

    CHAR_PARAMS_NOTIFY(UUID.fromString("5D602001-B7A6-F695-E3A9-E61E24DCBA0E")),
    CHAR_PARAMS_WRITE(UUID.fromString("5D602002-B7A6-F695-E3A9-E61E24DCBA0E")),
    ;

    private final UUID uuid;

    OrderCHAR(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
