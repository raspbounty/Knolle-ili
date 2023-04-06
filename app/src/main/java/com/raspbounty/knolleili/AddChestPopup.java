package com.raspbounty.knolleili;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AddChestPopup extends DialogFragment {
   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState) {
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.popupTheme);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
         builder.setView(R.layout.popup_window);
      }
      return builder.create();
   }


   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      return inflater.inflate(R.layout.popup_window, container, false);

   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);


      Log.d("popup", "on view created");


      final EditText editText = view.findViewById(R.id.et_content);
      Log.d("popup", Integer.toString(editText.getId()));
      Button btnDone = view.findViewById(R.id.popupCreateBtn);
      btnDone.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            if(TextUtils.isEmpty(editText.getText())) {
               Log.d("popup", "is empty");
               //DialogListener dialogListener = (DialogListener) getActivity();
               //dialogListener.onFinishEditDialog(editText.getText().toString());
            }else {
               Log.d("popup", "is not empty");
               dismiss();
            }
         }
      });
      btnDone.setText("bla");

      Button btnCancel = view.findViewById(R.id.popupCancelButton);
      btnCancel.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Log.d("popup", "cancelled");
            dismiss();
         }
      });

   }

   @Override
   public void onResume() {
      super.onResume();

   }
}
/*
   View iewv = getLayoutInflater().inflate(R.layout.popup_window, null);
   //Toast.makeText(ctx, "You clicked add", Toast.LENGTH_SHORT).show();
   androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this, R.style.popupTheme);
        builder.setView(iewv);


                iewv.findViewById(R.id.popupCancelButton).setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View view) {
final EditText etContent, etRoom, etX, etY, etRack;
        etContent = iewv.findViewById(R.id.et_content);
        etRoom = iewv.findViewById(R.id.et_room);
        etX = iewv.findViewById(R.id.et_x);
        etY = iewv.findViewById(R.id.et_y);
        etRack = iewv.findViewById(R.id.et_rack);

        if (TextUtils.isEmpty(etContent.getText())) {
        Toast.makeText(ctx, "content empts", Toast.LENGTH_SHORT).show();
        etContent.setError(getString(R.string.all_emptyinput, getString(R.string.all_content)));
        } else if (TextUtils.isEmpty(etRoom.getText())) {
        etRoom.setError(getString(R.string.all_emptyinput, getString(R.string.all_room)));
        } else if (TextUtils.isEmpty(etX.getText())) {
        etX.setError(getString(R.string.all_emptyinput, getString(R.string.all_X)));
        } else if (TextUtils.isEmpty(etY.getText())) {
        etY.setError(getString(R.string.all_emptyinput, getString(R.string.all_Y)));
        } else if (TextUtils.isEmpty(etRack.getText())) {
        etRack.setError(getString(R.string.all_emptyinput, getString(R.string.all_rack)));
        } else {

        }
        }
        });
        iewv.findViewById(R.id.popupCancelButton).setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View view) {
        }
        });


        builder.setMessage("Create chest");


        // Create the AlertDialog
        //AlertDialog dialog = builder.create();
        builder.show();*/