package com.moko.support.lw009;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.lw009.entity.ParamsKeyEnum;
import com.moko.support.lw009.task.ParamsWriteTask;

import java.lang.annotation.Target;

public class OrderTaskAssembler {
    public static OrderTask getFirmwareVersion() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_FIRMWARE_INFO.getParamsKey());
        return task;
    }

    public static OrderTask getWorkMode() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_RAED_DEVICE_WORK_MODE.getParamsKey());
        return task;
    }

    public static OrderTask getRegion() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_READ_REGION.getParamsKey());
        return task;
    }

    public static OrderTask getHeartInterval() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_READ_HEART_INTERVAL.getParamsKey());
        return task;
    }

    public static OrderTask getDetectionAlgorithms() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_READ_DETECTION_ALGORITHMS.getParamsKey());
        return task;
    }

    public static OrderTask getDetectionSensitivity() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_READ_DETECTION_SENSITIVITY.getParamsKey());
        return task;
    }

    public static OrderTask getDevEui() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_READ_DEV_EUI.getParamsKey());
        return task;
    }

    public static OrderTask replyUnlock(byte[] randomBytes, int decrypt) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.replyUnlock(randomBytes, decrypt);
        return task;
    }

    public static OrderTask replyUnlockState(@IntRange(from = 0, to = 1) int result) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.replyUnlockState(result);
        return task;
    }

    public static OrderTask reboot() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_REBOOT_DEVICE.getParamsKey());
        return task;
    }

    public static OrderTask uplinkTest() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_UPLINK_TEST.getParamsKey());
        return task;
    }

    public static OrderTask getLoraMode() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_READ_LORA_MODE.getParamsKey());
        return task;
    }

    public static OrderTask getDevAddR() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_READ_DEV_ADD_R.getParamsKey());
        return task;
    }

    public static OrderTask getAppEui() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_READ_APP_EUI.getParamsKey());
        return task;
    }

    public static OrderTask getNwkSKey() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_READ_NWK_S_KEY.getParamsKey());
        return task;
    }

    public static OrderTask getAppSKey() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_READ_APP_S_KEY.getParamsKey());
        return task;
    }

    public static OrderTask getAppKey() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_READ_APP_KEY.getParamsKey());
        return task;
    }

    public static OrderTask setDevAddR(@NonNull String addR) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setDevAddR(addR);
        return task;
    }

    public static OrderTask setDevEui(@NonNull String devEui) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setDevEui(devEui);
        return task;
    }

    public static OrderTask setAppEui(@NonNull String appEui) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAppEui(appEui);
        return task;
    }

    public static OrderTask setNwkSKey(@NonNull String nwkSKey) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setNwkSKey(nwkSKey);
        return task;
    }

    public static OrderTask setAppSKey(@NonNull String appSKey) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAppSKey(appSKey);
        return task;
    }

    public static OrderTask setAppKey(@NonNull String appKey) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAppKey(appKey);
        return task;
    }

    public static OrderTask setLoraMode(int mode) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setLoraMode(mode);
        return task;
    }

    public static OrderTask syncLocalTime() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.syncLocalTime();
        return task;
    }

    public static OrderTask setStopDetectionCycle(@IntRange(from = 1, to = 60) int cycle) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setStopDetectionCycle(cycle);
        return task;
    }

    public static OrderTask getStopDetectionCycle() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_READ_STOP_DETECTION.getParamsKey());
        return task;
    }

    public static OrderTask setStopDetectionDuration(@IntRange(from = 1, to = 60) int duration) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setStopDetectionDuration(duration);
        return task;
    }

    public static OrderTask getStopDetectionDuration() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_READ_DETECTION_DURATION.getParamsKey());
        return task;
    }

    public static OrderTask setSelfCalibrationSwitch(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setSelfCalibrationSwitch(enable);
        return task;
    }

    public static OrderTask getSelfCalibrationSwitch() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_READ_SELF_CALIBRATION.getParamsKey());
        return task;
    }

    public static OrderTask setSelfCalibrationTrigger(@IntRange(from = 60, to = 6000) int trigger) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setSelfCalibrationTrigger(trigger);
        return task;
    }

    public static OrderTask getSelfCalibrationTrigger() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_READ_SELF_CALIBRATION_TRIGGER.getParamsKey());
        return task;
    }

    public static OrderTask setSelfCalibrationDelay(@IntRange(from = 30, to = 240) int delay) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setSelfCalibrationDelay(delay);
        return task;
    }

    public static OrderTask getSelfCalibrationDelay() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_READ_SELF_CALIBRATION_DELAY.getParamsKey());
        return task;
    }

    public static OrderTask getPackingTimes() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_READ_PACKING_TIMES.getParamsKey());
        return task;
    }

    public static OrderTask getTotalWorkingTime() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_READ_DEVICE_WORKING_TIME.getParamsKey());
        return task;
    }

    public static OrderTask getSnNumber() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.getData(ParamsKeyEnum.KEY_READ_SN_NUMBER.getParamsKey());
        return task;
    }

    public static OrderTask setLogSwitch(@IntRange(from = 0,to = 1) int enable){
        ParamsWriteTask task = new ParamsWriteTask();
        task.setLogSwitch(enable);
        return task;
    }

    public static OrderTask setWorkMode(int mode) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setWorkMode(mode);
        return task;
    }

    public static OrderTask setHeartInterval(@IntRange(from = 1, to = 2880) int interval) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setHeartInterval(interval);
        return task;
    }

    public static OrderTask setDetectionAlgorithms(int mode) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setDetectionAlgorithms(mode);
        return task;
    }

    public static OrderTask setDetectionSensitivity(int mode) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setDetectionSensitivity(mode);
        return task;
    }

    public static OrderTask readLastLog(){
        ParamsWriteTask task = new ParamsWriteTask();
        task.readLastLog();
        return task;
    }
}
