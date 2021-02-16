package com.raspbounty.knolleili;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class rackPopupActivity extends AppCompatActivity {

    private TableLayout resultTable;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rack_popup);
        ctx = this;
        resultTable = findViewById(R.id.popupTable);

        Intent intent = getIntent();
        String rackName = intent.getStringExtra("rackName");
        String roomName = intent.getStringExtra("roomName");
        String X = intent.getStringExtra("rackX");
        String Y = intent.getStringExtra("rackY");

        int colCount, rowCount;
        colCount = -1;
        rowCount = -1;
        switch (roomName) {
            case "K":
                switch (rackName) {
                    case "R1":
                        rowCount = 6;
                        colCount = 7;
                        break;
                    case "S1":
                        rowCount = 4;
                        colCount = 2;
                        break;
                    case "S2":
                        rowCount = 2;
                        colCount = 4;
                        break;
                    case "S3":
                        rowCount = 6;
                        colCount = 2;
                        break;
                    case "W1":
                        rowCount = 3;
                        colCount = 3;
                        break;
                    case "W2":
                        rowCount = 1;
                        colCount = 3;
                        break;
                    case "T1":
                        rowCount = 1;
                        colCount = 4;
                        break;
                    default:
                        break;
                }
                break;
            case "W":
                rowCount = 4;
                colCount = 6;
                break;
            case "D":
                rowCount = 1;
                colCount = 2;
                break;
            case "M":
                break;
            case "P":
                rowCount = 5;
                colCount = 5;
                break;
            default:
                break;
        }
        if(rowCount > 0 && colCount > 0) {
            createTable(X, Y, rowCount, colCount);
        }else{
            Toast.makeText(this, R.string.all_error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    private void createTable(String xString, String yString, int rowCount, int colCount){
        int x, y;
        x = Integer.parseInt(xString);
        y = Integer.parseInt(yString.split("\\.")[0]);

        //add top row
        TableRow header = new TableRow(ctx);
        TextView topLeft = new TextView(ctx);
        topLeft.setText("");
        if(Build.VERSION.SDK_INT > 16)
            topLeft.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        topLeft.setTextSize(18);
        header.addView(topLeft);
        for(int col = 1; col < colCount; col ++){
            TextView roomView = new TextView(ctx);
            roomView.setText(String.valueOf(col));
            if(Build.VERSION.SDK_INT > 16)
                roomView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            roomView.setTextSize(18);
            header.addView(roomView);
        }
        resultTable.addView(header);

        for(int row = rowCount; row >= 0; row--){
            TableRow tr = new TableRow(ctx);
            for(int col = 0; col < colCount; col ++){
                TextView roomView = new TextView(ctx);
                if(col == 0){
                    //left column
                    roomView.setText(String.valueOf(row));
                }else {
                    if (row == y && col == x) {
                        roomView.setText(xString + "," + yString);
                    } else {
                        roomView.setText("");
                    }
                    roomView.setBackgroundResource(R.drawable.back);
                }
                if(Build.VERSION.SDK_INT > 16)
                    roomView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                roomView.setTextSize(18);
                tr.addView(roomView);
            }
            resultTable.addView(tr);
        }
    }
}
