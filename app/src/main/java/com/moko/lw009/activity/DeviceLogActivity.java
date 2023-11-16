package com.moko.lw009.activity;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lw009.adapter.DeviceLogAdapter;
import com.moko.lw009.databinding.ActivityDeviceLogBinding;
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
    private final List<String> dataList = new LinkedList<>();
    private final Map<String, String> map = new HashMap<>(32);
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private boolean hasMore;
    private final String defaultLog = "蓝牙修改参数......";
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
        map.put("01", "有车");
        map.put("02", "无车");
        map.put("03", "强磁");
        map.put("04", "低电压报警");
        map.put("05", "磁传感器检测失效（可读IC信息）");
        map.put("06", "磁传感器硬件损坏(不可读IC信息)");
        map.put("07", "服务平台修改心跳");
        map.put("08", "服务平台同步时间");
        map.put("09", "服务平台修改灵敏度");
        map.put("0a", "服务平台无车校准");
        map.put("0b", "服务平台复位设备");
        map.put("0c", "LORA WAN注册失败，发送数据失败");
        map.put("0d", "LORA WAN下行MAC命令");
        map.put("0e", "IWDG复位");
        map.put("0f", "外部硬看门狗复位");
        map.put("10", "6次心跳网关失联复位");
        map.put("11", "修改服务平台IP地址");
        map.put("12", "修改服务平台端口号");
        map.put("13", "修改IOT平台");
        map.put("14", "修改ACK");
        map.put("15", "数据发送失败");
        map.put("16", "NB模块重启");
        map.put("31", "手动触发状态包发送");
        map.put("32", "手动触发参数包发送");
        map.put("33", "设备命令复位");
        map.put("80", "蓝牙有车校准");
        map.put("81", "蓝牙无车校准");
        map.put("82", "蓝牙修改工作模式");
        map.put("83", "蓝牙修改本地时间");
        map.put("f1", "固件升级");
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
                                XLog.i("333333log=" + MokoUtils.bytesToHexString(data));
                                for (int i = 0; i < logLength; i++) {
                                    if (data[i * 12] == 1) {
                                        //有记录数据
                                        hasMore = true;
                                        String type = MokoUtils.byte2HexString(data[i * 12 + 1]);
                                        String content = null == map.get(type) ? defaultLog : map.get(type);
                                        long timeTemp = MokoUtils.toInt(Arrays.copyOfRange(data, i * 12 + 2, i * 12 + 6));
                                        String time = sdf.format(new Date(timeTemp * 1000));
                                        XLog.i("333333time=" + content + time);
                                        dataList.add(0, time + "      " + content);
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
