package com.moko.support.lw009.entity;

import java.util.UUID;

public enum OrderServices {
    SERVICE_DEVICE_INFO(UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB")),
    SERVICE_CUSTOM(UUID.fromString("5d600001-b7a6-f695-e3a9-e61e24dcba0e")),
    ;
    private final UUID uuid;

    OrderServices(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
