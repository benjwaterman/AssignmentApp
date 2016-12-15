package uk.ac.lincoln.bwaterman.assignmentapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Ben on 15/12/2016.
 */

public class FileHelper {

    Context thisContext;

    //Function to save image to folder in internal storage
    public void saveImage(Context context, Bitmap bm, String fileName, String folderName){
        thisContext = context;

        ///Create path to create folder or check if it exists
        File path = new File(context.getFilesDir(), folderName);

        saveImageToDisk(bm, path, fileName);

    }

    //Save image with a nested folder
    public void saveImage(Context context, Bitmap bm, String fileName, String folderName, String subFolderName){
        thisContext = context;

        File parentPath = new File(context.getFilesDir(), folderName);
        File childPath = new File(parentPath, subFolderName);

        saveImageToDisk(bm, childPath, fileName);

    }

    private void saveImageToDisk(Bitmap bm, File path, String fileName) {
        FileOutputStream out;

        //Make directory if it doesn't exist
        if(!path.exists()) {
            boolean success = path.mkdirs();
            //If unsuccessful
            if(!success) {
                createToast("Couldn't create folder");
                return;
            }
        }

        //Write to file
        try {
            //Create filepath of the image to be saved
            File filePath = new File(path, fileName);
            //Open output stream from filepath
            out = new FileOutputStream(filePath);
            //Write data to file
            bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
            //Close stream
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Function to return saved image path
    public String getFilePath(Context context, String fileName, String folderName){
        thisContext = context;
        File filePath;
        try {
            //Folder path
            File path = new File(context.getFilesDir(), folderName);
            //Create filepath of the image in folder
            filePath = new File(path, fileName);

            return filePath.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getFilePath(Context context, String fileName, String folderName, String subFolderName){
        thisContext = context;

        try {
            File parentPath = new File(context.getFilesDir(), folderName);
            File childPath = new File(parentPath, subFolderName);
            File filePath = new File(childPath, fileName);

            return filePath.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Function to save text to internal storage, used to keep track of all images that have been saved
    public void saveText(Context context, String fileName, String text) {
        FileOutputStream out;
        File file = context.getFileStreamPath(fileName);
        try{
            if(file == null || !file.exists())
            {
                try {
                    out = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                    out.write(text.getBytes());
                    out.write("\r\n".getBytes());
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            else if(file.exists())
            {
                try {
                    out = context.openFileOutput(fileName, Context.MODE_APPEND);
                    out.write(text.getBytes());
                    out.write("\r\n".getBytes());
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    //Have to create toast in UI thread
    void createToast(String message ) {
        final String toastText = message;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(thisContext, toastText, Toast.LENGTH_LONG).show();
            }
        });
    }
}