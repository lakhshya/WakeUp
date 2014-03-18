package com.lythin.wakeup;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends FragmentActivity {

    static Database DB;
    AlarmManager alarmMgr;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        DB = Database.getInstance(this);
        Log.i("Test", "MainActivity 1");

        if (DB.isAlarmInProgress()) {
            Intent intentActivity = new Intent(this, AlarmOnActivity.class);
            startActivity(intentActivity);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    public class AlarmsSectionFragment extends Fragment {

        MyCustomAdapter myCustomAdapter;
        ListView listView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_alarms, container, false);
            listView = (ListView) rootView.findViewById(R.id.alarms_listView);

            //create an ArrayAdaptar from the String Array
            myCustomAdapter = new MyCustomAdapter(MainActivity.this, R.layout.alarm_info, DB.getAlarms());

            // Assign adapter to ListView
            listView.setAdapter(myCustomAdapter);


            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            myCustomAdapter.notifyDataSetChanged();
        }

        private class MyCustomAdapter extends ArrayAdapter<Alarm> {

            private ArrayList<Alarm> alarms;

            public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<Alarm> alarms) {
                super(context, textViewResourceId, alarms);
                this.alarms = new ArrayList<Alarm>();
                this.alarms.addAll(alarms);
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {

                ViewHolder holder = null;

                if (convertView == null) {
                    LayoutInflater vi = (LayoutInflater) getSystemService(
                            Context.LAYOUT_INFLATER_SERVICE);
                    convertView = vi.inflate(R.layout.alarm_info, null);

                    holder = new ViewHolder();
                    holder.textView = (TextView) convertView.findViewById(R.id.code);
                    holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox1);
                    convertView.setTag(holder);

                    holder.checkBox.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            CheckBox cb = (CheckBox) v;
                            DB.setAlarmEnabled(position, cb.isChecked());
                            Alarm alarm = DB.getAlarm(position);
                            if (cb.isChecked()) {
                                alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), 24 * 60 * 60 * 1000, alarm.generatePI(MainActivity.this));
                            } else {
                                alarmMgr.cancel(alarm.generatePI(MainActivity.this));
                                //DB.registerAlarmsWithManager(alarmMgr,MainActivity.this);
                            }

                        }
                    });
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                Alarm alarm = alarms.get(position);
                String timeDisplay = String.format("%02d:%02d " + alarm.getAmpm(), alarm.getHour(), alarm.getMinute());
                holder.textView.setText(alarm.getQuote());
                holder.checkBox.setText(timeDisplay);
                holder.checkBox.setChecked(alarm.isEnabled());
                holder.checkBox.setTag(alarm);

                return convertView;

            }

            private class ViewHolder {
                TextView textView;
                CheckBox checkBox;
            }

        }
    }

    public class SetAlarmSectionFragment extends Fragment {

        Button buttonOK, buttonCancel, buttonDelete;
        Spinner spinner;
        ArrayAdapter<String> adapter;
        TimePicker timePicker;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_setalarm, container, false);

            //Initialize the dropdown ListView/Spinner
            spinner = (Spinner) rootView.findViewById(R.id.spinner1);
            adapter = new ArrayAdapter<String>(MainActivity.DB.getContext(), android.R.layout.simple_expandable_list_item_1, android.R.id.text1, DB.getQuotesRef());
            spinner.setAdapter(adapter);

            //Initialize views
            buttonOK = (Button) rootView.findViewById(R.id.buttonOK);
            buttonCancel = (Button) rootView.findViewById(R.id.buttonCancel);
            buttonDelete = (Button) rootView.findViewById(R.id.buttonDelete);
            timePicker = (TimePicker) rootView.findViewById(R.id.timePicker1);

            //Button onClickListeners
            buttonOK.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO find a fix for this bug
                    mViewPager.setCurrentItem(2);
                    int time = timePicker.getCurrentHour() * 60 + timePicker.getCurrentMinute();
                    String quote = spinner.getSelectedItem().toString();
                    DB.addAlarm(time, quote, true);
                    Alarm alarm = DB.getAlarm(-1);
                    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), 24 * 60 * 60 * 1000, alarm.generatePI(MainActivity.this));
                    mViewPager.setCurrentItem(0);
                }
            });
            buttonCancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.finish();
                }
            });
            buttonDelete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.finish();
                }
            });
            return rootView;
        }
    }

    public class QuotesSectionFragment extends Fragment {
        public ListView listView;
        public Button button;
        public ArrayList<String> quotes;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_quotes, container, false);
            listView = (ListView) rootView.findViewById(R.id.quotes_listView1);
            quotes = MainActivity.DB.getQuotesRef();

            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.DB.getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, quotes);

            //Code for the New Quote button
            button = (Button) rootView.findViewById(R.id.quotes_button1);
            button.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    final EditText editText = new EditText(MainActivity.DB.getContext());
                    int maxLength = 50;
                    editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
                    new AlertDialog.Builder(MainActivity.DB.getContext())
                            .setTitle("New Quote")
                            .setMessage("Enter your quote")
                            .setView(editText)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Editable value = editText.getText();
                                    MainActivity.DB.addNewQuote(value.toString());
                                    adapter.notifyDataSetChanged();
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    }).show();

                }
            });

            //Code for quote long press
            listView.setOnItemLongClickListener(new OnItemLongClickListener() {

                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    final EditText editText = new EditText(MainActivity.DB.getContext());
                    int maxLength = 50;
                    editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
                    editText.setText(quotes.get(position));
                    new AlertDialog.Builder(MainActivity.DB.getContext())
                            .setTitle("Update Quote")
                            .setMessage("Enter your quote")
                            .setView(editText)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Editable value = editText.getText();
                                    MainActivity.DB.editQuote(position, value.toString());
                                    adapter.notifyDataSetChanged();
                                }
                            }).setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            MainActivity.DB.removeQuote(position);
                            adapter.notifyDataSetChanged();
                        }
                    }).setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Do nothing.
                        }
                    }).show();
                    return false;
                }
            });


            // Assign adapter to ListView
            listView.setAdapter(adapter);

            return rootView;
        }
    }

    public class DummySectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
            TextView dummyTextView = (TextView) rootView
                    .findViewById(R.id.section_label);
            dummyTextView.setText(Integer.toString(getArguments().getInt(
                    ARG_SECTION_NUMBER)) + "Hello world");
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            Fragment fragment = new DummySectionFragment();
            switch (position) {
                case 0:
                    fragment = new AlarmsSectionFragment();
                    break;
                case 1:
                    fragment = new SetAlarmSectionFragment();
                    break;
                case 2:
                    fragment = new QuotesSectionFragment();
                    break;
                case 3:
                    fragment = new DummySectionFragment();
                    break;
            }
            Bundle args = new Bundle();
            args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
                case 3:
                    return getString(R.string.title_section4).toUpperCase(l);
            }
            return null;
        }
    }


}
