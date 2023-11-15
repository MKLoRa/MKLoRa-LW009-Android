package com.moko.lw009.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lw009.databinding.ActivityAdvancedSettingBinding;
import com.moko.lw009.utils.ToastUtils;
import com.moko.lw009.utils.Utils;
import com.moko.support.lw009.MokoSupport;
import com.moko.support.lw009.OrderTaskAssembler;
import com.moko.support.lw009.entity.OrderCHAR;
import com.moko.support.lw009.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;

/**
 * @author: jun.liu
 * @date: 2023/11/14 10:21
 * @des:
 */
public class AdvancedSettingActivity extends Lw009BaseActivity {
    private ActivityAdvancedSettingBinding mBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityAdvancedSettingBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getStopDetectionCycle());
        mBind.tvSync.setOnClickListener(v -> {
            showSyncingProgressDialog();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.syncLocalTime());
        });
        mBind.tvLog.setOnClickListener(v -> startActivity(new Intent(this, DeviceLogActivity.class)));
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_PARAMS_NOTIFY) {
                    int header = value[0] & 0xff;
                    int key = value[1] & 0xff;
                    int length = value[2] & 0xff;
                    ParamsKeyEnum keyEnum = ParamsKeyEnum.fromParamKey(key);
                    if (header != 0xA2 || null == keyEnum) return;
                    switch (keyEnum) {
                        case KEY_READ_STOP_DETECTION:
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getStopDetectionDuration());
                            if (length == 1) {
                                int stop = value[3] & 0xff;
                                mBind.etStopDetection.setText(String.valueOf(stop));
                                mBind.etStopDetection.setSelection(mBind.etStopDetection.getText().length());
                            }
                            break;
                        case KEY_READ_DETECTION_DURATION:
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getSelfCalibrationSwitch());
                            if (length == 1) {
                                int duration = value[3] & 0xff;
                                mBind.etDetectionDuration.setText(String.valueOf(duration));
                                mBind.etDetectionDuration.setSelection(mBind.etDetectionDuration.getText().length());
                            }
                            break;

                        case KEY_READ_SELF_CALIBRATION:
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getSelfCalibrationTrigger());
                            if (length == 1) {
                                int enable = value[3] & 0xff;
                                mBind.cbSwitch.setChecked(enable == 1);
                            }
                            break;

                        case KEY_READ_SELF_CALIBRATION_TRIGGER:
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getSelfCalibrationDelay());
                            if (length == 2) {
                                int trigger = MokoUtils.toInt(Arrays.copyOfRange(value, 3, 5));
                                mBind.etTrigger.setText(String.valueOf(trigger));
                                mBind.etTrigger.setSelection(mBind.etTrigger.getText().length());
                            }
                            break;

                        case KEY_READ_SELF_CALIBRATION_DELAY:
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getPackingTimes());
                            if (length == 1) {
                                int delay = value[3] & 0xff;
                                mBind.etDelay.setText(String.valueOf(delay));
                                mBind.etDelay.setSelection(mBind.etDelay.getText().length());
                            }
                            break;

                        case KEY_READ_PACKING_TIMES:
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getTotalWorkingTime());
                            if (length == 4) {
                                int times = MokoUtils.toInt(Arrays.copyOfRange(value, 3, 7));
                                mBind.tvDetectionTimes.setText(String.valueOf(times));
                            }
                            break;

                        case KEY_READ_DEVICE_WORKING_TIME:
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getSnNumber());
                            if (length == 4) {
                                int time = MokoUtils.toInt(Arrays.copyOfRange(value, 3, 7));
                                String timeStr = Utils.toDayHoursMinSec(time);
                                mBind.tvWorkTime.setText(timeStr);
                            }
                            break;

                        case KEY_READ_SN_NUMBER:
                            dismissSyncProgressDialog();
                            if (length == 8) {
                                String sn = new String(Arrays.copyOfRange(value, 3, 11));
                                mBind.tvSn.setText(sn);
                            }
                            break;

                        case KEY_SET_STOP_DETECTION:
                            if (length == 1) {
                                int result = value[3] & 0xff;
                                if (result == 0) {
                                    MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setStopDetectionDuration(duration));
                                } else {
                                    dismissSyncProgressDialog();
                                    ToastUtils.showToast(this, "set up failed");
                                }
                            }
                            break;

                        case KEY_SET_DETECTION_DURATION:
                            if (length == 1) {
                                int result = value[3] & 0xff;
                                if (result == 0) {
                                    MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setSelfCalibrationSwitch(mBind.cbSwitch.isChecked() ? 1 : 0));
                                } else {
                                    dismissSyncProgressDialog();
                                    ToastUtils.showToast(this, "set up failed");
                                }
                            }
                            break;

                        case KEY_SET_SELF_CALIBRATION:
                            if (length == 1) {
                                int result = value[3] & 0xff;
                                if (result == 0) {
                                    MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setSelfCalibrationTrigger(trigger));
                                } else {
                                    dismissSyncProgressDialog();
                                    ToastUtils.showToast(this, "set up failed");
                                }
                            }
                            break;

                        case KEY_SET_SELF_CALIBRATION_TRIGGER:
                            if (length == 1) {
                                int result = value[3] & 0xff;
                                if (result == 0) {
                                    MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setSelfCalibrationDelay(delay));
                                } else {
                                    dismissSyncProgressDialog();
                                    ToastUtils.showToast(this, "set up failed");
                                }
                            }
                            break;

                        case KEY_SET_SELF_CALIBRATION_DELAY:
                        case KEY_SET_LOCAL_TIME:
                            dismissSyncProgressDialog();
                            if (length == 1) {
                                int result = value[3] & 0xff;
                                ToastUtils.showToast(this, result == 0 ? "set up success" : "set up failed");
                            }
                            break;
                    }
                }
            }
        });
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        if (isValid()) {
            showSyncingProgressDialog();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setStopDetectionCycle(cycle));
        } else {
            ToastUtils.showToast(this, "Para error!");
        }
    }

    private int cycle;
    private int duration;
    private int trigger;
    private int delay;

    private boolean isValid() {
        if (TextUtils.isEmpty(mBind.etStopDetection.getText())) return false;
        cycle = Integer.parseInt(mBind.etStopDetection.getText().toString());
        if (cycle < 1 || cycle > 60) return false;
        if (TextUtils.isEmpty(mBind.etDetectionDuration.getText())) return false;
        duration = Integer.parseInt(mBind.etDetectionDuration.getText().toString());
        if (duration < 1 || duration > 60) return false;
        if (TextUtils.isEmpty(mBind.etTrigger.getText())) return false;
        trigger = Integer.parseInt(mBind.etTrigger.getText().toString());
        if (trigger < 60 || trigger < 6000) return false;
        if (TextUtils.isEmpty(mBind.etDelay.getText())) return false;
        delay = Integer.parseInt(mBind.etDelay.getText().toString());
        return (delay >= 30 && delay <= 240);
    }

    public void onBack(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
