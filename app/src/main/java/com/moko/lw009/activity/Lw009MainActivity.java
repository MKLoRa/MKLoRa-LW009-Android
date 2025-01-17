package com.moko.lw009.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lw009.AppConstants;
import com.moko.lw009.BuildConfig;
import com.moko.lw009.R;
import com.moko.lw009.adapter.DeviceListAdapter;
import com.moko.lw009.databinding.Ps101mActivityMainBinding;
import com.moko.lw009.dialog.AlertMessageDialog;
import com.moko.lw009.dialog.LoadingMessageDialog;
import com.moko.lw009.dialog.PasswordDialog;
import com.moko.lw009.dialog.ScanFilterDialog;
import com.moko.lw009.entity.AdvInfo;
import com.moko.lw009.utils.AdvInfoAnalysisImpl;
import com.moko.lw009.utils.SPUtiles;
import com.moko.lw009.utils.ToastUtils;
import com.moko.support.lw009.MokoBleScanner;
import com.moko.support.lw009.MokoSupport;
import com.moko.support.lw009.OrderTaskAssembler;
import com.moko.support.lw009.callback.MokoScanDeviceCallback;
import com.moko.support.lw009.entity.DeviceInfo;
import com.moko.support.lw009.entity.OrderCHAR;
import com.moko.support.lw009.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class Lw009MainActivity extends Lw009BaseActivity implements MokoScanDeviceCallback, BaseQuickAdapter.OnItemChildClickListener {
    private Ps101mActivityMainBinding mBind;
    private boolean mReceiverTag = false;
    private ConcurrentHashMap<String, AdvInfo> beaconInfoHashMap;
    private ArrayList<AdvInfo> beaconInfos;
    private DeviceListAdapter adapter;
    private Animation animation = null;
    private MokoBleScanner mokoBleScanner;
    public Handler mHandler;
    public static String PATH_LOGCAT;
    private String mSavedPassword;
    private AdvInfoAnalysisImpl beaconInfoParseable;
    public String filterName;
    public int filterRssi = -127;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Ps101mActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        // 初始化Xlog
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 优先保存到SD卡中
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PATH_LOGCAT = getExternalFilesDir(null).getAbsolutePath() + File.separator + (BuildConfig.IS_LIBRARY ? "MKLoRa" : "LW009");
            } else {
                PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + (BuildConfig.IS_LIBRARY ? "MKLoRa" : "LW009");
            }
        } else {
            // 如果SD卡不存在，就保存到本应用的目录下
            PATH_LOGCAT = getFilesDir().getAbsolutePath() + File.separator + (BuildConfig.IS_LIBRARY ? "MKLoRa" : "LW009");
        }
        MokoSupport.getInstance().init(getApplicationContext());
        mSavedPassword = SPUtiles.getStringValue(this, AppConstants.SP_KEY_SAVED_PASSWORD_LW006, "");
        beaconInfoHashMap = new ConcurrentHashMap<>();
        beaconInfos = new ArrayList<>();
        adapter = new DeviceListAdapter();
        adapter.replaceData(beaconInfos);
        adapter.setOnItemChildClickListener(this);
        adapter.openLoadAnimation();
        mBind.rvDevices.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.shape_recycleview_divider));
        mBind.rvDevices.addItemDecoration(itemDecoration);
        mBind.rvDevices.setAdapter(adapter);
        mHandler = new Handler(Looper.getMainLooper());
        mokoBleScanner = new MokoBleScanner(this);
        EventBus.getDefault().register(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            MokoSupport.getInstance().enableBluetooth();
        } else {
            if (animation == null) startScan();
        }
    }

    private void startScan() {
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            MokoSupport.getInstance().enableBluetooth();
            return;
        }
        animation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
        mBind.ivRefresh.startAnimation(animation);
        beaconInfoParseable = new AdvInfoAnalysisImpl();
        mokoBleScanner.startScanDevice(this);
        mHandler.postDelayed(() -> mokoBleScanner.stopScanDevice(), 1000 * 60);
    }

    @Override
    public void onStartScan() {
        beaconInfoHashMap.clear();
        new Thread(() -> {
            while (animation != null) {
                runOnUiThread(() -> {
                    adapter.replaceData(beaconInfos);
                    mBind.tvDeviceNum.setText(String.format("DEVICE(%d)", beaconInfos.size()));
                });
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                updateDevices();
            }
        }).start();
    }

    @Override
    public void onScanDevice(DeviceInfo deviceInfo) {
        AdvInfo beaconInfo = beaconInfoParseable.parseDeviceInfo(deviceInfo);
        if (beaconInfo == null) return;
        beaconInfo.name = "LW009-" + (beaconInfo.mac.substring(11).replace(":", ""));
        beaconInfoHashMap.put(beaconInfo.mac, beaconInfo);
    }

    @Override
    public void onStopScan() {
        mBind.ivRefresh.clearAnimation();
        animation = null;
    }

    private void updateDevices() {
        beaconInfos.clear();
        if (!TextUtils.isEmpty(filterName) || filterRssi != -127) {
            ArrayList<AdvInfo> beaconInfosFilter = new ArrayList<>(beaconInfoHashMap.values());
            Iterator<AdvInfo> iterator = beaconInfosFilter.iterator();
            while (iterator.hasNext()) {
                AdvInfo beaconInfo = iterator.next();
                if (beaconInfo.rssi > filterRssi) {
                    if (TextUtils.isEmpty(filterName)) {
                        continue;
                    } else {
                        if (TextUtils.isEmpty(beaconInfo.name) && TextUtils.isEmpty(beaconInfo.mac)) {
                            iterator.remove();
                        } else if (TextUtils.isEmpty(beaconInfo.name) && beaconInfo.mac.toLowerCase().replaceAll(":", "").contains(filterName.toLowerCase())) {
                            continue;
                        } else if (TextUtils.isEmpty(beaconInfo.mac) && beaconInfo.name.toLowerCase().contains(filterName.toLowerCase())) {
                            continue;
                        } else if (!TextUtils.isEmpty(beaconInfo.name) && !TextUtils.isEmpty(beaconInfo.mac) && (beaconInfo.name.toLowerCase().contains(filterName.toLowerCase()) || beaconInfo.mac.toLowerCase().replaceAll(":", "").contains(filterName.toLowerCase()))) {
                            continue;
                        } else {
                            iterator.remove();
                        }
                    }
                } else {
                    iterator.remove();
                }
            }
            beaconInfos.addAll(beaconInfosFilter);
        } else {
            beaconInfos.addAll(beaconInfoHashMap.values());
        }
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(beaconInfos, (lhs, rhs) -> {
            if (lhs.rssi > rhs.rssi) {
                return -1;
            } else if (lhs.rssi < rhs.rssi) {
                return 1;
            }
            return 0;
        });
    }

    public void onRefresh(View view) {
        if (isWindowLocked()) return;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            MokoSupport.getInstance().enableBluetooth();
            return;
        }
        if (animation == null) {
            startScan();
        } else {
            mHandler.removeMessages(0);
            mokoBleScanner.stopScanDevice();
        }
    }

    public void onBack(View view) {
        back();
    }

    private void back() {
        if (animation != null) {
            mHandler.removeMessages(0);
            mokoBleScanner.stopScanDevice();
        }
        if (BuildConfig.IS_LIBRARY) {
            finish();
        } else {
            AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setMessage(R.string.main_exit_tips);
            dialog.setOnAlertConfirmListener(Lw009MainActivity.this::finish);
            dialog.show(getSupportFragmentManager());
        }
    }

    public void onAbout(View view) {
        if (isWindowLocked()) return;
        startActivity(new Intent(this, AboutActivity.class));
    }

    public void onFilter(View view) {
        if (isWindowLocked()) return;
        if (animation != null) {
            mHandler.removeMessages(0);
            mokoBleScanner.stopScanDevice();
        }
        ScanFilterDialog scanFilterDialog = new ScanFilterDialog(this);
        scanFilterDialog.setFilterName(filterName);
        scanFilterDialog.setFilterRssi(filterRssi);
        scanFilterDialog.setOnScanFilterListener((filterName, filterRssi) -> {
            Lw009MainActivity.this.filterName = filterName;
            Lw009MainActivity.this.filterRssi = filterRssi;
            if (!TextUtils.isEmpty(filterName) || filterRssi != -127) {
                mBind.rlFilter.setVisibility(View.VISIBLE);
                mBind.rlEditFilter.setVisibility(View.GONE);
                StringBuilder stringBuilder = new StringBuilder();
                if (!TextUtils.isEmpty(filterName)) {
                    stringBuilder.append(filterName);
                    stringBuilder.append(";");
                }
                if (filterRssi != -127) {
                    stringBuilder.append(String.format("%sdBm", filterRssi + ""));
                    stringBuilder.append(";");
                }
                mBind.tvFilter.setText(stringBuilder.toString());
            } else {
                mBind.rlFilter.setVisibility(View.GONE);
                mBind.rlEditFilter.setVisibility(View.VISIBLE);
            }
            if (isWindowLocked()) return;
            if (animation == null) startScan();
        });
        scanFilterDialog.setOnDismissListener(dialog -> {
            if (isWindowLocked()) return;
            if (animation == null) startScan();
        });
        scanFilterDialog.show();
    }

    public void onFilterDelete(View view) {
        if (animation != null) {
            mHandler.removeMessages(0);
            mokoBleScanner.stopScanDevice();
        }
        mBind.rlFilter.setVisibility(View.GONE);
        mBind.rlEditFilter.setVisibility(View.VISIBLE);
        filterName = "";
        filterRssi = -127;
        if (isWindowLocked()) return;
        if (animation == null) startScan();
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            MokoSupport.getInstance().enableBluetooth();
            return;
        }
        AdvInfo advInfo = (AdvInfo) adapter.getItem(position);
        if (advInfo != null && advInfo.connectable && !isFinishing()) {
            if (animation != null) {
                mHandler.removeMessages(0);
                mokoBleScanner.stopScanDevice();
            }
            showLoadingProgressDialog();
            MokoSupport.getInstance().connDevice(advInfo.mac);
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            if (animation != null) {
                                mHandler.removeMessages(0);
                                mokoBleScanner.stopScanDevice();
                                onStopScan();
                            }
                            break;
                        case BluetoothAdapter.STATE_ON:
                            if (animation == null) startScan();
                            break;
                    }
                }
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        String action = event.getAction();
        if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
            dismissLoadingProgressDialog();
            dismissLoadingMessageDialog();
            if (!isPasswordError) {
                ToastUtils.showToast(this, "Connection Failed");
            } else {
                isPasswordError = false;
            }
            if (animation == null) startScan();
        }
        if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
            XLog.i("connect success");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        final String action = event.getAction();
        if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            dismissLoadingMessageDialog();
            MokoSupport.getInstance().disConnectBle();
        }
        if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {

        }
        if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
            OrderTaskResponse response = event.getResponse();
            OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
            byte[] value = response.responseValue;
            if (null == value || value.length < 3) return;
            int header = value[0] & 0xff;
            int key = value[1] & 0xff;
            int length = value[2] & 0xff;
            if (header == 0xA3 && key == ParamsKeyEnum.KEY_REQUEST_UNLOCK.getParamsKey() && length == 4) {
                //回复解锁数据
                byte[] bytes = Arrays.copyOfRange(value, 3, length + 3);
                int random = MokoUtils.toInt(bytes);
                int data = randMixture(random);
                MokoSupport.getInstance().sendOrder(OrderTaskAssembler.replyUnlock(bytes, data));
            } else if (header == 0xA3 && key == ParamsKeyEnum.KEY_REQUEST_UNLOCK_COMPLETE.getParamsKey() && length == 1) {
                //回复蓝牙解锁状态
                dismissLoadingProgressDialog();
                int result = value[3] & 0xff;
                XLog.i("333333lock=" + (result == 0 ? "解锁成功" : "解锁失败"));
                MokoSupport.getInstance().sendOrder(OrderTaskAssembler.replyUnlockState(result));
                showPassword();
            }
        }
    }

    private boolean isPasswordError;

    private void showPassword() {
        final PasswordDialog dialog = new PasswordDialog(this);
        dialog.setData(mSavedPassword);
        dialog.setOnPasswordClicked(new PasswordDialog.PasswordClickListener() {
            @Override
            public void onEnsureClicked(String password) {
                if (!"Moko4321".equals(password)) {
                    isPasswordError = true;
                    ToastUtils.showToast(Lw009MainActivity.this, "Password Error");
                    MokoSupport.getInstance().disConnectBle();
                } else {
                    //密码校验成功了
                    startActivity(new Intent(Lw009MainActivity.this, DeviceSettingActivity.class));
                }
            }

            @Override
            public void onDismiss() {
                MokoSupport.getInstance().disConnectBle();
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

    private int randMixture(int rand) {
        int Temp;
        Temp = rand ^ 0x13141516;
        Temp += rand;
        Temp |= (~rand);
        return Temp;
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    private void showLoadingMessageDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Verifying..");
        mLoadingMessageDialog.show(getSupportFragmentManager());
    }

    private void dismissLoadingMessageDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            unregisterReceiver(mReceiver);
        }
        EventBus.getDefault().unregister(this);
    }
}
