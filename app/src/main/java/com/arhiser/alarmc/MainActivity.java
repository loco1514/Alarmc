package com.arhiser.alarmc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements
        AlarmAdapter.OnAlarmClickListener {

    private static final String PREFS_NAME = "AlarmPrefs";
    private static final String ALARMS_KEY = "alarms";
    private static final int SETTINGS_REQUEST_CODE = 0;

    private List<AlarmData> alarmList;
    private ListView listViewAlarms;
    private AlarmAdapter adapter; // Move this line to the class level

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarms);

        listViewAlarms = findViewById(R.id.listViewAlarms);
        Button btnOpenSettings = findViewById(R.id.btnOpenSettings);

        alarmList = new ArrayList<>();
        adapter = new AlarmAdapter(this, R.layout.alarm_item, alarmList, this);
        listViewAlarms.setAdapter(adapter);

        loadAlarms();

        btnOpenSettings.setOnClickListener(view ->
                startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), SETTINGS_REQUEST_CODE));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlarms();
    }

    private void loadAlarms() {
        List<AlarmData> alarmsList = getAlarmsFromPreferences();
        updateAlarmsListView(alarmsList);

        for (AlarmData alarm : alarmsList) {
            if (alarm != null) {
                Log.d("MainActivity", "Loaded Alarm: " +
                        "Time: " + alarm.getAlarmText() +
                        ", Day of Week: " + alarm.getDayOfWeek() +
                        ", Week Type: " + alarm.getWeekTypeText());
            }
        }
    }

    private List<AlarmData> getAlarmsFromPreferences() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> alarmsSet = preferences.getStringSet(ALARMS_KEY, new HashSet<>());

        List<AlarmData> alarmsList = new ArrayList<>();

        for (String alarmString : alarmsSet) {
            try {
                AlarmData alarm = AlarmData.fromString(alarmString);
                alarmsList.add(alarm);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return alarmsList;
    }

    private void updateAlarmsListView(List<AlarmData> alarms) {
        alarmList.clear();
        alarmList.addAll(alarms);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onEditClick(int position) {
        Intent editIntent = new Intent(this, SettingsActivity.class);
        editIntent.putExtra("EDIT_ALARM_POSITION", position);
        startActivityForResult(editIntent, SETTINGS_REQUEST_CODE);
    }

    @Override
    public void onDeleteClick(int position) {
        alarmList.remove(position);
        adapter.notifyDataSetChanged();
        updatePreferences(alarmList);
    }


    private void updatePreferences(List<AlarmData> alarms) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(ALARMS_KEY); // Remove existing alarms

        Set<String> alarmsSet = new HashSet<>();
        for (AlarmData alarm : alarms) {
            if (alarm != null) {
                alarmsSet.add(alarm.toString());
            }
        }

        editor.putStringSet(ALARMS_KEY, alarmsSet);
        boolean success = editor.commit();

        if (success) {
            Log.d("MainActivity", "updatePreferences: Preferences Updated");
        } else {
            Log.e("MainActivity", "updatePreferences: Failed to update preferences");
        }
    }
}