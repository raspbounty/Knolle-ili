package com.raspbounty.knolleili;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener{
    private AutoCompleteTextView input;
    private RecyclerView rvTable;
    private SwitchCompat swToggleEdit;
    private Context ctx;
    private String[] suggestContentList;
    private ArrayList<Chest> resultChests;
    private int knolleIconId;
    private Menu mOptionsMenu;
    private int[] knolleIcons;
    private boolean connection, needToUpdateSuggestions;
    private JSONArray localJSONArray;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private HashMap<String, int[]> shelfSizeMap;
    private final String stringDelimiter = ";;del;;";
    private int numberKnollen = 9;

    private int chestVisibility;

    final private String apikey = "jGd6AvfshUzd9mqSG2wGy7oX5SSu5VLu";

    //final private String baseURL = "https://api.psg-knolle.de/product/";
    final private String baseURL = "http://10.0.2.2/testKnolleApi/product/";
    private MyRecyclerViewAdapter rvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.coord_main);
        input = findViewById(R.id.input);
        rvTable = findViewById(R.id.rvOutput);

        ctx = getApplicationContext();

        sharedPref = this.getSharedPreferences("myPrefs", MODE_PRIVATE);
        connection = true;//sharedPref.getBoolean("connection", true);


        Calendar lastMonth = Calendar.getInstance();
        lastMonth.add(Calendar.MONTH, -1);
        long lastRead = sharedPref.getLong("lastread", 0);



        if(connection && lastRead < lastMonth.getTime().getTime()){
            performRead();
            Calendar now = Calendar.getInstance();
            editor = sharedPref.edit();
            editor.putLong("lastread", now.getTime().getTime());

            editor.apply();
            needToUpdateSuggestions=true;

        }else{
            needToUpdateSuggestions = false;
            getLocalJSON();

            String temp = sharedPref.getString("suggestContentList", "");
            suggestContentList = temp.split(stringDelimiter);

        }

        numberKnollen = 8;
        knolleIcons = new int[numberKnollen];
        knolleIcons[0] = R.drawable.ic_halloween_knolle;
        knolleIcons[1] = R.drawable.ic_frau_antje_knolle;
        knolleIcons[2] = R.drawable.ic_harry_knolle;
        knolleIcons[3] = R.drawable.ic_knolle;
        knolleIcons[4] = R.drawable.ic_piraten_knolle;
        knolleIcons[5] = R.drawable.ic_wikinger_knolle;
        knolleIcons[6] = R.drawable.ic_post_knolle;
        knolleIcons[7] = R.drawable.ic_harry_knolle;

        clearAll();
        setupLayout();


        createMaps();



        boolean showCasePlayed = sharedPref.getBoolean("showcasePlayed", false);
        if(!showCasePlayed) {
            editor = sharedPref.edit();
            editor.putBoolean("showcasePlayed", true);
            editor.apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Random rand = new Random();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        mOptionsMenu = menu;

        knolleIconId = rand.nextInt((knolleIcons.length));
        /*mOptionsMenu.findItem(R.id.knolleIcon).setIcon(knolleIcons[knolleIconId]);

        if(connection){
            mOptionsMenu.findItem(R.id.connection).setIcon(R.drawable.ic_connection_enabled);
        }else{
            mOptionsMenu.findItem(R.id.connection).setIcon(R.drawable.ic_connection_disabled);
        }*/

        MenuItem itemSwitch = mOptionsMenu.findItem(R.id.myswitch);
        itemSwitch.setActionView(R.layout.switch_layout);


        swToggleEdit = itemSwitch.getActionView().findViewById(R.id.switchForActionBar);
        boolean editState = sharedPref.getBoolean("editState", false);
        toggleEdit(editState);

        swToggleEdit.setChecked(editState);
        swToggleEdit.setOnCheckedChangeListener((compoundButton, b) -> {
            toggleEdit(b);
            editor = sharedPref.edit();
            editor.putBoolean("editState", b);
            editor.apply();
        });


        return super.onCreateOptionsMenu(mOptionsMenu);
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor = sharedPref.edit();
        editor.putBoolean("connection", connection);
        editor.apply();
    }

    public void createMaps(){
        File file = new File(getDir("data", MODE_PRIVATE), "shelfSizeMap");

        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
            shelfSizeMap = (HashMap<String, int[]>) inputStream.readObject();
            inputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(shelfSizeMap==null) {
            //some old values if the program cannot get saved values
            shelfSizeMap = new HashMap<>();
            shelfSizeMap.put("KR1", new int[]{6, 7});
            shelfSizeMap.put("KS1", new int[]{4, 2});
            shelfSizeMap.put("KS2", new int[]{2, 4});
            shelfSizeMap.put("KS3", new int[]{6, 2});
            shelfSizeMap.put("KW1", new int[]{3, 3});
            shelfSizeMap.put("KW2", new int[]{1, 3});
            shelfSizeMap.put("KT1", new int[]{1, 4});
            shelfSizeMap.put("WR1", new int[]{4, 6});
            shelfSizeMap.put("DS1", new int[]{1, 2});
            shelfSizeMap.put("PR1", new int[]{5, 5});
        }
    }

    private void setupLayout(){
        LinearLayoutManager lm = new LinearLayoutManager(ctx);
        rvTable.setHasFixedSize(true);
        rvTable.setLayoutManager(lm);

        //rvAdapter = new MyRecyclerViewAdapter(ctx, resultChests);
        //sets in this file implemented clickListener for each row
        //rvAdapter.setClickListener(this);
        //rvTable.setAdapter(rvAdapter);

        // Get the string array
        //String[] countries = getResources().getStringArray(R.array.content_array);
        // Create the adapter and set it to the AutoCompleteTextView

        if(suggestContentList == null){
           suggestContentList = getResources().getStringArray(R.array.content_array);
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, suggestContentList);
        input.setAdapter(adapter);

        //add an listener for the click on the done button
        input.setOnEditorActionListener((v, actionId, event) -> {
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
        });

        input.setOnItemClickListener((parent, view, position, id) -> {
            //input.setText(parent.getItemAtPosition(position).toString());
            performSearch();
            input.dismissDropDown();
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if(imm != null) {
                imm.hideSoftInputFromWindow(input.getApplicationWindowToken(), 0);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab_addchest);
        fab.setOnClickListener(this::addChestClick);


    }

    private void clearAll(){
        resultChests = new ArrayList<>();
    }

    private void processData(int mode, JSONObject[] resultArray){
        String shelfRoom;
        int newX, newY, oldX, oldY;
        try {
            int length;
            length = resultArray.length;
            if(length == 0 && mode == 1){
                Toast.makeText(ctx, R.string.all_noresult, LENGTH_LONG).show();
            }else if(length  > 5 && mode == 1){
                Toast.makeText(ctx, R.string.all_toomanyresults, LENGTH_LONG).show();
            }else {
                resultChests = new ArrayList<>();
                for (JSONObject chest : resultArray) {
                    shelfRoom = chest.getString("room") + chest.getString("rack");
                    newX = Integer.parseInt(chest.getString("X"));
                    newY = Integer.parseInt(chest.getString("Y").split("\\.")[0]);
                    resultChests.add(new Chest(ctx ,chest.getString("content"), chest.getString("room"), chest.getString("rack"), newX, newY, chestVisibility));

                    //rvAdapter.notifyItemInserted(resultChests.size()-1);
                    if (shelfSizeMap.containsKey(shelfRoom)) {
                        if (mode == 2) {
                            oldX = shelfSizeMap.get(shelfRoom)[0];
                            oldY = shelfSizeMap.get(shelfRoom)[1];
                            if (oldX < newX) {
                                if (oldY < newY) {
                                    shelfSizeMap.put(shelfRoom, new int[]{newX, newY});
                                } else {
                                    shelfSizeMap.put(shelfRoom, new int[]{newX, oldY});
                                }
                            } else if (oldY < newY) {
                                shelfSizeMap.put(shelfRoom, new int[]{oldX, newY});
                            }
                        }
                    }else{
                        Toast.makeText(ctx, getResources().getString(R.string.all_shelf_not_found, shelfRoom), LENGTH_LONG).show();
                    }
                }
                updateRV();
            }
        }catch (Exception e){
            Toast.makeText(ctx, e + " in processData", LENGTH_LONG).show();
            Log.d("error", e + " in processData");
        }
    }

    private void updateRV(){
        MyRecyclerViewAdapter rvAdapter = new MyRecyclerViewAdapter(ctx, resultChests);
        //sets in this file implemented clickListener for each row
        rvAdapter.setClickListener(this);
        rvTable.setAdapter(rvAdapter);
        rvAdapter.notifyDataSetChanged();
    }

    private void jsonRequest(final int mode){
        String url = "";
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        if(mode == 1) {
            url = String.format(baseURL + "search.php?s=%s&apikey=%s", input.getText(), apikey);
        }else if(mode == 2){
            url = String.format(baseURL + "read.php?apikey=%s", apikey);
        }
        final ProgressDialog dialog = ProgressDialog.show(this, null, "Please Wait");
        // Request a string response from the provided URL.
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (com.android.volley.Request.Method.GET, url, null,
                        response -> {
                            dialog.dismiss();
                            try {
                                JSONArray arrJson = response.getJSONArray("results");
                                JSONObject[] resultArray = new JSONObject[arrJson.length()];
                                for (int i = 0; i < arrJson.length(); i++) {
                                    resultArray[i] = arrJson.getJSONObject(i);
                                }
                                if(mode == 2){
                                    localJSONArray = arrJson;

                                    if(needToUpdateSuggestions){
                                        updateSuggestions();
                                        needToUpdateSuggestions = false;
                                    }
                                }
                                //clearAll();
                                processData(mode, resultArray);
                            }catch(JSONException e){
                                Toast.makeText(ctx, R.string.all_notFound, LENGTH_LONG).show();
                                Log.d("error", e.toString());
                            }
                        }, volleyError -> {
                            dialog.dismiss();
                            Log.d("error", volleyError.toString());
                            Toast.makeText(ctx, R.string.all_networkError, LENGTH_LONG).show();
                        });
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
        //queue.start();
    }

    private void jsonRequest(final int mode, Chest chest, int index){
        String url = "";

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);


        if(mode == 3) {
            url = String.format(baseURL + "add.php?apikey=%s&content=%s&room=%s&rack=%s&x=%s&y=%s", apikey, chest.content, chest.room.shrt, chest.rack.shrt, chest.coords[0], chest.coords[1]);
        }else if(mode == 4){
            url = String.format(baseURL + "editcontent.php?apikey=%s&content=%s&room=%s&rack=%s&x=%s&y=%s", apikey, chest.content, chest.room.shrt, chest.rack.shrt, chest.coords[0], chest.coords[1]);
        }
        Log.d("api", url);
        final ProgressDialog dialog = ProgressDialog.show(this, null, "Please Wait");
        // Request a string response from the provided URL.
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (com.android.volley.Request.Method.GET, url, null,
                        response -> {
                            String message = "";
                            dialog.dismiss();
                            try {
                                int respFlag = response.getInt("flag");
                                if(mode == 3){
                                    switch (respFlag) {
                                        case 0:
                                            message = getString(R.string.api_pos_exists, chest.coordsAsString, chest.rack.lng);
                                            break;
                                        case -1:
                                            message = getString(R.string.api_couldnt_add);
                                            break;
                                        case 1:
                                            message = getString(R.string.api_added_chest);

                                            resultChests.add(chest);
                                            //rvAdapter.notifyItemInserted(resultChests.size() - 1);
                                            updateRV();

                                            break;
                                    }
                                }else if(mode == 4){
                                    switch (respFlag) {
                                        case -1:
                                            message = getString(R.string.api_couldnt_edit);
                                            break;
                                        case 1:
                                            message = getString(R.string.api_edited_chest);
                                            resultChests.set(index, chest);
                                            updateRV();
                                            break;
                                    }
                                }
                                Toast.makeText(ctx, message, LENGTH_LONG).show();
                            }catch(JSONException e){
                                Toast.makeText(ctx, R.string.all_unknown, LENGTH_LONG).show();
                                Log.d("error", e.toString());
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
            if(jsonArrayString != null) {
                if (!jsonArrayString.equals("")) {
                    try {
                        localJSONArray = new JSONArray(jsonArrayString);
                        return true;
                    } catch (JSONException e) {
                        Toast.makeText(ctx, R.string.all_noLocalJson, LENGTH_LONG).show();
                        Log.d("error", e + " in getLocalJSON");
                        return false;
                    }
                }
                return false;
            }
        }
        return true;
    }

    private void retrieveFromLocal(int mode){
        JSONObject[] resultArray = null;
        ArrayList<JSONObject> resultList = new ArrayList<>();
        String searchString = input.getText().toString();
        try{
            if(localJSONArray!=null) {
                if (mode == 1) {
                    JSONObject localJSONObject;
                    for (int i = 0; i < localJSONArray.length(); i++) {
                    //for (JSONObject chest : localJSONObject) {
                        localJSONObject = localJSONArray.getJSONObject(i);
                        if (localJSONObject.getString("content").toLowerCase().contains(searchString.toLowerCase())) {
                            //Log.d("error", chest.getString("content"));
                            resultList.add(localJSONObject);
                        }
                        //convert the result List to the result Array
                        resultArray = new JSONObject[resultList.size()];
                        resultList.toArray(resultArray);
                    }
                } else if (mode == 2) {
                    JSONObject[] localJSONObjects = new JSONObject[localJSONArray.length()];
                    for (int i = 0; i < localJSONArray.length(); i++) {
                        localJSONObjects[i] = localJSONArray.getJSONObject(i);
                    }
                    resultArray = localJSONObjects;
                }
            }else{

                //ToDo handle this
            }
        }catch(JSONException je){
            Log.d("error", je + " in retrieveFromLocal");
        }
        if(resultArray != null) {
            //clearAll();
            processData(mode, resultArray);
        }else{
            Toast.makeText(ctx, R.string.all_notFound, LENGTH_LONG).show();
        }
    }

    private void saveLocalCopy(){
        if(localJSONArray!=null) {
            editor = sharedPref.edit();
            editor.putString("jsonArray", localJSONArray.toString());
            editor.apply();

            File file = new File(getDir("data", MODE_PRIVATE), "shelfSizeMap");

            try {
                ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
                outputStream.writeObject(shelfSizeMap);
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void updateSuggestions(){
        //get suggestionArray from localJsonArray
        if(localJSONArray != null){
            //convert localJSONArray to type JSONObject[]
            JSONObject chest;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < localJSONArray.length(); i++) {
                try {
                    chest = localJSONArray.getJSONObject(i);
                    sb.append(chest.getString("content").replaceAll("\\r\\n", stringDelimiter));
                    sb.append(stringDelimiter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            suggestContentList = sb.toString().split(stringDelimiter);
            //remove duplicates
            suggestContentList = new HashSet<>(Arrays.asList(suggestContentList)).toArray(new String[0]);

            sb = new StringBuilder();
            for (String item: suggestContentList) {
                sb.append(item);
                sb.append(stringDelimiter);
            }

            editor = sharedPref.edit();
            editor.putString("suggestContentList", sb.toString());
            editor.apply();

            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, suggestContentList);
            input.setAdapter(adapter);

        }
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        view.clearFocus();
    }

    public void readClick(View v){
        performRead();
        hideKeyboardFrom(ctx, v);
    }

    public void searchClick(View v){
        performSearch();
        hideKeyboardFrom(ctx, v);
    }

    public void addChestClick(View v){

        FragmentManager fm = getSupportFragmentManager();
        ChestPopup dialogFragment = ChestPopup.newInstance(1);

        fm.setFragmentResultListener("result", this, (requestKey, result) -> {
            String[] rs = result.getStringArray("newChest");
            Chest chest = new Chest(ctx ,rs[0], rs[1], rs[2], Integer.parseInt(rs[3]), Integer.parseInt(rs[4]), chestVisibility);
            jsonRequest(3, chest, 0);
            Log.d("popup", rs[0]);
        });
        dialogFragment.show(fm, "fragment_edit_name");
    }

    public void editChest(Chest chestToEdit, int index){
        FragmentManager fm = getSupportFragmentManager();
        ChestPopup dialogFragment = ChestPopup.newInstance(chestToEdit, 2);

        fm.setFragmentResultListener("result", this, (requestKey, result) -> {
            String[] rs = result.getStringArray("newChest");
            Chest chest = new Chest(ctx ,rs[0], rs[1], rs[2], Integer.parseInt(rs[3]), Integer.parseInt(rs[4]), chestVisibility);
            jsonRequest(4, chest, index);
            Log.d("popup", rs[0]);
        });
        dialogFragment.show(fm, "fragment_edit_name");
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
    }

    /*private void toggleConnection(){
        if(connection){
            boolean noError;
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
        editor = sharedPref.edit();
        editor.putBoolean("connection", connection);
        editor.apply();
    }*/

    private void toggleEdit(boolean state){
        ImageView ivA, ivE;

        ivA = findViewById(R.id.fab_addchest);
        if(state) {
            chestVisibility = View.VISIBLE;
        }else{
            chestVisibility = View.GONE;
        }

        ivA.setVisibility(chestVisibility);
        for (Chest chest: resultChests) {
            chest.visibility = chestVisibility;
        }
        updateRV();
    }

    private void showRoomImage(Chest chest, int pos){
        final ImageView ivRoom;
        final TextView tvTitle;
        final Button btnClose;
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(ctx);
        final View promptsView = li.inflate(R.layout.activity_image_popup, new LinearLayout(ctx), false);

        final PopupWindow pw = new PopupWindow(new ContextThemeWrapper(MainActivity.this, R.style.popupTheme));

        ivRoom  = promptsView.findViewById(R.id.iv_room);
        tvTitle = promptsView.findViewById(R.id.tv_img_title);
        int imageId = getResources().getIdentifier(chest.getImageName(), "drawable", getPackageName());

        ivRoom.setImageResource(imageId);

        pw.setContentView(promptsView);

        tvTitle.setText(getString(R.string.all_location_concat, chest.rack.lng, chest.room.lng));
        btnClose = promptsView.findViewById(R.id.btn_img_close);
        btnClose.setOnClickListener(view -> pw.dismiss());

        pw.setOutsideTouchable(true);
        pw.setFocusable(true);
        pw.showAtLocation(this.rvTable.getChildAt(0), Gravity.CENTER, 0, 0);
    }

    private void showShelf(Chest chest, int pos){
        TextView tv;
        final TextView tvTitle;
        final Button btnClose;
        int rowCount, colCount, x ,y;
        LayoutInflater li = LayoutInflater.from(ctx);
        View promptsView = li.inflate(R.layout.activity_shelf_popup, new LinearLayout(ctx), false);


        final PopupWindow pw = new PopupWindow(new ContextThemeWrapper(MainActivity.this, R.style.popupTheme));

        pw.setContentView(promptsView);

        final TableLayout tlShelf = promptsView.findViewById(R.id.tl_shelf);


        rowCount = Objects.requireNonNull(shelfSizeMap.get(chest.locationToString()), "shelf in the specific room not found")[0];
        colCount = Objects.requireNonNull(shelfSizeMap.get(chest.locationToString()), "shelf in the specific room not found")[1];
        x = chest.coords[0];
        y = chest.coords[1];

        //add top row
        TableRow header = new TableRow(ctx);
        TextView topLeft = new TextView(ctx);


        for(int row = rowCount; row >= 0; row--){
            TableRow tr = new TableRow(ctx);
            for(int col = 0; col < colCount; col ++){
                tv = new TextView(ctx);
                if(col == 0){
                    //y-axis legend
                    tv.setText(String.valueOf(row));
                    tv.setTextColor(getResources().getColor(R.color.colorSecondaryText));
                }else {
                    if (row == y && col == x) {
                        tv.setText(chest.coordsAsString);
                        tv.setTextColor(getResources().getColor(R.color.colorPrimary));
                    } else {
                        tv.setText("");
                    }
                    tv.setBackgroundResource(R.drawable.back);
                }

                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                tv.setTextSize(18);
                tv.setWidth(0);
                tr.addView(tv);

            }
            tlShelf.addView(tr);
        }

        //create x-axis legend
        topLeft.setText("");

        topLeft.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        topLeft.setTextSize(18);
        header.addView(topLeft);
        for(int col = 1; col < colCount; col ++){
            tv = new TextView(ctx);
            tv.setText(String.valueOf(col));

            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv.setTextSize(18);
            tv.setTextColor(getResources().getColor(R.color.colorSecondaryText));
            header.addView(tv);
        }
        tlShelf.addView(header);

        tvTitle = promptsView.findViewById(R.id.tv_shelf_title);
        tvTitle.setText(getString(R.string.all_location_concat, chest.rack.lng, chest.room.lng));
        btnClose = promptsView.findViewById(R.id.btn_shelf_close);
        btnClose.setOnClickListener(view -> pw.dismiss());

        pw.setOutsideTouchable(true);
        pw.setFocusable(true);
        pw.showAtLocation(this.rvTable.getChildAt(0), Gravity.CENTER, 0, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Random rand = new Random();
        int newId = rand.nextInt(numberKnollen);
        while(newId == knolleIconId){
            newId = rand.nextInt(numberKnollen);
        }
        knolleIconId = newId;
        // Handle item selection
        if(item.getItemId() == R.id.knolleIcon){
            mOptionsMenu.findItem(R.id.knolleIcon).setIcon(knolleIcons[knolleIconId]);
            return true;
        //}else if(item.getItemId() == R.id.connection){
        //    toggleConnection();
        //    return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        if(view.getId() == R.id.ll_roomshelf || view.getId() == R.id.tv_storage || view.getId() == R.id.tv_shelf) {
            showRoomImage(resultChests.get(position), position);
        }else if(view.getId()== R.id.tv_coords){
                showShelf(resultChests.get(position), position);

        }else if(view.getId() == R.id.iv_edit){
            editChest(resultChests.get(position), position);
        }

    }
}
