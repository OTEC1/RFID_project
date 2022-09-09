package com.otec.appbuild.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.otec.appbuild.R;

public class util {
    public   void  message(String text, Context context){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }


    public void getDevice(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        double density = (double) displayMetrics.density;
        Log.d("TAG", "SERIAL: " + Build.SERIAL);
        Log.d("TAG", "MODEL: " + Build.MODEL);
        Log.d("TAG", "ID: " + Build.ID);
        Log.d("TAG", "Manufacture: " + Build.MANUFACTURER);
        Log.d("TAG", "brand: " + Build.BRAND);
        Log.d("TAG", "type: " + Build.TYPE);
        Log.d("TAG", "user: " + Build.USER);
        Log.d("TAG", "BASE: " + Build.VERSION_CODES.BASE);
        Log.d("TAG", "INCREMENTAL " + Build.VERSION.INCREMENTAL);
        Log.d("TAG", "SDK  " + Build.VERSION.SDK);
        Log.d("TAG", "BOARD: " + Build.BOARD);
        Log.d("TAG", "BRAND " + Build.BRAND);
        Log.d("TAG", "HOST " + Build.HOST);
        Log.d("TAG", "FINGERPRINT: " + Build.FINGERPRINT);
        Log.d("TAG", "Version Code: " + Build.VERSION.RELEASE);
        Log.d("TAG", "display metrics: " + density);
    }


    public void openFragment(Fragment fragment, String my_fragment, int a, AppCompatActivity context) {
        FragmentTransaction fragmentTransaction = context.getSupportFragmentManager().beginTransaction();
        reuse_fragment(fragmentTransaction, fragment, my_fragment, null,  R.id.frameLayout);
    }


    private void reuse_fragment(FragmentTransaction fragmentTransaction, Fragment fragment, String my_fragment, Bundle b, int frameLayout) {
        fragment.setArguments(b);
        fragmentTransaction.replace(frameLayout, fragment, my_fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
