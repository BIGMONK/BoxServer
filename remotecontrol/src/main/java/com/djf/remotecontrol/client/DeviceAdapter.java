package com.djf.remotecontrol.client;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.djf.remotecontrol.CommandMsgBean;

import java.util.List;

/**
 * Created by djf on 2017/11/24.
 */

public class DeviceAdapter extends BaseAdapter {
    private List<CommandMsgBean> datas;
    private Context context;

    public DeviceAdapter(Context context, List<CommandMsgBean> datas) {
        this.datas = datas;
        this.context = context;
    }

    @Override
    public int getCount() {
        if (datas != null) {
            return datas.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = new TextView(context);
        CommandMsgBean bean = datas.get(position);
        String type = bean.getKeycode() == CommandMsgBean.DEVICE_FAMILY ? "家庭骑行"
                : (bean.getKeycode() == CommandMsgBean.DEVICE_BOAT ? "划船机" : (bean.getKeycode() ==
                CommandMsgBean.DEVICE_COACH ? "健身房系统" : "未知设备"));
        String msg = bean.getDevice() + "  " + type + "  MAC=" + bean.getMac() + "  IP=" + bean.getIp();
        textView.setText(msg);
        return textView;
    }
}
