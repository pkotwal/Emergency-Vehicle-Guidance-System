package com.pratik.locationtest;

import android.content.Context;

/**
 * Created by Pratik on 17-02-2017.
 */

public class ProgressDialog {
    Context context;
    android.app.ProgressDialog dialog;

    public ProgressDialog(Context context) {
        this.context = context;
    }

    public void displayDialog(String message) {
        dialog = new android.app.ProgressDialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage(message);
        dialog.show();
    }

    public void changeDialogMessage(String message) {
        if (dialog.isShowing()) {
            dialog.setMessage(message);
        }
    }

    public void dismissDialog() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}

