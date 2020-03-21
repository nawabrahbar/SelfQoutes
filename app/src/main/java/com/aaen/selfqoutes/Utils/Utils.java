package com.aaen.selfqoutes.Utils;

import android.content.Context;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.aaen.selfqoutes.R;
import com.google.android.material.snackbar.Snackbar;

public class Utils {

    public static void showSnackBar(View view, String message, Context context) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.choose_button_text));
        snackbar.show();
    }
}
