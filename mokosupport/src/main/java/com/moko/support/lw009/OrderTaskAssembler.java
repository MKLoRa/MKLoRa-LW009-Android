package com.moko.support.lw009;

import androidx.annotation.IntRange;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.lw009.task.GetFirmwareRevisionTask;
import com.moko.support.lw009.task.GetHardwareRevisionTask;
import com.moko.support.lw009.task.GetManufacturerNameTask;
import com.moko.support.lw009.task.GetModelNumberTask;
import com.moko.support.lw009.task.GetSerialNumberTask;
import com.moko.support.lw009.task.GetSoftwareRevisionTask;
import com.moko.support.lw009.task.ParamsWriteTask;

public class OrderTaskAssembler {
    ///////////////////////////////////////////////////////////////////////////
    // READ
    ///////////////////////////////////////////////////////////////////////////

    public static OrderTask getManufacturer() {
        GetManufacturerNameTask getManufacturerTask = new GetManufacturerNameTask();
        return getManufacturerTask;
    }

    public static OrderTask getDeviceModel() {
        GetModelNumberTask getDeviceModelTask = new GetModelNumberTask();
        return getDeviceModelTask;
    }

    public static OrderTask getSerialNumber() {
        GetSerialNumberTask getSerialNumberTask = new GetSerialNumberTask();
        return getSerialNumberTask;
    }

    public static OrderTask getHardwareVersion() {
        GetHardwareRevisionTask getHardwareVersionTask = new GetHardwareRevisionTask();
        return getHardwareVersionTask;
    }

    public static OrderTask getFirmwareVersion() {
        GetFirmwareRevisionTask getFirmwareVersionTask = new GetFirmwareRevisionTask();
        return getFirmwareVersionTask;
    }

    public static OrderTask getSoftwareVersion() {
        GetSoftwareRevisionTask getSoftwareVersionTask = new GetSoftwareRevisionTask();
        return getSoftwareVersionTask;
    }

    public static OrderTask replyUnlock(byte[] randomBytes, int decrypt) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.replyUnlock(randomBytes, decrypt);
        return task;
    }

    public static OrderTask  replyUnlockState(@IntRange(from = 0,to = 1) int result) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.replyUnlockState(result);
        return task;
    }
}
