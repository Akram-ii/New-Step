package com.example.newstep.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.example.newstep.R;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    String language = "en";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);


        if (savedInstanceState == null) {

            getChildFragmentManager().beginTransaction()
                    .replace(R.id.preference_fragment_container, new SettingsPreferenceFragment())
                    .commit();
        }

        if (getActivity() != null) {

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            language = preferences.getString("language_pref", "en");

        }



        setAppLocale(language);



        return rootView;

    }



    public static class SettingsPreferenceFragment extends androidx.preference.PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings, rootKey);
        }
    }




    private void setAppLocale(String language) {


        if (getContext() == null) return;

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);


        getContext().getResources().updateConfiguration(config, getContext().getResources().getDisplayMetrics());


        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }





}

