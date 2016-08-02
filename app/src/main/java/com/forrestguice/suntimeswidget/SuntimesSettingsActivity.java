/**
    Copyright (C) 2014 Forrest Guice
    This file is part of SuntimesWidget.

    SuntimesWidget is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesWidget is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/ 

package com.forrestguice.suntimeswidget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.getfix.ClearPlacesTask;
import com.forrestguice.suntimeswidget.getfix.ExportPlacesTask;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.List;
import java.util.Locale;

/**
 * A preferences activity for the main app;
 * @see SuntimesConfigActivity for widget configuration.
 */
public class SuntimesSettingsActivity extends PreferenceActivity
{
    final static String ACTION_PREFS_GENERAL = "com.forrestguice.suntimeswidget.PREFS_GENERAL";
    final static String ACTION_PREFS_LOCALE = "com.forrestguice.suntimeswidget.PREFS_LOCALE";
    final static String ACTION_PREFS_UI = "com.forrestguice.suntimeswidget.PREFS_UI";
    final static String ACTION_PREFS_WIDGETLIST = "com.forrestguice.suntimeswidget.PREFS_WIDGETLIST";
    final static String ACTION_PREFS_PLACES = "com.forrestguice.suntimeswidget.PREFS_PLACES";

    private Context context;

    public SuntimesSettingsActivity()
    {
        super();
    }

    /**
     * OnCreate: the Activity initially created
     * @param icicle
     */
    @Override
    public void onCreate(Bundle icicle)
    {
        setTheme(AppSettings.loadTheme(this));
        super.onCreate(icicle);

        String action = getIntent().getAction();
        if (action != null && action.equals(ACTION_PREFS_GENERAL))
        {
            addPreferencesFromResource(R.xml.preference_general);

        } else if (action != null && action.equals(ACTION_PREFS_LOCALE)) {
            addPreferencesFromResource(R.xml.preference_locale);

        } else if (action != null && action.equals(ACTION_PREFS_UI)) {
            addPreferencesFromResource(R.xml.preference_userinterface);

        } else if (action != null && action.equals(ACTION_PREFS_PLACES)) {
            addPreferencesFromResource(R.xml.preference_places);

        } else if (action != null && action.equals(ACTION_PREFS_WIDGETLIST)) {
            // TODO

        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            addPreferencesFromResource(R.xml.preference_headers_legacy);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        AppSettings.initLocale(this);
        initDisplayStrings();
    }

    private void initDisplayStrings()
    {
        context = SuntimesSettingsActivity.this;
        AppSettings.initDisplayStrings(context);
        WidgetSettings.initDisplayStrings(context);
    }

    /**
     * @param target
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            loadHeadersFromResource(R.xml.preference_headers, target);

            TypedValue typedValue = new TypedValue();   // force styled icons on headers
            int[] icActionAttr = new int[] { R.attr.icActionSettings };
            TypedArray a = obtainStyledAttributes(typedValue.data, icActionAttr);
            int settingsIcon = a.getResourceId(0, R.drawable.ic_action_settings);
            a.recycle();

            for (Header header : target)
            {
                if (header.iconRes == 0)
                {
                    header.iconRes = settingsIcon;
                }
            }
        }
    }

    /**
     * more fragment related bullshit
     * @param fragmentName
     * @return
     */
    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return GeneralPrefsFragment.class.getName().equals(fragmentName) ||
               LocalePrefsFragment.class.getName().equals(fragmentName) ||
               UIPrefsFragment.class.getName().equals(fragmentName) ||
               PlacesPrefsFragment.class.getName().equals(fragmentName);
    }

    public static final String DIALOGTAG_CLEARPLACES = "clearplaces";

    /**
     * OnStart: the Activity becomes visible
     */
    /**@Override
    public void onStart()
    {
    super.onStart();
    Context context = SuntimesSettingsActivity.this;
    }*/

    /**
     * OnResume: the user is now interacting w/ the Activity (running state)
     */
    /**@Override
    public void onResume()
    {
    super.onResume();
    }*/

    /**
     * OnPause: the user about to interact w/ another Activity
     */
    /**@Override
    public void onPause()
    {
    super.onPause();
    }*/


    /**
     * OnStop: the Activity no longer visible
     */
    /**@Override
    public void onStop()
    {
    super.onStop();
    }*/

    /**
     * OnDestroy: the activity destroyed
     */
    /**@Override
    public void onDestroy()
    {
    super.onDestroy();
    }*/

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    /**
     * General Prefs
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPrefsFragment extends PreferenceFragment
    {
        private ListPreference calculatorPref;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            Log.i("GeneralPrefsFragment", "Arguments: " + getArguments());

            PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_general, false);
            addPreferencesFromResource(R.xml.preference_general);
            initGeneral();
        }

        private void initGeneral()
        {
            SuntimesCalculatorDescriptor[] calculators = SuntimesCalculatorDescriptor.values();
            String[] calculatorEntries = new String[calculators.length];
            String[] calculatorValues = new String[calculators.length];

            int i = 0;
            for (SuntimesCalculatorDescriptor calculator : calculators)
            {
                calculatorEntries[i] = calculatorValues[i] = calculator.name();
                i++;
            }

            calculatorPref = (ListPreference) findPreference("appwidget_0_general_calculator");
            calculatorPref.setEntries(calculatorEntries);
            calculatorPref.setEntryValues(calculatorValues);

            loadGeneral(getActivity());
        }

        private void loadGeneral(Context context)
        {
            if (context != null && calculatorPref != null)
            {
                SuntimesCalculatorDescriptor currentMode = WidgetSettings.loadCalculatorModePref(context, 0);
                int currentIndex = calculatorPref.findIndexOfValue(currentMode.name());
                calculatorPref.setValueIndex(currentIndex);
                //Log.d("SuntimesSettings", "current mode: " + currentMode + " (" + currentIndex + ")");
            }
        }

        @Override
        @TargetApi(23)
        public void onAttach(Context context)
        {
            super.onAttach(context);
            loadGeneral(context);
        }

        @Override
        public void onAttach(Activity activity)
        {
            super.onAttach(activity);
            loadGeneral(activity);
        }
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    /**
     * Locale Prefs
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class LocalePrefsFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            Log.i("LocalePrefsFragment", "Arguments: " + getArguments());

            PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_locale, false);
            addPreferencesFromResource(R.xml.preference_locale);
        }
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    /**
     * Places Prefs
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PlacesPrefsFragment extends PreferenceFragment
    {
        public static final String KEY_ISCLEARING = "isclearing";
        public static final String KEY_ISEXPORTING = "isexporting";

        private Context myParent;
        private ProgressDialog progress;

        private ClearPlacesTask clearPlacesTask = null;
        private boolean isClearing = false;

        private ExportPlacesTask exportPlacesTask = null;
        private boolean isExporting = false;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            Log.i("PlacesPrefsFragment", "Arguments: " + getArguments());
            setRetainInstance(true);

            PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_places, false);
            addPreferencesFromResource(R.xml.preference_places);

            Preference clearPlacesPref = (Preference)findPreference("places_clear");
            clearPlacesPref.setOnPreferenceClickListener(onClickClearPlaces);

            Preference exportPlacesPref = (Preference)findPreference("places_export");
            exportPlacesPref.setOnPreferenceClickListener(onClickExportPlaces);
        }

        private void showProgressClearing()
        {
            progress = ProgressDialog.show(myParent, getString(R.string.locationcleared_dialog_title),getString(R.string.locationcleared_dialog_message), true);
        }

        private void showProgressExporting()
        {
            progress = ProgressDialog.show(myParent, getString(R.string.locationexport_dialog_title), getString(R.string.locationexport_dialog_message), true);
        }

        private void dismissProgress()
        {
            if (progress != null && progress.isShowing())
            {
                progress.dismiss();
            }
        }

        /**
         * Export Places (click handler)
         */
        private Preference.OnPreferenceClickListener onClickExportPlaces = new Preference.OnPreferenceClickListener()
        {
            public boolean onPreferenceClick(Preference preference)
            {
                if (myParent != null)
                {
                    exportPlacesTask = new ExportPlacesTask(myParent, "SuntimesPlaces", true);
                    exportPlacesTask.setTaskListener(exportPlacesListener);
                    /**{
                        @Override
                        protected void onPostExecute(ExportResult results)
                        {
                            dismissProgress();

                            if (results.getResult())
                            {
                                if (usedExternalStorage)
                                {
                                    super.onPostExecute(results);
                                }

                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(results.getExportFile()));
                                shareIntent.setType("text/csv");
                                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.msg_export_to)));

                            } else {
                                super.onPostExecute(results);
                            }
                        }
                    };*/
                    exportPlacesTask.execute();
                    return true;
                }
                return false;
            }
        };

        /**
         * Export Places (task handler)
         */
        private ExportPlacesTask.TaskListener exportPlacesListener = new ExportPlacesTask.TaskListener()
        {
            @Override
            public void onStarted()
            {
                isExporting = true;
                showProgressExporting();
            }

            @Override
            public void onFinished(ExportPlacesTask.ExportResult results)
            {
                exportPlacesTask = null;
                isExporting = false;
                dismissProgress();

                if (results.getResult())
                {
                    String successMessage = getString(R.string.msg_export_success, results.getExportFile().getAbsolutePath());
                    Toast.makeText(myParent.getApplicationContext(), successMessage, Toast.LENGTH_LONG).show();

                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(results.getExportFile()));
                    shareIntent.setType("text/csv");
                    startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.msg_export_to)));

                } else {
                    String failureMessage = getString(R.string.msg_export_failure, results.getExportFile().getAbsolutePath());
                    Toast.makeText(myParent.getApplicationContext(), failureMessage, Toast.LENGTH_LONG).show();
                }
            }
        };

        /**
         * Clear Places (click handler)
         */
        private Preference.OnPreferenceClickListener onClickClearPlaces = new Preference.OnPreferenceClickListener()
        {
            public boolean onPreferenceClick(Preference preference)
            {
                if (myParent != null)
                {
                    AlertDialog.Builder confirm = new AlertDialog.Builder(myParent)
                            .setTitle(myParent.getString(R.string.locationclear_dialog_title))
                            .setMessage(myParent.getString(R.string.locationclear_dialog_message))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(myParent.getString(R.string.locationclear_dialog_ok), new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int whichButton)
                                {
                                    clearPlacesTask = new ClearPlacesTask(myParent);
                                    clearPlacesTask.setTaskListener(clearPlacesListener);
                                    clearPlacesTask.execute((Object[]) null);
                                }
                            })
                            .setNegativeButton(myParent.getString(R.string.locationclear_dialog_cancel), null);

                    confirm.show();
                    return true;
                }
                return false;
            }
        };

        /**
         * Clear Places (task handler)
         */
        private ClearPlacesTask.TaskListener clearPlacesListener = new ClearPlacesTask.TaskListener()
        {
            @Override
            public void onStarted()
            {
                isClearing = true;
                showProgressClearing();
            }

            @Override
            public void onFinished(Boolean result)
            {
                clearPlacesTask = null;
                isClearing = false;
                dismissProgress();
                Toast.makeText(myParent, getString(R.string.locationcleared_toast_success), Toast.LENGTH_LONG).show();
            }
        };

        @Override
        public void onStop()
        {
            super.onStop();

            if (isClearing && clearPlacesTask != null)
            {
                clearPlacesTask.pauseTask();
                clearPlacesTask.clearTaskListener();
            }

            if (isExporting && exportPlacesTask != null)
            {
                exportPlacesTask.pauseTask();
                exportPlacesTask.clearTaskListener();
            }

            dismissProgress();
        }

        @Override
        public void onResume()
        {
            super.onResume();
            if (isClearing && clearPlacesTask != null && clearPlacesTask.isPaused())
            {
                clearPlacesTask.setTaskListener(clearPlacesListener);
                showProgressClearing();
                clearPlacesTask.resumeTask();
            }

            if (isExporting && exportPlacesTask != null)
            {
                exportPlacesTask.setTaskListener(exportPlacesListener);
                showProgressExporting();
                exportPlacesTask.resumeTask();
            }
        }

        @Override
        @TargetApi(23)
        public void onAttach(Context context)
        {
            super.onAttach(context);
            myParent = context;
        }

        @Override
        public void onAttach(Activity activity)
        {
            super.onAttach(activity);
            myParent = activity;
        }
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    /**
     * User Interface Prefs
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class UIPrefsFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            Log.i("UIPrefsFragment", "Arguments: " + getArguments());

            PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_userinterface, false);
            addPreferencesFromResource(R.xml.preference_userinterface);
        }
    }

}
