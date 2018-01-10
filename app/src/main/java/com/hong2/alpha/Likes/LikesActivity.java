package com.hong2.alpha.Likes;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hong2.alpha.R;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.hong2.alpha.Utils.BottomNavigationViewHelper;

import java.util.UUID;

/**
 * Created by User on 5/28/2017.
 */

public class LikesActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LikesActivity";
    private static final int ACTIVITY_NUM = 3;

    private Context mContext = LikesActivity.this;

    //custom drawing view
    private DrawingView drawView;
    //buttons
    private ImageButton currPaint, eraseBtn, newBtn, saveBtn, loadBtn, palleteBtn, filterBtn, frameBtn, effectBtn;
    private final int PERMISSION_REQUEST_CODE = 133;
    //sizes
    private boolean isErasing;
    private int selectColor, selectBlur;
    private float selectSize;
    private final CharSequence[] filters = {"normal", "lighten", "darken", "overlay", "screen", "multiply"};
    private final CharSequence[] effects = {"negate", "embose", "grayscale"};
    private final short RESULT_LOAD_IMAGE = 0x1ff3;
    private boolean isFramed;

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (result == PackageManager.PERMISSION_GRANTED)
                return true;
            else
                return false;
        } else {
            return false;
        }
    }
    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);
        Log.d(TAG, "onCreate: started.");

        setupBottomNavigationView();
        if (!checkPermission())
            requestPermission();
        isFramed = false;
        //get drawing view
        drawView = (DrawingView) findViewById(R.id.drawing);

        //get the palette and first color button
        LinearLayout paintLayout = (LinearLayout) findViewById(R.id.paint_colors);
        currPaint = (ImageButton) paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));


        //set initial size
        drawView.setBrushSize(getResources().getInteger(R.integer.medium_size));

        //erase button
        eraseBtn = (ImageButton) findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);

        //new button
        newBtn = (ImageButton) findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

        //save button
        saveBtn = (ImageButton) findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        //import button
        loadBtn = (ImageButton) findViewById(R.id.load_btn);
        loadBtn.setOnClickListener(this);


        palleteBtn = (ImageButton) findViewById(R.id.pallete_btn);
        palleteBtn.setOnClickListener(this);

        filterBtn = (ImageButton) findViewById(R.id.filter_btn);
        filterBtn.setOnClickListener(this);


        frameBtn = (ImageButton) findViewById(R.id.frame_btn);
        frameBtn.setOnClickListener(this);

        effectBtn = (ImageButton) findViewById(R.id.effect_btn);
        effectBtn.setOnClickListener(this);

        isErasing = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //user clicked paint
    public void paintClicked(View view) {
        //use chosen color

        //set erase false
        drawView.setErase(false);
        drawView.setPaintAlpha(100);

        if (view != currPaint) {
            ImageButton imgView = (ImageButton) view;
            String color = view.getTag().toString();
            drawView.setColor(color);
            //update ui
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            if (currPaint != null)
                currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint = (ImageButton) view;
        }
    }

    @Override
    public void onClick(View view) {


        if (view.getId() == R.id.erase_btn) {
            //switch to erase - choose size
            if (isErasing) {
                drawView.setErase(false);
                eraseBtn.setImageResource(R.drawable.eraser);
                isErasing = false;
            } else {
                drawView.setErase(true);
                eraseBtn.setImageResource(R.drawable.brush);
                isErasing = true;
            }
        } else if (view.getId() == R.id.new_btn) {
            //new button
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("New drawing");
            newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    drawView.startNew();
                    isFramed = false;
                    frameBtn.setImageResource(R.drawable.branch);
                    dialog.dismiss();
                }
            });
            newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            newDialog.show();
        } else if (view.getId() == R.id.save_btn) {
            if (isFramed) {
                Toast mergeToast = Toast.makeText(getApplicationContext(),
                        "Must merge frame to save.", Toast.LENGTH_SHORT);
                mergeToast.show();
            } else {
                //save drawing
                AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
                saveDialog.setTitle("Save drawing");
                saveDialog.setMessage("Save drawing to device Gallery?");
                saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //save drawing
                        drawView.setDrawingCacheEnabled(true);
                        //attempt to save
                        String imgSaved = MediaStore.Images.Media.insertImage(
                                getContentResolver(), drawView.getDrawingCache(),
                                UUID.randomUUID().toString() + ".png", "drawing");
                        //feedback
                        if (imgSaved != null) {
                            Toast savedToast = Toast.makeText(getApplicationContext(),
                                    "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                            savedToast.show();
                        } else {
                            Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                    "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
                            unsavedToast.show();
                        }
                        drawView.destroyDrawingCache();
                    }
                });
                saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                saveDialog.show();
            }
        } else if (view.getId() == R.id.load_btn) {
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setType("image/*");
            startActivityForResult(i, RESULT_LOAD_IMAGE);
        } else if (view.getId() == R.id.pallete_btn) {
            //launch opacity chooser
            final View innerView = getLayoutInflater().inflate(R.layout.opacity_chooser, null);
            selectColor = drawView.getColor();
            selectSize = drawView.getBrushSize();
            selectBlur = drawView.getBlur();
            final Dialog seekDialog = new Dialog(this);
            WindowManager.LayoutParams params = seekDialog.getWindow().getAttributes();
            seekDialog.setTitle("Opacity level:");
            seekDialog.setContentView(innerView);
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            seekDialog.getWindow().setAttributes((WindowManager.LayoutParams) params);


            //get ui elements
            final TextView seekTxt = (TextView) seekDialog.findViewById(R.id.opq_txt);
            final SeekBar seekOpq = (SeekBar) seekDialog.findViewById(R.id.opacity_seek);

            final TextView redTxt = (TextView) seekDialog.findViewById(R.id.red_txt);
            final SeekBar seekRed = (SeekBar) seekDialog.findViewById(R.id.red_seek);

            final TextView greenTxt = (TextView) seekDialog.findViewById(R.id.green_txt);
            final SeekBar seekGreen = (SeekBar) seekDialog.findViewById(R.id.green_seek);

            final TextView blueTxt = (TextView) seekDialog.findViewById(R.id.blue_txt);
            final SeekBar seekBlue = (SeekBar) seekDialog.findViewById(R.id.blue_seek);

            final TextView sizeTxt = (TextView) seekDialog.findViewById(R.id.size_txt);
            final SeekBar seekSize = (SeekBar) seekDialog.findViewById(R.id.size_seek);

            final TextView blurTxt = (TextView) seekDialog.findViewById(R.id.blur_txt);
            final SeekBar seekBlur = (SeekBar) seekDialog.findViewById(R.id.blur_seek);

            final View seekImg = (View) seekDialog.findViewById(R.id.opacity_image);
            //set max
            seekOpq.setMax(255);
            seekRed.setMax(255);
            seekGreen.setMax(255);
            seekBlue.setMax(255);
            seekSize.setMax(200);
            seekBlur.setMax(50);

            //show current level

            int currLevel = ((selectColor & 0xff000000) >> 24) & 0xff;
            int currRed = (selectColor & 0x00ff0000) >> 16;
            int currGreen = (selectColor & 0x0000ff00) >> 8;
            int currBlue = (selectColor & 0x000000ff);

            seekTxt.setText(Integer.toString(currLevel));
            seekOpq.setProgress(currLevel);

            redTxt.setText(Integer.toString(currRed));
            seekRed.setProgress(currRed);

            greenTxt.setText(Integer.toString(currGreen));
            seekGreen.setProgress(currGreen);

            blueTxt.setText(Integer.toString(currBlue));
            seekBlue.setProgress(currBlue);

            sizeTxt.setText(Integer.toString((int) selectSize));
            seekSize.setProgress((int) selectSize);

            blurTxt.setText(Integer.toString(selectBlur));
            seekBlur.setProgress(selectBlur);

            seekImg.setBackgroundColor(selectColor);
            seekImg.invalidate();
            //update as user interacts
            seekOpq.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    seekTxt.setText(Integer.toString(progress));
                    selectColor = (selectColor & 0xffffff) | (progress << 24);
                    seekImg.setBackgroundColor(selectColor);
                    seekImg.invalidate();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }

            });

            seekRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    redTxt.setText(Integer.toString(i));
                    selectColor = (selectColor & 0xff00ffff) | ((i & 0xff) << 16);
                    seekImg.setBackgroundColor(selectColor);
                    seekImg.invalidate();

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            seekGreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    greenTxt.setText(Integer.toString(i));
                    selectColor = (selectColor & 0xffff00ff) | ((i & 0xff) << 8);
                    seekImg.setBackgroundColor(selectColor);
                    seekImg.invalidate();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            seekBlue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    blueTxt.setText(Integer.toString(i));
                    selectColor = (selectColor & 0xffffff00) | (i & 0xff);
                    seekImg.setBackgroundColor(selectColor);
                    seekImg.invalidate();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            seekSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    sizeTxt.setText(Integer.toString(i));
                    selectSize = i;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            seekBlur.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    blurTxt.setText(Integer.toString(i));
                    selectBlur = i;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            //listen for clicks on ok
            Button opqBtn = (Button) seekDialog.findViewById(R.id.opq_ok);
            opqBtn.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    drawView.setColor(selectColor);
                    drawView.setBrushSize(selectSize);
                    drawView.setBlur(selectBlur);
                    if (currPaint != null) {
                        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
                    }
                    currPaint = null;
                    seekDialog.dismiss();
                }
            });
            //show dialog
            seekDialog.show();

        } else if (view.getId() == R.id.filter_btn) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select filter");
            builder.setItems(filters, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    drawView.setXfermode(filters[item].toString());
                    dialog.dismiss();
                }
            });
            builder.show();
        } else if (view.getId() == R.id.frame_btn) {
            if (!isFramed) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select filter");
                builder.setItems(filters, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        drawView.saveBitmap(filters[item].toString());
                        frameBtn.setImageResource(R.drawable.merge);
                        isFramed = true;
                        dialog.dismiss();
                    }
                });
                builder.show();
            } else {
                drawView.mergeBitmap();
                frameBtn.setImageResource(R.drawable.branch);
                isFramed = false;
            }
        } else if (view.getId() == R.id.effect_btn) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select effect");
            builder.setItems(effects, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    drawView.setEffect(effects[item].toString());
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                    drawView.importImage(bitmap);
                }
                cursor.close();


            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

            } else {
                finish();
            }
        }
    }
    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
