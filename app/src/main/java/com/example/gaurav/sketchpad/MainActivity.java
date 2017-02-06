package com.example.gaurav.sketchpad;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Handler;
import android.preference.DialogPreference;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Wrapper;
import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;;
import java.util.jar.Manifest;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    DrawingView drawing;
    private ImageButton currColor, newFileBtn, saveBtn, brushBtn, eraseBtn, deleteBtn;
    private PopupWindow popupWindow;
    LinearLayout linearLayout;
    private float brush1 = 10, brush2 = 20, brush3 = 30, brush4 = 40;
    DisplayMetrics dm;
    int height, width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawing = (DrawingView) findViewById(R.id.drawing);
        LinearLayout colors = (LinearLayout) findViewById(R.id.colors);
        currColor = (ImageButton) colors.getChildAt(0);
        currColor.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        newFileBtn = (ImageButton) findViewById(R.id.newFileBtn);
        newFileBtn.setOnClickListener(this);

        saveBtn = (ImageButton) findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(this);

        brushBtn = (ImageButton) findViewById(R.id.brushBtn);
        brushBtn.setOnClickListener(this);

        linearLayout = (LinearLayout) findViewById(R.id.activity_main);

        drawing.setBrushSize(brush2);

        eraseBtn = (ImageButton) findViewById(R.id.eraseBtn);
        eraseBtn.setOnClickListener(this);

        dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        height = dm.heightPixels;
        width = dm.widthPixels;

        deleteBtn = (ImageButton) findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(this);
    }

    public void paintClicked(View view) {

        if (view != currColor) {
            ImageButton changeColor = (ImageButton) view;
            String color = view.getTag().toString();
            drawing.setColor(color);
            changeColor.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currColor.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currColor = (ImageButton) view;
            drawing.setErase(false);
            drawing.setBrushSize(drawing.getLastBrushSize());
        } else {
            String color = view.getTag().toString();
            drawing.setColor(color);
            drawing.setErase(false);
            drawing.setBrushSize(drawing.getLastBrushSize());
        }
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.newFileBtn) {

            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("Start New Drawing");
            newDialog.setMessage("Are You Sure?? Your current drawing will not be saved!!");
            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    drawing.startNew();
                    dialog.dismiss();
                }
            });
            newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            newDialog.show();
        } else if (view.getId() == R.id.saveBtn) {

            final AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle("Save Drawing");
            saveDialog.setMessage("Are you sure you want to save drawing to the gallery??");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    drawing.setDrawingCacheEnabled(true);

                    // String imgName = MediaStore.Images.Media.insertImage(getContentResolver(),drawing.getDrawingCache(),
                    //        "DRW_" + s + ".png","drawing");

                    Date d = new Date();

                    CharSequence s = android.text.format.DateFormat.format("yyyyMMdd_hhmmss", d.getTime());

                    String imgName = "DRW_" + s + ".png";

                    File directory = new File(Environment.getExternalStorageDirectory() + "/Sketch Pad/");

                    if(!directory.exists())
                        directory.mkdirs();

                    File file = new File(directory, imgName);
                    FileOutputStream out = null;

                    try {
                        out = new FileOutputStream(file);
                        drawing.getDrawingCache().compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (out != null)
                        Toast.makeText(getApplicationContext(), "Drawing Saved Successfully!!", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(), "Drawing not saved.. Please check storage permissions!!", Toast.LENGTH_SHORT).show();
                    drawing.destroyDrawingCache();

                    MediaScannerConnection.scanFile(getApplicationContext(),new String[]{file.getPath()},null,null);
                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            saveDialog.show();
        } else if (view.getId() == R.id.brushBtn) {

            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.brush_pop_up_window, null);
            popupWindow = new PopupWindow(container, (int) (width * 0.9), (int) (height * 0.11), true);
            popupWindow.showAtLocation(linearLayout, Gravity.NO_GRAVITY, 0, (int) (height * 0.25));

            container.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popupWindow.dismiss();
                    return true;
                }
            });

            ImageButton brush1Btn = (ImageButton) container.findViewById(R.id.brush1Btn);
            brush1Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawing.setBrushSize(brush1);
                    drawing.setLastBrushSize(brush1);
                    drawing.setErase(false);
                    popupWindow.dismiss();
                }
            });

            ImageButton brush2Btn = (ImageButton) container.findViewById(R.id.brush2Btn);
            brush2Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawing.setBrushSize(brush2);
                    drawing.setLastBrushSize(brush2);
                    drawing.setErase(false);
                    popupWindow.dismiss();
                }
            });

            ImageButton brush3Btn = (ImageButton) container.findViewById(R.id.brush3Btn);
            brush3Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawing.setBrushSize(brush3);
                    drawing.setLastBrushSize(brush3);
                    drawing.setErase(false);
                    popupWindow.dismiss();
                }
            });

            ImageButton brush4Btn = (ImageButton) container.findViewById(R.id.brush4Btn);
            brush4Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawing.setBrushSize(brush4);
                    drawing.setLastBrushSize(brush4);
                    drawing.setErase(false);
                    popupWindow.dismiss();
                }
            });
        } else if (view.getId() == R.id.eraseBtn) {

            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.eraser_pop_up_window, null);
            popupWindow = new PopupWindow(container, (int) (width * 0.9), (int) (height * 0.11), true);
            popupWindow.showAtLocation(linearLayout, Gravity.NO_GRAVITY, (int) (width * 0.1), (int) (height * 0.25));

            container.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popupWindow.dismiss();
                    return true;
                }
            });

            ImageButton erase1Btn = (ImageButton) container.findViewById(R.id.erase1Btn);
            erase1Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawing.setErase(true);
                    drawing.setBrushSize(brush1);
                    popupWindow.dismiss();
                }
            });

            ImageButton erase2Btn = (ImageButton) container.findViewById(R.id.erase2Btn);
            erase2Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawing.setErase(true);
                    drawing.setBrushSize(brush2);
                    popupWindow.dismiss();
                }
            });

            ImageButton erase3Btn = (ImageButton) container.findViewById(R.id.erase3Btn);
            erase3Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawing.setErase(true);
                    drawing.setBrushSize(brush3);
                    popupWindow.dismiss();
                }
            });

            ImageButton erase4Btn = (ImageButton) container.findViewById(R.id.erase4Btn);
            erase4Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawing.setErase(true);
                    drawing.setBrushSize(brush4);
                    popupWindow.dismiss();
                }
            });

        } else if (view.getId() == R.id.deleteBtn) {

            drawing.undo();
        }


    }
}