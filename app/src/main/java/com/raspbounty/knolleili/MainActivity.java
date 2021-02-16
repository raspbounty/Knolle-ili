package com.raspbounty.knolleili;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener{
    private AutoCompleteTextView input;
    //private TableLayout resultTable;
    private RecyclerView rvTable;
    private Context ctx;
    private Activity activity;
    private ImageView resultImage;
    private HashMap<String, String> roomMap, rackMap;
    private TextView roomName;
    private ArrayList<Chest> resultChests;
    private int clickableChestID, knolleIconId;
    private Menu mOptionsMenu;
    private int[] knolleIcons;
    private boolean allShowing, backWasShown, connection;
    private JSONArray localJSONArray;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private MyRecyclerViewAdapter rvAdapter;
    private HashMap<String, int[]> shelfMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        input = findViewById(R.id.input);
        //resultTable = findViewById(R.id.resultTable);
        rvTable = findViewById(R.id.rvOutput);
        resultImage = findViewById(R.id.resultImage);
        roomName = findViewById(R.id.roomName);
        ctx = getApplicationContext();
        activity = MainActivity.this;

        sharedPref = this.getSharedPreferences("myPrefs", MODE_PRIVATE);
        connection = sharedPref.getBoolean("connection", true);
        editor = sharedPref.edit();
        getLocalJSON();
        allShowing = false;
        backWasShown = false;

        knolleIcons = new int[7];
        knolleIcons[0] = R.mipmap.ic_weihnachts_knolle;
        knolleIcons[1] = R.mipmap.ic_frau_antje_knolle;
        knolleIcons[2] = R.mipmap.ic_jubilaeums_knolle;
        knolleIcons[3] = R.mipmap.ic_knolle;
        knolleIcons[4] = R.mipmap.ic_piraten_knolle;
        knolleIcons[5] = R.mipmap.ic_wikinger_knolle;
        knolleIcons[6] = R.mipmap.ic_post_knolle;

        setupLayout();

        clearAll();
        createMaps();

        performRead();

/*
        Boolean showCasePlayed = sharedPref.getBoolean("showcasePlayed", false);
        if(!showCasePlayed) {
            createTour();
            editor.putBoolean("showcasePlayed", true);
            editor.apply();
        }
*/
        //showNewFeature();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Random rand = new Random();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        mOptionsMenu = menu;

        knolleIconId = rand.nextInt((knolleIcons.length));
        mOptionsMenu.findItem(R.id.knolleIcon).setIcon(knolleIcons[knolleIconId]);

        if(connection){
            mOptionsMenu.findItem(R.id.connection).setIcon(R.drawable.ic_connection_enabled);
        }else{
            mOptionsMenu.findItem(R.id.connection).setIcon(R.drawable.ic_connection_disabled);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor.putBoolean("connection", connection);
        editor.apply();
    }

    public void createMaps(){
        roomMap = new HashMap<>();
        roomMap.put("M", getString(R.string.all_m));
        roomMap.put("K", getString(R.string.all_k));
        roomMap.put("W", getString(R.string.all_w));
        roomMap.put("D", getString(R.string.all_d));
        roomMap.put("P", getString(R.string.all_p));

        rackMap = new HashMap<>();
        rackMap.put("R1", "Regal 1");
        rackMap.put("S1", "Schrank 1");
        rackMap.put("S2", "Schrank 2");
        rackMap.put("S3", "Schrank 3");
        rackMap.put("W1", "Würfel 1");
        rackMap.put("W2", "Würfel 2");
        rackMap.put("T1", "T 1");

        shelfMap = new HashMap<>();
        shelfMap.put("KR1", new int[]{6, 7});
        shelfMap.put("KS1", new int[]{4, 2});
        shelfMap.put("KS2", new int[]{2, 4});
        shelfMap.put("KS3", new int[]{6, 2});
        shelfMap.put("KW1", new int[]{3, 3});
        shelfMap.put("KW2", new int[]{1, 3});
        shelfMap.put("KT1", new int[]{1, 4});
        shelfMap.put("WR1", new int[]{4, 6});
        shelfMap.put("DS1", new int[]{1, 2});
        shelfMap.put("PR1", new int[]{5, 5});
    }

    private void setupLayout(){
        LinearLayoutManager lm = new LinearLayoutManager(ctx);
        rvTable.setHasFixedSize(true);
        rvTable.setLayoutManager(lm);

        // Get the string array
        String[] countries = getResources().getStringArray(R.array.content_array);
        // Create the adapter and set it to the AutoCompleteTextView
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, countries);
        input.setAdapter(adapter);

        //add an listener for the click on the done button
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    performSearch();

                    //hide the soft keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    if(imm != null) {
                        imm.hideSoftInputFromWindow(input.getApplicationWindowToken(), 0);
                    }

                    //hide dropdown
                    input.dismissDropDown();

                    handled = true;
                }
                return handled;
            }
        });

        input.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //input.setText(parent.getItemAtPosition(position).toString());
                performSearch();
                input.dismissDropDown();
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                if(imm != null) {
                    imm.hideSoftInputFromWindow(input.getApplicationWindowToken(), 0);
                }
            }
        });
    }

    private int getPixelColor(int x, int y){
        resultImage.setDrawingCacheEnabled(true);
        Bitmap hotspots = Bitmap.createBitmap(resultImage.getDrawingCache());
        resultImage.setDrawingCacheEnabled(false);
        return hotspots.getPixel(x, y);
    }
/*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //check if any chest is selected
        if(clickableChestID != -1) {
            int[] viewCoords;
            int imageX2, imageY2;
            float touchX, touchY;
            Chest resultChest;
            Intent intent;

            viewCoords = new int[2];
            resultImage.getLocationOnScreen(viewCoords);

            touchX = event.getX();
            touchY = event.getY();

            imageX2 = (int) ((touchX - viewCoords[0]));
            imageY2 = (int) ((touchY - viewCoords[1]));

            //check if clicked element is resultImage
            if((imageX2>0 && imageX2<resultImage.getWidth()) && (imageY2>0 && imageY2<resultImage.getHeight())) {
                //check if clicked pixel is blue (in rgb 30116253, in Color -14781187)
                if(getPixelColor(imageX2, imageY2) == -14781187) {
                    resultChest = resultChests.get(clickableChestID);
                    intent = new Intent(ctx, rackPopupActivity.class);
                    intent.putExtra("rackName", resultChest.shelfShort);
                    intent.putExtra("roomName", resultChest.roomShort);
                    intent.putExtra("rackX", resultChest.coords[0]);
                    intent.putExtra("rackY", resultChest.coords[0]);

                    activity.startActivity(intent);
                }
            }
        }
        return true;
    }*/

    private void visualizeChest(Chest chest){
        String rack, imageName;
        int imageId;

        imageName = chest.getImageName();
        imageId = getResources().getIdentifier(imageName, "drawable", getPackageName());

        resultImage.setImageResource(imageId);
        roomName.setText(chest.roomLong);

        //clickableChestID = chestId;
    }

    private void clearAll(){
        resultImage.setImageDrawable(null);
        roomName.setText("");
        resultChests = new ArrayList<>();
        //clickableChestID = -1;

        //while (resultTable.getChildCount() > 1)
        //    resultTable.removeView(resultTable.getChildAt(resultTable.getChildCount() - 1));
    }

    private void showNewFeature(){
        try {
            int versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            if (sharedPref.getInt("lastUpdate", 0) != versionCode) {
                // Commiting in the preferences, that the update was successful.
                editor.putInt("lastUpdate", versionCode);
                editor.apply();

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                // set title
                alertDialogBuilder.setTitle(getString(R.string.all_update_note));

                // set dialog message
                alertDialogBuilder
                        .setMessage(getString(R.string.tour_connection))
                        .setCancelable(false)
                        .setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // if this button is clicked, close
                                // current activity
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        }catch(Exception e){
            Log.d("error", e.toString()+"in showNewFeature");
        }
    }

    private void processData(int mode, JSONObject[] resultArray){
        //ArrayList<Chest> chests = new ArrayList<>();

        try {
            int count, length;
            count = 0;
            length = resultArray.length;
            ArrayList<String> resultChest;
            if(length == 0 && mode == 1){
                Toast.makeText(ctx, R.string.all_noresult, LENGTH_LONG).show();
            }else if(length  > 5 && mode == 1){
                Toast.makeText(ctx, R.string.all_toomanyresults, LENGTH_LONG).show();
            }else {
                if(mode == 1){
                    resultImage.setVisibility(View.VISIBLE);
                }else if(mode == 2){
                    resultImage.setVisibility(View.GONE);
                }
                /*
                for (JSONObject chest : resultArray) {
                    resultChest = new ArrayList<>();

                    resultChest.add(chest.getString("content"));
                    resultChest.add(chest.getString("room"));
                    resultChest.add(chest.getString("rack"));
                    resultChest.add(chest.getString("X"));
                    resultChest.add(chest.getString("Y"));

                    addToTable(resultChest, count);
                    resultChests.add(resultChest);
                    count++;
                }

                //if the result is exactly one chest, display it
                if (count == 1 && mode == 1) {
                    visualizeChest(0);
                }*/
                resultChests = new ArrayList<>();
                for (JSONObject chest : resultArray) {
                    resultChests.add(new Chest(chest.getString("content"), chest.getString("room"), chest.getString("rack"), chest.getString("X"), chest.getString("Y"), roomMap.get(chest.getString("room")), rackMap.get(chest.getString("rack"))));
                }
                rvAdapter = new MyRecyclerViewAdapter(ctx, resultChests);
                //sets in this file implemented clickListener for each row
                rvAdapter.setClickListener(this);
                rvTable.setAdapter(rvAdapter);
                rvAdapter.notifyDataSetChanged();
            }
        }catch (Exception e){
            Toast.makeText(ctx, e.toString() + " in processData", LENGTH_LONG).show();
            Log.d("error", e.toString() + " in processData");
        }
    }
/*
    private void addToTable(ArrayList<String> chest, int tag){
        TableRow tr = new TableRow(ctx);

        for(int i = 0; i < 5; i++) {
            TextView tv = new TextView(ctx);
            LinearLayout ll = new LinearLayout(ctx);

            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT));

            if(i == 1){
                tv.setText(roomMap.get(chest.get(i)));
            }else {
                tv.setText(chest.get(i));
            }
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onTableClick(v);
                }
            });

            tv.setTag(tag);
            if(Build.VERSION.SDK_INT > 16)
                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv.setBackgroundResource(R.drawable.back);
            tv.setGravity(Gravity.CENTER);
            tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

            ll.addView(tv);
            tr.addView(ll);
        }
        resultTable.addView(tr);
    }
*/
    private void jsonRequest(final int mode){
        String url = "";
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String apikey = "jGd6AvfshUzd9mqSG2wGy7oX5SSu5VLu";

        if(mode == 1) {
            url = String.format("https://api.psg-knolle.de/product/search.php?s=%s&apikey=%s", input.getText(), apikey);
        }else if(mode == 2){
            url = String.format("https://api.psg-knolle.de/product/read.php?apikey=%s", apikey);
        }
        final ProgressDialog dialog = ProgressDialog.show(this, null, "Please Wait");
        // Request a string response from the provided URL.
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (com.android.volley.Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                dialog.dismiss();
                                try {
                                    JSONArray arrJson = response.getJSONArray("results");
                                    JSONObject[] resultArray = new JSONObject[arrJson.length()];
                                    for (int i = 0; i < arrJson.length(); i++) {
                                        resultArray[i] = arrJson.getJSONObject(i);
                                    }
                                    if(mode == 2){
                                        localJSONArray = arrJson;
                                    }
                                    clearAll();
                                    processData(mode, resultArray);
                                }catch(JSONException e){
                                    Toast.makeText(ctx, R.string.all_notFound, LENGTH_LONG).show();
                                    Log.d("error", e.toString());
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        dialog.dismiss();
                        Log.d("error", volleyError.toString());
                        Toast.makeText(ctx, R.string.all_networkError, LENGTH_LONG).show();
                    }
                });
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
        //queue.start();
    }

    private boolean getLocalJSON(){
        if(localJSONArray == null){
            String jsonArrayString = sharedPref.getString("jsonArray","");
            if(!jsonArrayString.equals("")) {
                try{
                    localJSONArray = new JSONArray(jsonArrayString);
                    return true;
                }catch(JSONException e){
                    Toast.makeText(ctx, R.string.all_noLocalJson, LENGTH_LONG).show();
                    Log.d("error", e.toString() + " in getLocalJSON");
                    return false;
                }
            }
            return false;
        }
        return true;
    }

    private void retrieveFromLocal(int mode){
        JSONObject[] resultArray = null;
        ArrayList<JSONObject> resultList = new ArrayList<>();
        String searchString = input.getText().toString();
        try{
            if(localJSONArray!=null) {
                //convert localJSONArray to type JSONObject[]
                JSONObject[] localJSONObject = new JSONObject[localJSONArray.length()];
                for (int i = 0; i < localJSONArray.length(); i++) {
                    localJSONObject[i] = localJSONArray.getJSONObject(i);
                }

                if (mode == 1) {
                    for (JSONObject chest : localJSONObject) {
                        if (chest.getString("content").toLowerCase().contains(searchString.toLowerCase())) {
                            //Log.d("error", chest.getString("content"));
                            resultList.add(chest);
                        }
                        //convert the result List to the result Array
                        resultArray = new JSONObject[resultList.size()];
                        resultList.toArray(resultArray);
                    }
                } else if (mode == 2) {
                    resultArray = localJSONObject;
                }
            }else{

                //ToDo handle this
            }
        }catch(JSONException je){
            Log.d("error", je.toString() + " in retrieveFromLocal");
        }
        if(resultArray != null) {
            clearAll();
            processData(mode, resultArray);
        }else{
            Toast.makeText(ctx, R.string.all_notFound, LENGTH_LONG).show();
        }
    }

    private void saveLocalCopy(){
        if(localJSONArray!=null) {
            editor.putString("jsonArray", localJSONArray.toString());
            editor.apply();
        }
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        view.clearFocus();
    }
/*
    public void onTableClick(View v){
        if(allShowing){
            backWasShown = true;
        }
        int tag = (int) v.getTag();
        if (resultImage.getVisibility() == View.GONE) {
            ArrayList<String> resultChest = resultChests.get(tag);
            clearAll();
            tag = 0;
            resultChests.add(resultChest);
            addToTable(resultChest, tag);
            resultImage.setVisibility(View.VISIBLE);
        }
        visualizeChest(tag);

        markTableRow(tag);
    }*/
/*
    private void markTableRow(int tag){
        TableRow tr;
        LinearLayout ll;
        for(int j = 1; j < resultTable.getChildCount(); j++) {
            tr = (TableRow)resultTable.getChildAt(j);
            for (int i = 0; i < tr.getChildCount(); i++) {
                ll = (LinearLayout) tr.getChildAt(i);
                if(j == tag+1) {
                    ll.getChildAt(0).setBackgroundResource(R.drawable.back_marked);
                }else{
                    ll.getChildAt(0).setBackgroundResource(R.drawable.back);
                }
            }
        }
    }*/

    private void createTour(){
        input.setText(getString(R.string.tour_gips));
        performSearch();
        Button searchBtn = findViewById(R.id.searchBtn);
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this);

        sequence.setConfig(config);

        sequence.addSequenceItem(input,
                getString(R.string.tour_searchbar), getString(R.string.all_got));

        sequence.addSequenceItem(searchBtn,
                getString(R.string.tour_searchbutton), getString(R.string.all_got));

        //sequence.addSequenceItem(resultTable,
        //        getString(R.string.tour_table), getString(R.string.all_got));

        sequence.addSequenceItem(resultImage,
                getString(R.string.tour_image), getString(R.string.all_got));

        sequence.addSequenceItem(findViewById(R.id.readBtn),
                getString(R.string.tour_readbutton), getString(R.string.all_got));

        sequence.start();
    }

    public void readClick(View v){
        performRead();
        hideKeyboardFrom(ctx, v);
    }

    public void searchClick(View v){
        performSearch();
        hideKeyboardFrom(ctx, v);
    }

    public void clearInput(View v){
        input.setText("");
    }

    private void performSearch(){
        if(connection) {
            jsonRequest(1);
        }else{
            retrieveFromLocal(1);
        }
    }

    private void performRead(){
        if(connection) {
            jsonRequest(2);
        }else{
            retrieveFromLocal(2);
        }
        allShowing = true;
    }

    private void toggleConnection(){
        if(connection){
            Boolean noError;
            noError = getLocalJSON();
            if(!noError){
                Toast.makeText(ctx, getString(R.string.all_noLocalJson), LENGTH_LONG).show();
            }else{
                mOptionsMenu.findItem(R.id.connection).setIcon(R.drawable.ic_connection_disabled);
                connection = false;
            }
        }else{
            connection = true;
            saveLocalCopy();
            mOptionsMenu.findItem(R.id.connection).setIcon(R.drawable.ic_connection_enabled);
        }

        editor.putBoolean("connection", connection);
        editor.apply();
    }

    private void showRoomImage(Chest chest, int pos){
        final ImageView ivRoom;
        final TextView tvTitle;
        final Button btnClose;
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(ctx);
        final View promptsView = li.inflate(R.layout.activity_image_popup, null);

        final PopupWindow pw = new PopupWindow(new ContextThemeWrapper(MainActivity.this, R.style.popupTheme));

        ivRoom  = promptsView.findViewById(R.id.iv_room);
        tvTitle = promptsView.findViewById(R.id.tv_img_title);
        int imageId = getResources().getIdentifier(chest.getImageName(), "drawable", getPackageName());

        ivRoom.setImageResource(imageId);

        pw.setContentView(promptsView);

        tvTitle.setText(getString(R.string.all_location_concat, chest.roomLong, chest.shelfLong));
        btnClose = promptsView.findViewById(R.id.btn_img_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pw.dismiss();
            }
        });

        pw.setOutsideTouchable(true);
        pw.setFocusable(true);
        pw.showAtLocation(this.rvTable.getChildAt(pos), Gravity.CENTER, 0, 0);
    }

    private void showShelf(Chest chest, int pos){
        TextView tv;
        final TextView tvTitle;
        final Button btnClose;
        int rowCount, colCount, x ,y;
        LayoutInflater li = LayoutInflater.from(ctx);
        View promptsView = li.inflate(R.layout.activity_shelf_popup, null);

        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.popupTheme));

        final PopupWindow pw = new PopupWindow(new ContextThemeWrapper(MainActivity.this, R.style.popupTheme));

        // set prompts.xml to alertdialog builder
        //alertDialogBuilder.setView(promptsView);
        pw.setContentView(promptsView);

        final TableLayout tlShelf = promptsView.findViewById(R.id.tl_shelf);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            rowCount = Objects.requireNonNull(shelfMap.get(chest.locationToString()), "shelf in the specific room not found")[0];
            colCount = Objects.requireNonNull(shelfMap.get(chest.locationToString()), "shelf in the specific room not found")[1];
        }else{
            rowCount = shelfMap.get(chest.locationToString())[0];
            colCount = shelfMap.get(chest.locationToString())[1];
        }
        x = chest.coords[0];
        y = chest.coords[1];

        //add top row
        TableRow header = new TableRow(ctx);
        TextView topLeft = new TextView(ctx);
        topLeft.setText("");
        if(Build.VERSION.SDK_INT > 16)
            topLeft.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        topLeft.setTextSize(18);
        header.addView(topLeft);
        for(int col = 1; col < colCount; col ++){
            tv = new TextView(ctx);
            tv.setText(String.valueOf(col));
            if(Build.VERSION.SDK_INT > 16)
                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv.setTextSize(18);
            header.addView(tv);
        }
        tlShelf.addView(header);

        for(int row = rowCount; row >= 0; row--){
            TableRow tr = new TableRow(ctx);
            for(int col = 0; col < colCount; col ++){
                tv = new TextView(ctx);
                if(col == 0){
                    //left column
                    tv.setText(String.valueOf(row));
                }else {
                    if (row == y && col == x) {
                        tv.setText(chest.coordsToString());
                    } else {
                        tv.setText("");
                    }
                    tv.setBackgroundResource(R.drawable.back);
                }
                if(Build.VERSION.SDK_INT > 16)
                    tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                tv.setTextSize(18);
                tr.addView(tv);
            }
            tlShelf.addView(tr);
        }


        // set dialog message
        /*
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.dismiss();
                            }
                        })
        .setTitle(chest.shelfLong);
*/
        // create alert dialog
        //AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        //alertDialog.show();
        tvTitle = promptsView.findViewById(R.id.tv_shelf_title);
        tvTitle.setText(getString(R.string.all_location_concat, chest.roomLong, chest.shelfLong));
        btnClose = promptsView.findViewById(R.id.btn_shelf_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pw.dismiss();
            }
        });

        pw.setOutsideTouchable(true);
        pw.setFocusable(true);
        pw.showAtLocation(this.rvTable.getChildAt(pos), Gravity.CENTER, 0, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Random rand = new Random();
        int newId = rand.nextInt((knolleIcons.length));
        while(newId == knolleIconId){
            newId = rand.nextInt((knolleIcons.length));
        }
        knolleIconId = newId;
        // Handle item selection
        if(item.getItemId() == R.id.knolleIcon){
            mOptionsMenu.findItem(R.id.knolleIcon).setIcon(knolleIcons[knolleIconId]);
            return true;
        }else if(item.getItemId() == R.id.connection){
            toggleConnection();
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(backWasShown){
            backWasShown = false;
            performRead();
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        //Toast.makeText(ctx, "You clicked " + rvAdapter.getItem(position).content + " on row number " + position, Toast.LENGTH_SHORT).show();
        //Toast.makeText(ctx, "You clicked " + view.getId(), Toast.LENGTH_SHORT).show();

        if(view.getId() == R.id.ll_roomshelf || view.getId() == R.id.tv_storage || view.getId() == R.id.tv_shelf) {
            //Toast.makeText(ctx, "You clicked " + view.getId() + " untilTv", Toast.LENGTH_SHORT).show();
            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(timetableURL+rvAdapter.getItem(position).timetableID)));
            showRoomImage(resultChests.get(position), position);
        }else if(view.getId()== R.id.tv_coords){
                //Toast.makeText(ctx, "You clicked " + view.getId() + " occupiedTv", Toast.LENGTH_SHORT).show();
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(timetableURL+rvAdapter.getItem(position).timetableID)));
                showShelf(resultChests.get(position), position);

        }

    }
}
