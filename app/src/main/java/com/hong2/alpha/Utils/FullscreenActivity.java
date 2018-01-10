package com.hong2.alpha.Utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.hong2.alpha.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;


public class FullscreenActivity extends AppCompatActivity {

    private long enqueue;
    private DownloadManager dm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        Intent curIntent = getIntent();
        if (curIntent.hasExtra("URL")) {
            final String url = curIntent.getStringExtra("URL");
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(
                    url,
                    (ImageView)findViewById(R.id.big_image));
            ImageButton downloadButton = findViewById(R.id.download_button);
            downloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadThroughManager(url, FullscreenActivity.this);
                }
            });
        }
    }

    public static void downloadThroughManager(String imageUrl, Context context) {


        File path = new File(imageUrl);
        String fileName = path.getName();
        final DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(imageUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        if(fileName.indexOf('?') != -1) {
            fileName = fileName.substring(0, fileName.indexOf('?'));
        }
        if(fileName.lastIndexOf('/') != -1)
        {
            fileName = fileName.substring(fileName.lastIndexOf('/' ) + 1);
        }

        if(fileName.lastIndexOf("%2F") != -1)
        {
            fileName = fileName.substring(fileName.lastIndexOf("%2F" ) + 3);
        }

        if(fileName.lastIndexOf("%2f") != -1)
        {
            fileName = fileName.substring(fileName.lastIndexOf("%2f" ) + 3);
        }

        fileName = fileName + ".png";

        request.setTitle(fileName);
        request.setDescription(fileName);
        request.setVisibleInDownloadsUi(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        long ref = downloadManager.enqueue(request);

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);




        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long downloadReference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                Log.i("GenerateTurePDfAsync", "Download completed");


                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadReference);

                Cursor cur = downloadManager.query(query);

                if (cur.moveToFirst()) {
                    int columnIndex = cur.getColumnIndex(DownloadManager.COLUMN_STATUS);



                    if (DownloadManager.STATUS_SUCCESSFUL == cur.getInt(columnIndex)) {
                        String uriString = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));

                        Toast.makeText(context, "File has been downloaded successfully.", Toast.LENGTH_SHORT).show();


                    } else if (DownloadManager.STATUS_FAILED == cur.getInt(columnIndex)) {
                        int columnReason = cur.getColumnIndex(DownloadManager.COLUMN_REASON);
                        int reason = cur.getInt(columnReason);
                        switch(reason){

                            case DownloadManager.ERROR_FILE_ERROR:
                                Toast.makeText(context, "Download Failed.File is corrupt.", Toast.LENGTH_LONG).show();
                                break;
                            case DownloadManager.ERROR_HTTP_DATA_ERROR:
                                Toast.makeText(context, "Download Failed.Http Error Found.", Toast.LENGTH_LONG).show();
                                break;
                            case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                                Toast.makeText(context, "Download Failed due to insufficient space in internal storage", Toast.LENGTH_LONG).show();
                                break;

                            case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                                Toast.makeText(context, "Download Failed. Http Code Error Found.", Toast.LENGTH_LONG).show();
                                break;
                            case DownloadManager.ERROR_UNKNOWN:
                                Toast.makeText(context, "Download Failed.", Toast.LENGTH_LONG).show();
                                break;
                            case DownloadManager.ERROR_CANNOT_RESUME:
                                Toast.makeText(context, "ERROR_CANNOT_RESUME", Toast.LENGTH_LONG).show();
                                break;
                            case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                                Toast.makeText(context, "ERROR_TOO_MANY_REDIRECTS", Toast.LENGTH_LONG).show();
                                break;
                            case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                                Toast.makeText(context, "ERROR_DEVICE_NOT_FOUND", Toast.LENGTH_LONG).show();
                                break;

                        }
                    }
                }
            }

        };


        context.registerReceiver(receiver, filter);
    }
}
