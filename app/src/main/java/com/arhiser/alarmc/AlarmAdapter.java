package com.arhiser.alarmc;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

public class AlarmAdapter extends ArrayAdapter<AlarmData> {

    private OnAlarmClickListener onAlarmClickListener;

    public interface OnAlarmClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }


    public AlarmAdapter(Context context, int resource, List<AlarmData> objects, OnAlarmClickListener listener) {
        super(context, resource, objects);
        this.onAlarmClickListener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.alarm_item, parent, false);

        TextView textViewTime = row.findViewById(R.id.textViewTime);
        TextView textViewDayOfWeek = row.findViewById(R.id.textViewDayOfWeek);
        TextView textViewWeekType = row.findViewById(R.id.textViewWeekType);

        AlarmData alarmData = getItem(position);

        if (alarmData != null) {
            textViewTime.setText(alarmData.getAlarmText());
            textViewDayOfWeek.setText(alarmData.getDayOfWeek());
            textViewWeekType.setText(alarmData.getWeekTypeText());

        }

        row.setOnClickListener(v -> {
            if (onAlarmClickListener != null) {
                onAlarmClickListener.onEditClick(position);
            }
        });

        Button deleteButton = row.findViewById(R.id.btnDelete);
        deleteButton.setOnClickListener(v -> {
            if (onAlarmClickListener != null) {
                onAlarmClickListener.onDeleteClick(position);
            }
        });

        return row;
    }
}