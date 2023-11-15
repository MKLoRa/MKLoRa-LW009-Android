package com.moko.support.lw009.task;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.lw009.entity.OrderCHAR;
import com.moko.support.lw009.entity.ParamsKeyEnum;

import java.util.Calendar;

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

    public void getData(int key) {
        response.responseValue = data = new byte[]{
                (byte) 0xA1,
                (byte) key,
                (byte) 0x00,
                (byte) 0x10,
                (byte) 0x21,
                (byte) 0xE0
        };
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

    public void setDevAddR(@NonNull String addR) {
        byte[] bytes = MokoUtils.hex2bytes(addR);
        byte[] crcBytes = checkCrc(bytes);
        int length = bytes.length;
        data = new byte[length + 6];
        data[0] = (byte) 0xA1;
        data[1] = (byte) ParamsKeyEnum.KEY_SET_DEV_ADD_R.getParamsKey();
        data[2] = (byte) length;
        for (int i = 0; i < length; i++) {
            data[3 + i] = bytes[i];
        }
        data[3 + length] = crcBytes[0];
        data[4 + length] = crcBytes[1];
        data[5 + length] = (byte) 0xE0;
        response.responseValue = data;
    }

    public void setDevEui(@NonNull String devEui) {
        byte[] bytes = MokoUtils.hex2bytes(devEui);
        byte[] crcBytes = checkCrc(bytes);
        int length = bytes.length;
        data = new byte[length + 6];
        data[0] = (byte) 0xA1;
        data[1] = (byte) ParamsKeyEnum.KEY_SET_DEV_EUI.getParamsKey();
        data[2] = (byte) length;
        for (int i = 0; i < length; i++) {
            data[3 + i] = bytes[i];
        }
        data[3 + length] = crcBytes[0];
        data[4 + length] = crcBytes[1];
        data[5 + length] = (byte) 0xE0;
        response.responseValue = data;
    }

    public void setAppEui(@NonNull String appEui) {
        byte[] bytes = MokoUtils.hex2bytes(appEui);
        byte[] crcBytes = checkCrc(bytes);
        int length = bytes.length;
        data = new byte[length + 6];
        data[0] = (byte) 0xA1;
        data[1] = (byte) ParamsKeyEnum.KEY_SET_APP_EUI.getParamsKey();
        data[2] = (byte) length;
        for (int i = 0; i < length; i++) {
            data[3 + i] = bytes[i];
        }
        data[3 + length] = crcBytes[0];
        data[4 + length] = crcBytes[1];
        data[5 + length] = (byte) 0xE0;
        response.responseValue = data;
    }

    public void setNwkSKey(@NonNull String nwkSKey) {
        byte[] bytes = MokoUtils.hex2bytes(nwkSKey);
        byte[] crcBytes = checkCrc(bytes);
        int length = bytes.length;
        data = new byte[length + 6];
        data[0] = (byte) 0xA1;
        data[1] = (byte) ParamsKeyEnum.KEY_SET_NWK_S_KEY.getParamsKey();
        data[2] = (byte) length;
        for (int i = 0; i < length; i++) {
            data[3 + i] = bytes[i];
        }
        data[3 + length] = crcBytes[0];
        data[4 + length] = crcBytes[1];
        data[5 + length] = (byte) 0xE0;
        response.responseValue = data;
    }

    public void setAppSKey(@NonNull String appSKey) {
        byte[] bytes = MokoUtils.hex2bytes(appSKey);
        byte[] crcBytes = checkCrc(bytes);
        int length = bytes.length;
        data = new byte[length + 6];
        data[0] = (byte) 0xA1;
        data[1] = (byte) ParamsKeyEnum.KEY_SET_APP_S_KEY.getParamsKey();
        data[2] = (byte) length;
        for (int i = 0; i < length; i++) {
            data[3 + i] = bytes[i];
        }
        data[3 + length] = crcBytes[0];
        data[4 + length] = crcBytes[1];
        data[5 + length] = (byte) 0xE0;
        response.responseValue = data;
    }

    public void setAppKey(@NonNull String appKey) {
        byte[] bytes = MokoUtils.hex2bytes(appKey);
        byte[] crcBytes = checkCrc(bytes);
        int length = bytes.length;
        data = new byte[length + 6];
        data[0] = (byte) 0xA1;
        data[1] = (byte) ParamsKeyEnum.KEY_SET_APP_KEY.getParamsKey();
        data[2] = (byte) length;
        for (int i = 0; i < length; i++) {
            data[3 + i] = bytes[i];
        }
        data[3 + length] = crcBytes[0];
        data[4 + length] = crcBytes[1];
        data[5 + length] = (byte) 0xE0;
        response.responseValue = data;
    }

    public void setLoraMode(int mode) {
        byte[] crcBytes = checkCrc(new byte[]{(byte) mode});
        response.responseValue = data = new byte[]{
                (byte) 0xA1,
                (byte) ParamsKeyEnum.KEY_SET_LORA_MODE.getParamsKey(),
                (byte) 0x01,
                (byte) mode,
                crcBytes[0],
                crcBytes[1],
                (byte) 0xE0
        };
    }

    public void syncLocalTime() {
        Calendar calendar = Calendar.getInstance();
        long time = calendar.getTimeInMillis() / 1000;
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; ++i) {
            bytes[i] = (byte) (time >> 8 * (3 - i) & 255);
        }
        byte[] crcBytes = checkCrc(bytes);
        response.responseValue = data = new byte[]{
                (byte) 0xA1,
                (byte) ParamsKeyEnum.KEY_SET_LOCAL_TIME.getParamsKey(),
                (byte) 0x04,
                bytes[0],
                bytes[1],
                bytes[2],
                bytes[3],
                crcBytes[0],
                crcBytes[1],
                (byte) 0xE0
        };
    }

    public void setStopDetectionCycle(@IntRange(from = 1, to = 60) int cycle) {
        byte[] crcBytes = checkCrc(new byte[]{(byte) cycle});
        response.responseValue = data = new byte[]{
                (byte) 0xA1,
                (byte) ParamsKeyEnum.KEY_SET_STOP_DETECTION.getParamsKey(),
                (byte) 0x01,
                (byte) cycle,
                crcBytes[0],
                crcBytes[1],
                (byte) 0xE0
        };
    }

    public void setStopDetectionDuration(@IntRange(from = 1, to = 60) int duration) {
        byte[] crcBytes = checkCrc(new byte[]{(byte) duration});
        response.responseValue = data = new byte[]{
                (byte) 0xA1,
                (byte) ParamsKeyEnum.KEY_SET_DETECTION_DURATION.getParamsKey(),
                (byte) 0x01,
                (byte) duration,
                crcBytes[0],
                crcBytes[1],
                (byte) 0xE0
        };
    }

    public void setSelfCalibrationSwitch(@IntRange(from = 0, to = 1) int enable) {
        byte[] crcBytes = checkCrc(new byte[]{(byte) enable});
        response.responseValue = data = new byte[]{
                (byte) 0xA1,
                (byte) ParamsKeyEnum.KEY_SET_SELF_CALIBRATION.getParamsKey(),
                (byte) 0x01,
                (byte) enable,
                crcBytes[0],
                crcBytes[1],
                (byte) 0xE0
        };
    }

    public void setSelfCalibrationTrigger(@IntRange(from = 60, to = 6000) int trigger) {
        byte[] bytes = MokoUtils.toByteArray(trigger, 2);
        byte[] crcBytes = checkCrc(bytes);
        response.responseValue = data = new byte[]{
                (byte) 0xA1,
                (byte) ParamsKeyEnum.KEY_SET_SELF_CALIBRATION_TRIGGER.getParamsKey(),
                (byte) 0x02,
                bytes[0],
                bytes[1],
                crcBytes[0],
                crcBytes[1],
                (byte) 0xE0
        };
    }

    public void setSelfCalibrationDelay(@IntRange(from = 30, to = 240) int delay) {
        byte[] crcBytes = checkCrc(new byte[]{(byte) delay});
        response.responseValue = data = new byte[]{
                (byte) 0xA1,
                (byte) ParamsKeyEnum.KEY_SET_SELF_CALIBRATION_DELAY.getParamsKey(),
                (byte) 0x01,
                (byte) delay,
                crcBytes[0],
                crcBytes[1],
                (byte) 0xE0
        };
    }

    public void setLogSwitch(@IntRange(from = 0, to = 1) int enable) {
        byte[] crcBytes = checkCrc(new byte[]{(byte) enable});
        response.responseValue = data = new byte[]{
                (byte) 0xA1,
                (byte) ParamsKeyEnum.KEY_SET_LOG_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable,
                crcBytes[0],
                crcBytes[1],
                (byte) 0xE0
        };
    }

    public void setWorkMode(int mode) {
        byte[] crcBytes = checkCrc(new byte[]{(byte) mode});
        response.responseValue = data = new byte[]{
                (byte) 0xA1,
                (byte) ParamsKeyEnum.KEY_SET_DEVICE_WORK_MODE.getParamsKey(),
                (byte) 0x01,
                (byte) mode,
                crcBytes[0],
                crcBytes[1],
                (byte) 0xE0
        };
    }

    public void setHeartInterval(@IntRange(from = 1, to = 2880) int interval) {
        byte[] bytes = MokoUtils.toByteArray(interval, 2);
        byte[] crcBytes = checkCrc(bytes);
        response.responseValue = data = new byte[]{
                (byte) 0xA1,
                (byte) ParamsKeyEnum.KEY_SET_HEART_INTERVAL.getParamsKey(),
                (byte) 0x02,
                bytes[0],
                bytes[1],
                crcBytes[0],
                crcBytes[1],
                (byte) 0xE0
        };
    }

    public void setDetectionAlgorithms(int mode) {
        byte[] crcBytes = checkCrc(new byte[]{(byte) mode});
        response.responseValue = data = new byte[]{
                (byte) 0xA1,
                (byte) ParamsKeyEnum.KEY_SET_DETECTION_ALGORITHMS.getParamsKey(),
                (byte) 0x01,
                (byte) mode,
                crcBytes[0],
                crcBytes[1],
                (byte) 0xE0
        };
    }

    public void setDetectionSensitivity(int mode) {
        byte[] crcBytes = checkCrc(new byte[]{(byte) mode});
        response.responseValue = data = new byte[]{
                (byte) 0xA1,
                (byte) ParamsKeyEnum.KEY_SET_DETECTION_SENSITIVITY.getParamsKey(),
                (byte) 0x01,
                (byte) mode,
                crcBytes[0],
                crcBytes[1],
                (byte) 0xE0
        };
    }

    public void readLastLog(){
        byte[] crcBytes = checkCrc(new byte[]{(byte) 1});
        response.responseValue = data = new byte[]{
                (byte) 0xA1,
                (byte) ParamsKeyEnum.KEY_READ_DEVICE_LOG.getParamsKey(),
                (byte) 0x01,
                (byte) 0x01,
                crcBytes[0],
                crcBytes[1],
                (byte) 0xE0
        };
    }

}
