package com.arhiser.alarmc;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionBarPolicy;

import com.google.protobuf.StringValue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AlarmPrefs";
    private static final String ALARMS_KEY = "alarms";

    private RadioGroup radioGroupWeek;
    private Spinner spinnerDayOfWeek;
    private CheckBox checkBoxRepeat;
    private TimePicker timePicker;
    private List<AlarmData> alarmList;
    private AlarmAdapter adapter;
    private Button btnExite;
    Calendar currentTime = Calendar.getInstance();

    @SuppressLint("MissingInflatedId")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize views
        timePicker = findViewById(R.id.timePicker);
        radioGroupWeek = findViewById(R.id.radioGroupWeek);
        spinnerDayOfWeek = findViewById(R.id.spinnerDayOfWeek);
        checkBoxRepeat = findViewById(R.id.checkBoxRepeat);
        btnExite = findViewById(R.id.btnExit);

        if (timePicker != null) {
            timePicker.setIs24HourView(true);
        }

        String[] daysOfWeek = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"};

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, daysOfWeek);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerDayOfWeek.setAdapter(spinnerAdapter);

        // Initialize alarmList before calling loadAlarms
        alarmList = new ArrayList<>();
        loadAlarms();

        // Initialize adapter after loading alarms
        adapter = new AlarmAdapter(this, R.layout.alarm_item, alarmList, null);

        // Handle the case when editing an existing alarm
        Intent intent = getIntent();
        if (intent.hasExtra("EDIT_ALARM_POSITION")) {
            int editedAlarmPosition = intent.getIntExtra("EDIT_ALARM_POSITION", -1);
            if (editedAlarmPosition != -1) {
                // Load existing alarm data for editing
                loadExistingAlarmData(editedAlarmPosition);
            }
        }
        btnExite.setOnClickListener(v ->finish());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void loadExistingAlarmData(int position) {
        if (position >= 0 && position < alarmList.size()) {
            AlarmData existingAlarm = alarmList.get(position);
            if (existingAlarm != null) {
                timePicker.setHour(existingAlarm.getHour());
                timePicker.setMinute(existingAlarm.getMinute());

                // Set other UI components based on the existing alarm data
                int weekType = getWeekTypeFromString(existingAlarm.getWeekTypeText());
                setRadioGroupSelection(weekType);

                spinnerDayOfWeek.setSelection(getIndex(spinnerDayOfWeek, existingAlarm.getDayOfWeek()));

                // Check if the Switch is available in the layout before accessing it
                setAlarm();
            } else {
                // Handle the case where existingAlarm is null
                Log.e("SettingsActivity", "Existing alarm at position " + position + " is null.");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onSaveButtonClick(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int editedAlarmPosition = getIntent().getIntExtra("EDIT_ALARM_POSITION", -1);
            if (editedAlarmPosition != -1) {
                updateAlarm(editedAlarmPosition);
            } else {
                saveAlarm();
            }

            // Новый код для установки будильника
            setAlarm(); // Вызов метода установки будильника
        }
        finish();
    }
    private int generateRequestCode() {
        return (int) System.currentTimeMillis();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setAlarm() {
        int hourOfDay = timePicker.getHour();
        int minute = timePicker.getMinute();
        String dayOfWeek = spinnerDayOfWeek.getSelectedItem().toString();
        Calendar currentTime = Calendar.getInstance();

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, YourAlarmReceiver.class);
        alarmIntent.setAction("YOUR_CUSTOM_ACTION");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, generateRequestCode(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        int dayOfWeeks;
        switch (dayOfWeek) {
            case "Воскресенье":
                dayOfWeeks = Calendar.SUNDAY;
                break;
            case "Понедельник":
                dayOfWeeks = Calendar.MONDAY;
                break;
            case "Вторник":
                dayOfWeeks = Calendar.TUESDAY;
                break;
            case "Среда":
                dayOfWeeks = Calendar.WEDNESDAY;
                break;
            case "Четверг":
                dayOfWeeks = Calendar.THURSDAY;
                break;
            case "Пятница":
                dayOfWeeks = Calendar.FRIDAY;
                break;
            case "Суббота":
                dayOfWeeks = Calendar.SATURDAY;
                break;
            default:
                dayOfWeeks = Calendar.SUNDAY; // По умолчанию можно задать любой день
                break;
        }

        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeeks);

            // Проверяем, прошло ли выбранное время
            if (currentTime.getTimeInMillis() > calendar.getTimeInMillis()) {
                // Если да, устанавливаем на следующую неделю
                calendar.add(Calendar.DAY_OF_WEEK, 14);
            }


                // Установка одиночного аларма
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Log.d("SettingsActivity", "setAlarm() called");
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateAlarm(int position) {
        int hourOfDay = timePicker.getHour();
        int minute = timePicker.getMinute();
        int weekType = getSelectedWeekType();
        String dayOfWeek = spinnerDayOfWeek.getSelectedItem().toString();

        String formattedAlarm = String.format("%02d:%02d,%s,%s,%s,%d",
                hourOfDay, minute, dayOfWeek, getWeekTypeText(weekType), "", 1);

        AlarmData updatedAlarm = AlarmData.fromString(formattedAlarm);
        alarmList.set(position, updatedAlarm);

        // Update the UI or any other necessary steps
        // Example: updateAlarmsListView(alarmList);

        Toast.makeText(this, "Alarm updated", Toast.LENGTH_SHORT).show();

        // Update SharedPreferences
        updateAlarms(alarmList);
        // Notify the adapter that the data set has changed
        adapter.notifyDataSetChanged();
        setAlarm();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveAlarm() {
        int hourOfDay = timePicker.getHour();
        int minute = timePicker.getMinute();
        int weekType = getSelectedWeekType();
        String dayOfWeek = spinnerDayOfWeek.getSelectedItem().toString();

        String formattedAlarm = String.format("%02d:%02d,%s,%s,%s,%d",
                hourOfDay, minute, dayOfWeek, getWeekTypeText(weekType), "", 1);

        List<AlarmData> existingAlarms = getAlarmsFromPreferences();
        existingAlarms.add(AlarmData.fromString(formattedAlarm));
        updateAlarms(existingAlarms);

        Toast.makeText(this, "Alarm saved", Toast.LENGTH_SHORT).show();
        // Notify the adapter that the data set has changed
        adapter.notifyDataSetChanged();
        setAlarm();
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateAlarms(List<AlarmData> alarms) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> alarmsSet = new HashSet<>();

        for (AlarmData alarm : alarms) {
            if (alarm != null) {
                alarmsSet.add(alarm.toString());
            }
        }

        editor.putStringSet(ALARMS_KEY, alarmsSet);
        boolean success = editor.commit(); // Use commit instead of apply for synchronous execution
        setAlarm();

        if (success) {
            Log.d("MainActivity", "updateAlarms: Preferences Updated");
            Log.d("MainActivity", String.valueOf(editor.commit()));
        } else {
            Log.e("MainActivity", "updateAlarms: Failed to update preferences");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void loadAlarms() {
        alarmList = getAlarmsFromPreferences();
        setAlarm();
    }

    private int getSelectedWeekType() {
        int selectedId = radioGroupWeek.getCheckedRadioButtonId();
        RadioButton radioButtonEven = findViewById(R.id.radioButtonEven);
        RadioButton radioButtonOdd = findViewById(R.id.radioButtonOdd);

        if (radioButtonEven != null && radioButtonOdd != null) {
            if (selectedId == radioButtonEven.getId()) {
                return 0; // Even week
            } else if (selectedId == radioButtonOdd.getId()) {
                return 1; // Odd week
            }
        }

        return -1;
    }

    private String getWeekTypeText(int weekType) {
        return weekType == 0 ? "Четная" : "Нечетная";
    }


    private int getWeekTypeFromString(String weekTypeText) {
        return weekTypeText.equals("Четная") ? 0 : 1;
    }

    private void setRadioGroupSelection(int weekType) {
        if (weekType == 0) {
            radioGroupWeek.check(R.id.radioButtonEven);
        } else if (weekType == 1) {
            radioGroupWeek.check(R.id.radioButtonOdd);
        }
    }

    // Helper method to get the index of an item in a spinner
    private int getIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0; // Default to the first item if not found
    }
}