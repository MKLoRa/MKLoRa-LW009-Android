package com.moko.support.lw009.task;

import android.net.Uri;

import androidx.annotation.IntRange;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.lw009.entity.OrderCHAR;
import com.moko.support.lw009.entity.ParamsKeyEnum;

public class ParamsWriteTask extends OrderTask {
    public byte[] data;

    public ParamsWriteTask() {
        super(OrderCHAR.CHAR_PARAMS_WRITE, OrderTask.RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    private byte[] checkCrc(byte[] bytes) {
        int crc = 0x0000;
        for (byte aByte : bytes) {
            crc += aByte & 0xff;
        }
        crc ^= 0x1021;
        return MokoUtils.toByteArray(crc, 2);
    }

    public void replyUnlock(byte[] randomBytes, int decrypt) {
        byte[] bytes = MokoUtils.toByteArray(decrypt, 4);
        byte[] crcBytesData = {randomBytes[0], randomBytes[1], randomBytes[2], randomBytes[3],
                bytes[0], bytes[1], bytes[2], bytes[3]};
        byte[] crcByte = checkCrc(crcBytesData);
        response.responseValue = data = new byte[]{
                (byte) 0xA4,
                (byte) ParamsKeyEnum.KEY_REQUEST_UNLOCK.getParamsKey(),
                0x08,
                randomBytes[0],
                randomBytes[1],
                randomBytes[2],
                randomBytes[3],
                bytes[0],
                bytes[1],
                bytes[2],
                bytes[3],
                crcByte[0],
                crcByte[1],
                (byte) 0xE0
        };
        XLog.i("333333----" + MokoUtils.bytesToHexString(response.responseValue));
    }

    public void replyUnlockState(@IntRange(from = 0, to = 1) int result) {
        byte[] crcBytes = checkCrc(MokoUtils.toByteArray(result, 1));
        response.responseValue = data = new byte[]{
                (byte) 0xA4,
                (byte) ParamsKeyEnum.KEY_REQUEST_UNLOCK_COMPLETE.getParamsKey(),
                0x01,
                (byte) result,
                crcBytes[0],
                crcBytes[1],
                (byte) 0xE0
        };
    }
}
