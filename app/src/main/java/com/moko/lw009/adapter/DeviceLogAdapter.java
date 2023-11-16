package com.moko.lw009.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.lw009.R;

/**
 * @author: jun.liu
 * @date: 2023/11/16 9:36
 * @des:
 */
public class DeviceLogAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public DeviceLogAdapter() {
        super(R.layout.item_device_log);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.tvContent, item);
    }
}
