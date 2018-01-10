package com.hong2.alpha.Utils;

import android.os.Environment;

/**
 * Created by User on 7/24/2017.
 */

public class FilePaths {

    //"storage/emulated/0"
    public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    public String PICTURES = ROOT_DIR + "/Pictures";
    public String CAMERA = ROOT_DIR + "/DCIM/Camera";
    public String DOWNLOAD = ROOT_DIR + "/" + Environment.DIRECTORY_DOWNLOADS;

    public String FIREBASE_IMAGE_STORAGE = "photos/users/";


}
