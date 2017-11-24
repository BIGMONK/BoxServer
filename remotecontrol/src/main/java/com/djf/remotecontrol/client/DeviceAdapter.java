package com.djf.remotecontrol.client;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.djf.remotecontrol.ServerDevice;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by djf on 2017/11/24.
 */

public class DeviceAdapter extends BaseAdapter {
    private List<ServerDevice> datas;
    private Context context;

    public DeviceAdapter( Context context,List<ServerDevice> datas) {
        this.datas = datas;
        this.context = context;
    }

    @Override
    public int getCount() {
        if (datas!=null){
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
        TextView textView=new TextView(context);
        textView.setText(datas.get(position).toString());
        return textView;
    }
}
