package com.moko.lw009.activity;

import android.os.Bundle;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.lw009.databinding.ActivityDeviceLogBinding;
import com.moko.lw009.utils.ToastUtils;
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
 * @date: 2023/11/14 14:03
 * @des:
 */
public class DeviceLogActivity extends Lw009BaseActivity {
    private ActivityDeviceLogBinding mBind;
    private StringBuilder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityDeviceLogBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 400)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 400)
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
                        case KEY_SET_LOG_ENABLE:
                            if (length == 1) {
                                int result = value[3] & 0xff;
                                ToastUtils.showToast(this, result == 0 ? "set up success" : "set up failed");
                                if (mBind.cbSwitch.isChecked()) {
                                    builder = new StringBuilder();
                                    MokoSupport.getInstance().sendOrder(OrderTaskAssembler.readLastLog());
                                } else {
                                    dismissSyncProgressDialog();
                                }
                            }
                            break;

                        case KEY_READ_DEVICE_LOG:
                            if (length > 0) {
                                String log = new String(Arrays.copyOfRange(value, 3, 3 + length));
                                builder.append(log);
                                mBind.tvLog.setText(log);
                            }
                            break;
                    }
                }
            }
        });
    }

    public void onBack(View view) {
        finish();
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setLogSwitch(mBind.cbSwitch.isChecked() ? 1 : 0));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
