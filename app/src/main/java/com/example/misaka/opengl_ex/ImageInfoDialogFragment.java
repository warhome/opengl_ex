package com.example.misaka.opengl_ex;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class ImageInfoDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        // Get and configure custom layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.info_dialog,null);

        Bundle bundle = this.getArguments();
        ArrayList<String> data = bundle.getStringArrayList(MainActivity.SHOW_IMAGE_DATA_DIALOG_TAG);

        TextView heightContainer = view.findViewById(R.id.height_container);
        TextView widthContainer  = view.findViewById(R.id.width_container);
        TextView dateContainer   = view.findViewById(R.id.date_container);
        TextView timeContainer   = view.findViewById(R.id.time_container);

        heightContainer.setText(data != null ? data.get(0) : "Unknown");
        widthContainer.setText(data != null ? data.get(1) : "Unknown");
        dateContainer.setText(data != null ? data.get(2) : "Unknown");
        timeContainer.setText(data != null ? data.get(3) : "Unknown");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set custom layout
        builder.setView(view);

        return builder.create();
    }
}

