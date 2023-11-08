package com.moko.lw009.utils;

import android.os.ParcelUuid;
import android.os.SystemClock;

import com.moko.lw009.entity.AdvInfo;
import com.moko.support.lw009.entity.DeviceInfo;
import com.moko.support.lw009.entity.OrderServices;
import com.moko.support.lw009.service.DeviceInfoParseable;

import java.util.HashMap;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class AdvInfoAnalysisImpl implements DeviceInfoParseable<AdvInfo> {
    private final HashMap<String, AdvInfo> advInfoHashMap;

    public AdvInfoAnalysisImpl() {
        this.advInfoHashMap = new HashMap<>();
    }

    @Override
    public AdvInfo parseDeviceInfo(DeviceInfo deviceInfo) {
        ScanResult result = deviceInfo.scanResult;
        ScanRecord record = result.getScanRecord();
        if (null == record) return null;
        List<ParcelUuid> uuids = record.getServiceUuids();
        if (null == uuids || uuids.isEmpty()) return null;
        String uuid = uuids.get(0).toString();
        if (!OrderServices.SERVICE_CUSTOM.getUuid().toString().equalsIgnoreCase(uuid)) return null;
        AdvInfo advInfo;
        if (advInfoHashMap.containsKey(deviceInfo.mac)) {
            advInfo = advInfoHashMap.get(deviceInfo.mac);
            if (null == advInfo) return null;
            advInfo.name = deviceInfo.name;
            advInfo.rssi = deviceInfo.rssi;
            long currentTime = SystemClock.elapsedRealtime();
            advInfo.intervalTime = currentTime - advInfo.scanTime;
            advInfo.scanTime = currentTime;
            advInfo.connectable = result.isConnectable();
        } else {
            advInfo = new AdvInfo();
            advInfo.name = deviceInfo.name;
            advInfo.mac = deviceInfo.mac;
            advInfo.rssi = deviceInfo.rssi;
            advInfo.scanTime = SystemClock.elapsedRealtime();
            advInfo.connectable = result.isConnectable();
            advInfoHashMap.put(deviceInfo.mac, advInfo);
        }
        return advInfo;
    }
}
