package com.example.misaka.opengl_ex;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.DialogFragment;

public class OpenImageDialogFragment extends DialogFragment{

    public interface OpenImageDialogCommunicator {
        void onUpdateOption(int which, String tag);
    }

    private OpenImageDialogCommunicator mOpenImageDialogCommunicator;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.pick_option).setItems(R.array.Options,
                (dialog, which) -> mOpenImageDialogCommunicator.onUpdateOption(which, getTag()));
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mOpenImageDialogCommunicator = (OpenImageDialogCommunicator) activity;
    }
}
