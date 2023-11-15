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
import com.moko.lw009.databinding.ActivityLoraSettingsBinding;
import com.moko.lw009.dialog.AlertMessageDialog;
import com.moko.lw009.dialog.BottomDialog;
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

/**
 * @author: jun.liu
 * @date: 2023/11/13 14:46
 * @des:
 */
public class LoraSettingsActivity extends Lw009BaseActivity {
    private ActivityLoraSettingsBinding mBind;
    private int loraMode;
    private final String[] valuesMode = {"ABP", "OTAA"};
    private boolean hasSaved;
    private int savedLoraMode;
    private String savedDevEui;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityLoraSettingsBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        String regionStr = getIntent().getStringExtra("region");
        loraMode = getIntent().getIntExtra("loraMode", 0);
        mBind.tvLoraMode.setText(loraMode == 0 ? "ABP" : "OTAA");
        mBind.tvRegion.setText(regionStr);
        changeMode(loraMode);
        mBind.tvLoraMode.setOnClickListener(v -> onLoraModeClick());
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getDevEui());
    }

    private void onLoraModeClick() {
        if (isWindowLocked()) return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(valuesMode)), loraMode);
        dialog.setListener(value -> {
            loraMode = value;
            mBind.tvLoraMode.setText(valuesMode[value]);
            changeMode(value);
        });
        dialog.show(getSupportFragmentManager());
    }

    private void changeMode(int mode) {
        if (mode == 0) {
            //ABP
            mBind.lineAppKey.setVisibility(View.VISIBLE);
            mBind.layoutAppKey.setVisibility(View.VISIBLE);
            mBind.lineDevAddR.setVisibility(View.GONE);
            mBind.layoutDevAddR.setVisibility(View.GONE);
            mBind.lineAppSKey.setVisibility(View.GONE);
            mBind.layoutAppSKey.setVisibility(View.GONE);
            mBind.lineNwkSKey.setVisibility(View.GONE);
            mBind.layoutNwkSKey.setVisibility(View.GONE);
        } else {
            mBind.lineAppKey.setVisibility(View.GONE);
            mBind.layoutAppKey.setVisibility(View.GONE);
            mBind.lineDevAddR.setVisibility(View.VISIBLE);
            mBind.layoutDevAddR.setVisibility(View.VISIBLE);
            mBind.lineAppSKey.setVisibility(View.VISIBLE);
            mBind.layoutAppSKey.setVisibility(View.VISIBLE);
            mBind.lineNwkSKey.setVisibility(View.VISIBLE);
            mBind.layoutNwkSKey.setVisibility(View.VISIBLE);
        }
    }

    public void onBack(View view) {
        back();
    }

    private void back() {
        Intent intent = new Intent();
        intent.putExtra("hasSaved", hasSaved);
        intent.putExtra("mode", savedLoraMode);
        intent.putExtra("devEui", savedDevEui);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        back();
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
                        case KEY_READ_DEV_EUI:
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getAppEui());
                            if (length == 8) {
                                String devEui = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 3, 3 + length));
                                mBind.etDevEui.setText(devEui);
                                mBind.etDevEui.setSelection(mBind.etDevEui.getText().length());
                            }
                            break;
                        case KEY_READ_APP_EUI:
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getAppKey());
                            if (length == 8) {
                                String appEui = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 3, 3 + length));
                                mBind.etAppEui.setText(appEui);
                                mBind.etAppEui.setSelection(mBind.etAppEui.getText().length());
                            }
                            break;

                        case KEY_READ_APP_KEY:
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getDevAddR());
                            if (length == 16) {
                                String appKey = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 3, 3 + length));
                                mBind.etAppKey.setText(appKey);
                                mBind.etAppKey.setSelection(mBind.etAppKey.getText().length());
                            }
                            break;

                        case KEY_READ_DEV_ADD_R:
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getAppSKey());
                            if (length == 4) {
                                String devAddR = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 3, 3 + length));
                                mBind.etDevAddR.setText(devAddR);
                                mBind.etDevAddR.setSelection(mBind.etDevAddR.getText().length());
                            }
                            break;

                        case KEY_READ_APP_S_KEY:
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getNwkSKey());
                            if (length == 16) {
                                String appSKey = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 3, 3 + length));
                                mBind.etAppSKey.setText(appSKey);
                                mBind.etAppSKey.setSelection(mBind.etAppSKey.getText().length());
                            }
                            break;

                        case KEY_READ_NWK_S_KEY:
                            dismissSyncProgressDialog();
                            if (length == 16) {
                                String nwsKey = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 3, 3 + length));
                                mBind.etNwkSKey.setText(nwsKey);
                                mBind.etNwkSKey.setSelection(mBind.etNwkSKey.getText().length());
                            }
                            break;

                        case KEY_SET_LORA_MODE:
                            if (length == 1) {
                                int result = value[3] & 0xff;
                                if (result == 0) {
                                    MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setDevEui(mBind.etDevEui.getText().toString()));
                                } else {
                                    dismissSyncProgressDialog();
                                    ToastUtils.showToast(this, "set up failed");
                                }
                            }
                            break;

                        case KEY_SET_DEV_EUI:
                            if (length == 1) {
                                int result = value[3] & 0xff;
                                if (result == 0) {
                                    MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setAppEui(mBind.etAppEui.getText().toString()));
                                } else {
                                    dismissSyncProgressDialog();
                                    ToastUtils.showToast(this, "set up failed");
                                }
                            }
                            break;

                        case KEY_SET_APP_EUI:
                            if (length == 1) {
                                int result = value[3] & 0xff;
                                if (result == 0) {
                                    if (loraMode == 1) {
                                        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setAppKey(mBind.etAppKey.getText().toString()));
                                    } else {
                                        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setDevAddR(mBind.etDevAddR.getText().toString()));
                                    }
                                } else {
                                    dismissSyncProgressDialog();
                                    ToastUtils.showToast(this, "set up failed");
                                }
                            }
                            break;

                        case KEY_SET_APP_KEY:
                        case KEY_SET_NWK_S_KEY:
                            if (length == 1) {
                                int result = value[3] & 0xff;
                                dismissSyncProgressDialog();
                                if (result == 0) {
                                    showSuccessDialog();
                                } else {
                                    ToastUtils.showToast(this, "set up failed");
                                }
                            }
                            break;

                        case KEY_SET_DEV_ADD_R:
                            if (length == 1) {
                                int result = value[3] & 0xff;
                                if (result == 0) {
                                    MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setAppSKey(mBind.etAppSKey.getText().toString()));
                                } else {
                                    dismissSyncProgressDialog();
                                    ToastUtils.showToast(this, "set up failed");
                                }
                            }
                            break;

                        case KEY_SET_APP_S_KEY:
                            if (length == 1) {
                                int result = value[3] & 0xff;
                                if (result == 0) {
                                    MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setNwkSKey(mBind.etNwkSKey.getText().toString()));
                                } else {
                                    dismissSyncProgressDialog();
                                    ToastUtils.showToast(this, "set up failed");
                                }
                            }
                            break;

                        case KEY_RAED_DEVICE_WORK_MODE:
                            dismissSyncProgressDialog();
                            if (length == 1) {
                                int result = value[3] & 0xff;
                                if (result == 1) showRebootDialog();
                            }
                            break;
                    }
                }
            }
        });
    }

    private void showRebootDialog() {
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setMessage("Reboot successfully!");
        dialog.setConfirm("OK");
        dialog.setCancelGone();
        hasSaved = true;
        savedLoraMode = loraMode;
        savedDevEui = mBind.etDevEui.getText().toString();
        dialog.show(getSupportFragmentManager());
    }

    private void showSuccessDialog() {
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setMessage("Save Successfully！");
        dialog.setCancelGone();
        dialog.setConfirm("OK");
        dialog.setOnAlertConfirmListener(() -> {
            showSyncingProgressDialog();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.reboot());
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        if (isValid()) {
            showSyncingProgressDialog();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setLoraMode(loraMode));
        } else {
            ToastUtils.showToast(this, "Para error!");
        }
    }

    private boolean isValid() {
        if (TextUtils.isEmpty(mBind.etDevEui.getText()) || mBind.etDevEui.getText().length() != 16)
            return false;
        if (TextUtils.isEmpty(mBind.etAppEui.getText()) || mBind.etAppEui.getText().length() != 16)
            return false;
        if (loraMode == 0) {
            //ABP
            return !TextUtils.isEmpty(mBind.etAppKey.getText()) && mBind.etAppKey.getText().length() == 32;
        } else {
            //OTAA
            if (TextUtils.isEmpty(mBind.etDevAddR.getText()) || mBind.etDevAddR.getText().length() != 8)
                return false;
            if (TextUtils.isEmpty(mBind.etAppSKey.getText()) || mBind.etAppSKey.getText().length() != 32)
                return false;
            return !TextUtils.isEmpty(mBind.etNwkSKey.getText()) && mBind.etNwkSKey.getText().length() == 32;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
