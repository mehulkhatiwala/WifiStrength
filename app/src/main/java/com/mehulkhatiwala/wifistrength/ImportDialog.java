package com.mehulkhatiwala.wifistrength;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

public class ImportDialog {

    Activity activity;
    AlertDialog.Builder builder;
    String detailProvider;

    public ImportDialog(Activity a, String detailProvader) {
        this.activity = a;
        this.detailProvider = detailProvader;
        builder = new AlertDialog.Builder(a);
    }

    public void showDialog() {

        builder.setTitle("wifi Provider Details");
        builder.setMessage(detailProvider);

        AlertDialog alert = builder.create();
        alert.show();
    }
}
