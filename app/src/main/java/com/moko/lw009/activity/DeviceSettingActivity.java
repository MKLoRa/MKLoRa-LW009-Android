package com.moko.lw009.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lw009.databinding.ActivityDeviceSettingBinding;
import com.moko.lw009.dialog.AlertMessageDialog;
import com.moko.lw009.dialog.BottomDialog;
import com.moko.lw009.dialog.PasswordDialog;
import com.moko.lw009.utils.ToastUtils;
import com.moko.support.lw009.MokoSupport;
import com.moko.support.lw009.OrderTaskAssembler;
import com.moko.support.lw009.entity.OrderCHAR;
import com.moko.support.lw009.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author: jun.liu
 * @date: 2023/11/8 19:34
 * @des:
 */
public class DeviceSettingActivity extends Lw009BaseActivity {
    private ActivityDeviceSettingBinding mBind;
    private int workMode;
    private final String[] mValues = {"Sleep Mode", "Normal Mode"};
    private int selectedDetectionAlgorithms;
    private final String[] valueAlgorithms = {"Magnetic only", "Radar only", "Joint Detection"};
    private final String[] valueSensitivity = {"1", "2", "3", "4", "5", "6", "7"};
    private int selectedSensitivity;
    private String regionStr;
    private int loraMode;
    private boolean onAutoInstall;
    private Handler handler;
    private boolean hasReboot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityDeviceSettingBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        handler = new Handler(Looper.getMainLooper());
        showSyncingProgressDialog();
        timeout();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getFirmwareVersion());
        mBind.tvModeSelect.setOnClickListener(v -> onDeviceModeClick());
        mBind.tvDetectionAlog.setOnClickListener(v -> onDetectionAlgorithmsClick());
        mBind.tvDetectionSensitivity.setOnClickListener(v -> onDetectionSensitivityClick());
        mBind.tvReboot.setOnClickListener(v -> reboot());
        mBind.tvTest.setOnClickListener(v -> uplinkTest());
        mBind.layoutRegion.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoraSettingsActivity.class);
            intent.putExtra("region", regionStr);
            intent.putExtra("loraMode", loraMode);
            launcher.launch(intent);
        });
        mBind.layoutAdvanceSet.setOnClickListener(v -> onAdvancedClick());
        mBind.viewDisable.setOnClickListener(v -> {
        });
        mBind.tvAutoInstall.setOnClickListener(v -> onAutoInstallClick());
        mBind.tvReset.setOnClickListener(v -> reset());
    }

    private void onAdvancedClick() {
        if (isTriggerValid()) showPassword();
    }

    private void showPassword() {
        final PasswordDialog dialog = new PasswordDialog(this);
        dialog.setOnPasswordClicked(new PasswordDialog.PasswordClickListener() {
            @Override
            public void onEnsureClicked(String password) {
                if (!"MOKOLORA".equals(password)) {
                    ToastUtils.showToast(DeviceSettingActivity.this, "Password Error");
                } else {
                    //密码校验成功了
                    startActivity(new Intent(DeviceSettingActivity.this, AdvancedSettingActivity.class));
                }
            }

            @Override
            public void onDismiss() {
            }
        });
        dialog.show();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(dialog::showKeyboard);
            }
        }, 200);
    }

    private void reset() {
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Factory Reset");
        dialog.setMessage("After factory reset,all the data will be reseted to the factory values.");
        dialog.setCancel("Cancel");
        dialog.setConfirm("OK");
        dialog.setOnAlertConfirmListener(() -> {
            showSyncingProgressDialog();
            timeout();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.reset());
        });
        dialog.show(getSupportFragmentManager());
    }

    private void timeout() {
        handler.postDelayed(() -> {
            dismissSyncProgressDialog();
            ToastUtils.showToast(this, "time out");
        }, 3000);
    }

    private void onAutoInstallClick() {
        showSyncingProgressDialog();
        onAutoInstall = true;
        timeout();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setWorkMode(1));
    }

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && null != result.getData()) {
            Intent intent = result.getData();
            boolean hasSaved = intent.getBooleanExtra("hasSaved", false);
            if (hasSaved) {
                loraMode = intent.getIntExtra("mode", -1);
                String devEui = intent.getStringExtra("devEui");
                String mode = loraMode == 0 ? "ABP" : "OTAA";
                mBind.tvRegion.setText(mode + "/" + regionStr + "/ClassA");
                mBind.tvDevEui.setText(devEui);
            }
        }
    });

    private void uplinkTest() {
        if (isWindowLocked()) return;
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.uplinkTest());
    }

    private void reboot() {
        if (isWindowLocked()) return;
        showSyncingProgressDialog();
        timeout();
        hasReboot = true;
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.reboot());
    }

    private void onDetectionSensitivityClick() {
        if (isWindowLocked()) return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(valueSensitivity)), selectedSensitivity);
        dialog.setListener(value -> {
            selectedSensitivity = value;
            mBind.tvDetectionSensitivity.setText(valueSensitivity[value]);
        });
        dialog.show(getSupportFragmentManager());
    }

    private void onDetectionAlgorithmsClick() {
        if (isWindowLocked()) return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(valueAlgorithms)), selectedDetectionAlgorithms);
        dialog.setListener(value -> {
            selectedDetectionAlgorithms = value;
            mBind.tvDetectionAlog.setText(valueAlgorithms[value]);
        });
        dialog.show(getSupportFragmentManager());
    }

    private void onDeviceModeClick() {
        if (isWindowLocked()) return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(mValues)), workMode);
        dialog.setListener(value -> {
            workMode = value;
            mBind.tvModeSelect.setText(mValues[value]);
        });
        dialog.show(getSupportFragmentManager());
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                showDisconnectDialog();
            }
        });
    }

    private int getIndex(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == 0) {
                return i;
            }
        }
        return -1;
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
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
                        case KEY_FIRMWARE_INFO:
                            handler.removeMessages(0);
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getWorkMode());
                            timeout();
                            if (length == 128) {
                                //固件信息
                                byte[] bytes = Arrays.copyOfRange(value, 3, 19);
                                int index = getIndex(bytes);
                                if (index == -1) index = bytes.length;
                                String mode = new String(Arrays.copyOfRange(bytes, 0, index));
                                if ("TBS-201".equals(mode)) {
                                    mBind.tvTitle.setText("LW009-IG");
                                } else if ("TBS-223".equals(mode)) {
                                    mBind.tvTitle.setText("LW009-SM");
                                }
                                String softwareVersion = MokoUtils.byte2HexString(value[0x51 + 3]);
                                String hardwareVersion = MokoUtils.byte2HexString(value[0x52 + 3]);
                                mBind.tvSoftwareVersion.setText(softwareVersion);
                                mBind.tvHardwareVersion.setText(hardwareVersion);
                            }
                            break;

                        case KEY_RAED_DEVICE_WORK_MODE:
                            handler.removeMessages(0);
                            if (!hasReboot) {
                                timeout();
                                MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getRegion());
                                if (length == 1) {
                                    workMode = value[3] & 0xff;
                                    mBind.tvModeSelect.setText(mValues[workMode]);
                                    mBind.tvMode.setText(mValues[workMode]);
                                    mBind.viewDisable.setVisibility(workMode == 0 ? View.VISIBLE : View.GONE);
                                }
                            } else {
                                dismissSyncProgressDialog();
                                hasReboot = false;
                                int result = value[3] & 0xff;
                                AlertMessageDialog dialog = new AlertMessageDialog();
                                dialog.setConfirm("OK");
                                if (result == 1) {
                                    dialog.setCancelGone();
                                } else {
                                    dialog.setCancel("Try again");
                                    dialog.setOnAlertCancelListener(this::reboot);
                                }
                                String msg = result == 1 ? "Device reboot successfully！" : "Warning! Device reboot failed！";
                                dialog.setMessage(msg);
                                dialog.show(getSupportFragmentManager());
                            }
                            break;

                        case KEY_READ_REGION:
                            handler.removeMessages(0);
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getLoraMode());
                            timeout();
                            if (length == 1) {
                                int region = value[3] & 0xff;
                                if (region == 2) regionStr = "EU868";
                                if (region == 3) regionStr = "AS923";
                            }
                            break;

                        case KEY_READ_LORA_MODE:
                            handler.removeMessages(0);
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getHeartInterval());
                            timeout();
                            if (length == 1) {
                                loraMode = value[3] & 0xff;
                                String mode = loraMode == 0 ? "ABP" : "OTAA";
                                mBind.tvRegion.setText(mode + "/" + regionStr + "/ClassA");
                            }
                            break;

                        case KEY_READ_HEART_INTERVAL:
                            handler.removeMessages(0);
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getDetectionAlgorithms());
                            timeout();
                            if (length == 2) {
                                int interval = MokoUtils.toInt(Arrays.copyOfRange(value, 3, 3 + length));
                                mBind.etHeartInterval.setText(String.valueOf(interval));
                                mBind.etHeartInterval.setSelection(mBind.etHeartInterval.getText().length());
                            }
                            break;

                        case KEY_READ_DETECTION_ALGORITHMS:
                            //停车检测算法
                            handler.removeMessages(0);
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getDetectionSensitivity());
                            timeout();
                            if (length == 1) {
                                selectedDetectionAlgorithms = (value[3] & 0xff) - 1;
                                mBind.tvDetectionAlog.setText(valueAlgorithms[selectedDetectionAlgorithms]);
                            }
                            break;

                        case KEY_READ_DETECTION_SENSITIVITY:
                            handler.removeMessages(0);
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getDevEui());
                            timeout();
                            if (length == 1) {
                                selectedSensitivity = (value[3] & 0xff) - 1;
                                mBind.tvDetectionSensitivity.setText(valueSensitivity[selectedSensitivity]);
                            }
                            break;

                        case KEY_READ_DEV_EUI:
                            dismissSyncProgressDialog();
                            handler.removeMessages(0);
                            if (length == 8) {
                                String devEui = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 3, 3 + length));
                                mBind.tvDevEui.setText(devEui);
                            }
                            break;

                        case KEY_REBOOT_DEVICE:
                            dismissSyncProgressDialog();
                            handler.removeMessages(0);
                            if (length == 1) {
                                int result = value[3] & 0xff;
                                AlertMessageDialog dialog = new AlertMessageDialog();
                                dialog.setConfirm("OK");
                                if (result == 0) {
                                    dialog.setCancelGone();
                                } else {
                                    dialog.setCancel("Try again");
                                    dialog.setOnAlertCancelListener(this::reboot);
                                }
                                String msg = result == 0 ? "Device reboot successfully！" : "Warning! Device reboot failed！";
                                dialog.setMessage(msg);
                                dialog.show(getSupportFragmentManager());
                            }
                            break;

                        case KEY_UPLINK_TEST:
                            dismissSyncProgressDialog();
                            handler.removeMessages(0);
                            if (length == 1) {
                                int result = value[3] & 0xff;
                                AlertMessageDialog dialog = new AlertMessageDialog();
                                dialog.setConfirm("OK");
                                if (result == 0) {
                                    dialog.setCancelGone();
                                } else {
                                    dialog.setCancel("Try again");
                                    dialog.setOnAlertCancelListener(this::uplinkTest);
                                }
                                String msg = result == 0 ? "Device State Payload for test snet successfully！" : "Warning! Device State Payload for test snet failed！";
                                dialog.setMessage(msg);
                                dialog.show(getSupportFragmentManager());
                            }
                            break;

                        case KEY_SET_DEVICE_WORK_MODE:
                            if (length == 1) {
                                int result = value[3] & 0xff;
                                if (result == 0) {
                                    if (onAutoInstall) {
                                        handler.removeMessages(0);
                                        dismissSyncProgressDialog();
                                        onAutoInstall = false;
                                        workMode = 1;
                                        mBind.tvModeSelect.setText(mValues[workMode]);
                                        mBind.tvMode.setText(mValues[workMode]);
                                        mBind.viewDisable.setVisibility(workMode == 0 ? View.VISIBLE : View.GONE);
                                    } else {
                                        //正常修改的
                                        if (workMode == 0) {
                                            //睡眠模式
                                            mBind.tvModeSelect.setText(mValues[workMode]);
                                            mBind.tvMode.setText(mValues[workMode]);
                                            mBind.viewDisable.setVisibility(workMode == 0 ? View.VISIBLE : View.GONE);
                                        }
                                        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setHeartInterval(interval));
                                    }
                                } else {
                                    dismissSyncProgressDialog();
                                    ToastUtils.showToast(this, "set up failed");
                                }
                            }
                            break;

                        case KEY_SET_HEART_INTERVAL:
                            if (length == 1) {
                                int result = value[3] & 0xff;
                                if (result == 0) {
                                    MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setDetectionAlgorithms(selectedDetectionAlgorithms + 1));
                                } else {
                                    dismissSyncProgressDialog();
                                    ToastUtils.showToast(this, "set up failed");
                                }
                            }
                            break;

                        case KEY_SET_DETECTION_ALGORITHMS:
                            if (length == 1) {
                                int result = value[3] & 0xff;
                                if (result == 0) {
                                    MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setDetectionSensitivity(selectedSensitivity + 1));
                                } else {
                                    dismissSyncProgressDialog();
                                    ToastUtils.showToast(this, "set up failed");
                                }
                            }
                            break;

                        case KEY_SET_DETECTION_SENSITIVITY:
                            dismissSyncProgressDialog();
                            handler.removeMessages(0);
                            if (length == 1) {
                                int result = value[3] & 0xff;
                                ToastUtils.showToast(this, result == 0 ? "set up success" : "set up failed");
                            }
                            break;

                        case KEY_RESET:
                            dismissSyncProgressDialog();
                            handler.removeMessages(0);
                            if (length == 1) {
                                int result = value[3] & 0xff;
                                if (result == 0) {
                                    AlertMessageDialog dialog = new AlertMessageDialog();
                                    dialog.setTitle("Factory Reset");
                                    dialog.setMessage("Factory reset successfully!");
                                    dialog.setCancelGone();
                                    dialog.setConfirm("OK");
                                    dialog.show(getSupportFragmentManager());
                                } else {
                                    ToastUtils.showToast(this, "set up failed");
                                }
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
            timeout();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setWorkMode(workMode));
        } else {
            ToastUtils.showToast(this, "Para error!");
        }
    }

    private int interval;

    private boolean isValid() {
        if (TextUtils.isEmpty(mBind.etHeartInterval.getText())) return false;
        interval = Integer.parseInt(mBind.etHeartInterval.getText().toString());
        return (interval >= 1 && interval <= 2880);
    }

    public void onBack(View view) {
        back();
    }

    private void back() {
        MokoSupport.getInstance().disConnectBle();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void showDisconnectDialog() {
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setMessage("The device is disconnected!");
        dialog.setConfirm("OK");
        dialog.setCancelGone();
        dialog.setOnAlertConfirmListener(() -> {
            setResult(RESULT_OK);
            finish();
        });
        dialog.show(getSupportFragmentManager());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    // 记录上次页面控件点击时间,屏蔽无效点击事件
    private long mLastOnClickTime = 0;
    private int mTriggerSum;

    private boolean isTriggerValid() {
        long current = SystemClock.elapsedRealtime();
        if (current - mLastOnClickTime > 500) {
            mTriggerSum = 0;
            mLastOnClickTime = current;
        } else {
            mTriggerSum++;
            if (mTriggerSum == 2) {
                mTriggerSum = 0;
                return true;
            }
        }
        return false;
    }
}
