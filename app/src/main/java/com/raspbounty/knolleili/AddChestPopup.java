package com.raspbounty.knolleili;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;

public class AddChestPopup extends DialogFragment {
   private static Chest chest;
   private static int mode;
   public AddChestPopup(){
      //needs to be here empty
   }

   public static AddChestPopup newInstance(Chest mchest, int mmode){
      mode = mmode;
      chest = mchest;
      //final EditText etContent = view.findViewById(R.id.et_content);
      AddChestPopup frag = new AddChestPopup();
      //Bundle args = new Bundle();
      //args.putString("return", "etContent.toString()");
      //frag.setArguments(args);

      return frag;
   }

   public static AddChestPopup newInstance(int mmode){
      mode = mmode;
      //final EditText etContent = view.findViewById(R.id.et_content);
      AddChestPopup frag = new AddChestPopup();
      //Bundle args = new Bundle();
      //args.putString("return", "etContent.toString()");
      //frag.setArguments(args);

      return frag;
   }


   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      return inflater.inflate(R.layout.popup_window, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      final EditText etContent, etRoom, etX, etY, etRack;
      Context ctx = this.getContext();
      final AppCompatSpinner spRoom, spRack;

      Button btnDone = view.findViewById(R.id.popupCreateBtn);
      Button btnCancel = view.findViewById(R.id.popupCancelButton);
      spRoom = view.findViewById(R.id.spinner_room);
      spRack = view.findViewById(R.id.spinner_rack);
      etContent = view.findViewById(R.id.et_content);
      etX = view.findViewById(R.id.et_x);
      etY = view.findViewById(R.id.et_y);

      if (mode == 2) {
         etContent.setText(chest.content);

         spRoom.setEnabled(false);
         spRoom.setClickable(false);


         spRack.setEnabled(false);
         spRack.setClickable(false);

         etX.setEnabled(false);
         etX.setInputType(0);
         etX.setText(chest.coords[0]);

         etY.setEnabled(false);
         etY.setInputType(0);
         etY.setText(chest.coords[0]);
      }


      ArrayAdapter<CharSequence> adapterRoom = ArrayAdapter.createFromResource(ctx,
              R.array.room_array, android.R.layout.simple_spinner_item);
      // Specify the layout to use when the list of choices appears
      adapterRoom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      // Apply the adapter to the spinner
      spRoom.setAdapter(adapterRoom);
      spRoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            int arrayID = R.array.rack_array;
            switch (i) {
               //Materialkeller
               case 0:
                  arrayID = R.array.rack_array;
                  break;
               //Kömpchen
               case 1:
                  arrayID = R.array.k_array;
                  break;
               //Wappenraum
               case 2:
                  arrayID = R.array.w_array;
                  break;
               //Dachgiebel
               case 3:
                  arrayID = R.array.d_array;
                  break;
               //Kabuff
               case 4:
                  arrayID = R.array.p_array;
            }

            ArrayAdapter<CharSequence> adapterRack = ArrayAdapter.createFromResource(ctx,
                    arrayID, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
            adapterRack.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            spRack.setAdapter(adapterRack);
         }


         @Override
         public void onNothingSelected(AdapterView<?> adapterView) {

         }
      });


      ArrayAdapter<CharSequence> adapterRack = ArrayAdapter.createFromResource(this.getContext(),
              R.array.rack_array, android.R.layout.simple_spinner_item);
      // Specify the layout to use when the list of choices appears
      adapterRack.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      // Apply the adapter to the spinner
      spRack.setAdapter(adapterRack);

      if(mode == 2){
         int roomPos = adapterRoom.getPosition(chest.roomLong);
         int rackPos = adapterRoom.getPosition(chest.shelfLong);

         spRoom.setSelection(roomPos);
         spRack.setSelection(rackPos);
      }


      btnDone.setOnClickListener(view1 -> {
          if(TextUtils.isEmpty(etContent.getText())) {
            Toast.makeText(this.getContext(), getString(R.string.all_emptyinput, getString(R.string.all_content)), Toast.LENGTH_LONG).show();
         //}else if(TextUtils.isEmpty(etRoom.getText())) {
         //    Toast.makeText(this.getContext(), getString(R.string.all_emptyinput, getString(R.string.all_room)), Toast.LENGTH_LONG).show();
         //}else if(TextUtils.isEmpty(etRack.getText())) {
         //    Toast.makeText(this.getContext(), getString(R.string.all_emptyinput, getString(R.string.all_rack)), Toast.LENGTH_LONG).show();
         }else if(TextUtils.isEmpty(etX.getText())) {
            Toast.makeText(this.getContext(), getString(R.string.all_emptyinput, getString(R.string.all_X)), Toast.LENGTH_LONG).show();
         }else if(TextUtils.isEmpty(etY.getText())) {
            Toast.makeText(this.getContext(), getString(R.string.all_emptyinput, getString(R.string.all_Y)), Toast.LENGTH_LONG).show();
         }else{
            Bundle bd = new Bundle();
            //String[] chest = new String[]{etContent.getText().toString(), etRoom.getText().toString(), etRack.getText().toString(), etX.getText().toString(), etY.getText().toString()};
            String[] chest = new String[]{etContent.getText().toString(), spRoom.getSelectedItem().toString(), spRack.getSelectedItem().toString(), etX.getText().toString(), etY.getText().toString()};
            bd.putStringArray("newChest", chest);
            getParentFragmentManager().setFragmentResult("result", bd);
            dismiss();
         }
      });


      btnCancel.setOnClickListener(view12 -> {
         Log.d("popup", "cancelled");
         dismiss();
      });
   }
}