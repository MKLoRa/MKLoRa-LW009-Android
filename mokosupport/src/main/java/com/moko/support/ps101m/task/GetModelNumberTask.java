package com.moko.support.ps101m.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.ps101m.entity.OrderCHAR;

public class GetModelNumberTask extends OrderTask {

    public byte[] data;

    public GetModelNumberTask() {
        super(OrderCHAR.CHAR_MODEL_NUMBER, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
