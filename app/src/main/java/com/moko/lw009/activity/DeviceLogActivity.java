package com.moko.lw009.activity;

import android.os.Bundle;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lw009.adapter.DeviceLogAdapter;
import com.moko.lw009.databinding.ActivityDeviceLogBinding;
import com.moko.lw009.entity.DeviceLogBean;
import com.moko.lw009.utils.ToastUtils;
import com.moko.support.lw009.MokoSupport;
import com.moko.support.lw009.OrderTaskAssembler;
import com.moko.support.lw009.entity.OrderCHAR;
import com.moko.support.lw009.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author: jun.liu
 * @date: 2023/11/14 14:03
 * @des:
 */
public class DeviceLogActivity extends Lw009BaseActivity {
    private ActivityDeviceLogBinding mBind;
    private DeviceLogAdapter adapter;
    private int currentPackage;
    private final List<DeviceLogBean> dataList = new LinkedList<>();
    private final Map<String, String> map = new HashMap<>(64);
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private boolean hasMore;
    private final String defaultLog = "Bluetooth modification parameters";
    private boolean hasSaved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityDeviceLogBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        adapter = new DeviceLogAdapter();
        adapter.replaceData(dataList);
        mBind.rvList.setAdapter(adapter);
        map.put("01", "Have a car");
        map.put("02", "No car");
        map.put("03", "Strong magnet");
        map.put("04", "Low voltage alarm");
        map.put("05", "Magnetic sensor detection failure (readable IC information)");
        map.put("06", "Magnetic sensor hardware is damaged (IC information cannot be read)");
        map.put("07", "Service platform modifies heartbeat");
        map.put("08", "Service platform synchronization time");
        map.put("09", "Service platform modification sensitivity");
        map.put("0a", "Service platform car-free calibration");
        map.put("0b", "Service platform reset equipment");
        map.put("0c", "LORA WAN registration failed and data sending failed");
        map.put("0d", "LORA WAN downlink MAC command");
        map.put("0e", "IWDG reset");
        map.put("0f", "External hard watchdog reset");
        map.put("10", "6 Heartbeat Gateway Lost Connection Reset");
        map.put("11", "Modify the service platform IP address");
        map.put("12", "Modify the service platform port number");
        map.put("13", "Modify IOT platform");
        map.put("14", "Modify ACK");
        map.put("15", "Data sending failed");
        map.put("16", "NB module restart");
        map.put("31", "Manually trigger status packet sending");
        map.put("32", "Manually trigger parameter packet sending");
        map.put("33", "Device command reset");
        map.put("80", "Bluetooth car calibration");
        map.put("81", "Bluetooth car-free calibration");
        map.put("82", "Bluetooth modification working mode");
        map.put("83", "Bluetooth changes local time");
        map.put("f1", "Firmware upgrade");
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
                                    if (!hasSaved) {
                                        hasSaved = true;
                                        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.readLastLog(0));
                                    } else {
                                        dismissSyncProgressDialog();
                                    }
                                } else {
                                    dismissSyncProgressDialog();
                                }
                            }
                            break;

                        case KEY_READ_DEVICE_LOG:
                            if (length > 0) {
                                dismissSyncProgressDialog();
                                currentPackage = value[3] & 0xff;
                                byte[] data = Arrays.copyOfRange(value, 4, 3 + length);
                                int logLength = data.length / 12;
                                for (int i = 0; i < logLength; i++) {
                                    if (data[i * 12] == 1) {
                                        //有记录数据
                                        hasMore = true;
                                        DeviceLogBean bean = new DeviceLogBean();
                                        String type = MokoUtils.byte2HexString(data[i * 12 + 1]);
                                        bean.content = null == map.get(type) ? defaultLog : map.get(type);
                                        long timeTemp = MokoUtils.toInt(Arrays.copyOfRange(data, i * 12 + 2, i * 12 + 6));
                                        bean.time = sdf.format(new Date(timeTemp * 1000));
                                        dataList.add(0, bean);
                                    } else {
                                        hasMore = false;
                                    }
                                }
                                if (hasMore) currentPackage++;
                                adapter.replaceData(dataList);
                                if (hasMore && currentPackage < 10) {
                                    MokoSupport.getInstance().sendOrder(OrderTaskAssembler.readLastLog(currentPackage));
                                }
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
        if (isWindowLocked() || hasSaved) return;
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setLogSwitch(mBind.cbSwitch.isChecked() ? 1 : 0));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
