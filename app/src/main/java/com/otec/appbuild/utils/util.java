package com.otec.appbuild.utils;

import android.content.Context;
import android.widget.Toast;

public class util {
    public   void  message(String text, Context context){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();

    }
}
