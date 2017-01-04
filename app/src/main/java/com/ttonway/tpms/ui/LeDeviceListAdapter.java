package com.ttonway.tpms.ui;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ttonway.tpms.R;
import com.ttonway.tpms.bluetooth.BluetoothLeDriver;
import com.ttonway.tpms.core.TpmsDevice;

import java.util.ArrayList;

// Adapter for holding devices found through scanning.
public class LeDeviceListAdapter extends BaseAdapter {
    private ArrayList<BluetoothDevice> mLeDevices;
    private LayoutInflater mInflator;

    private BluetoothLeDriver mLeDriver;

    public LeDeviceListAdapter(Context context) {
        super();
        mLeDevices = new ArrayList<BluetoothDevice>();
        mInflator = LayoutInflater.from(context);

        mLeDriver = (BluetoothLeDriver) TpmsDevice.getInstance(context).getTpmsDriver();
    }

    public void addDevice(BluetoothDevice device) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = mInflator.inflate(R.layout.list_item_device, null);
            viewHolder = new ViewHolder();
            viewHolder.indicator = view.findViewById(R.id.indicator);
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDevice device = mLeDevices.get(i);
        if (TextUtils.equals(mLeDriver.getBluetoothDeviceAddress(), device.getAddress())) {
            viewHolder.indicator.setVisibility(View.VISIBLE);
        } else {
            viewHolder.indicator.setVisibility(View.INVISIBLE);
        }
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText(R.string.unknown_device);
        viewHolder.deviceAddress.setText(device.getAddress());

        return view;
    }

    static class ViewHolder {
        View indicator;
        TextView deviceName;
        TextView deviceAddress;
    }
}