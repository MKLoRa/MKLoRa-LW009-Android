package com.moko.support.lw009.service;

import com.moko.support.lw009.entity.DeviceInfo;

public interface DeviceInfoParseable<T> {
    T parseDeviceInfo(DeviceInfo deviceInfo);
}
