package uk.ac.lincoln.bwaterman.assignmentapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

/**
 * Created by Ben on 15/12/2016.
 */

public class FileHelper {

    private Context thisContext;
    private ImageLoader imageLoader;

    public FileHelper(Context context) {

        thisContext = context;
    }

    //Function to save image to folder in internal storage
    public void saveImage(Context context, Bitmap bm, String fileName, String folderName){
        thisContext = context;

        ///Create path to create folder or check if it exists
        File path = new File(context.getFilesDir(), folderName);

        saveImageToDisk(bm, path, fileName);

    }

    //Displays an image either by downloading the image and storing it or using a local copy if its available
    public void displayImage(Context context, ImageType imageType, String imageUrl, String name, ImageView _imageView, ProgressBar _progressBar, boolean isFavourite) {
        if (imageLoader == null)
            imageLoader = ImageLoader.getInstance();

        //Show progress spinner while image loads
        final ProgressBar progressBar = _progressBar;
        final ImageView imageView = _imageView;

        if(progressBar != null) {
            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.bringToFront();
        }

        //Name folder depending on if games or streamers or favourites
        String folderName;
        //Name file
        String fileName;
        switch(imageType) {
            case GAME:
                folderName = "games";
                fileName = "thumbnail";
                break;

            case STREAM:
                folderName = "streamers";
                fileName = "thumbnail";
                break;

            //If in favourites save to a different location than normal streamers, so the image doesn't get deleted if the user clears image data
            case LOGO:
                if(isFavourite) {
                    folderName = "favourites";
                }
                else {
                    folderName = "streamers";
                }
                fileName = "logo";
                break;

            case BANNER:
                if(isFavourite) {
                    folderName = "favourites";
                }
                else {
                    folderName = "streamers";
                }
                fileName = "banner";
                break;

            default:
                folderName = "games";
                fileName = "thumbnail";
                break;
        }

        //Save subfolder name as title of stream/game
        String subFolderName = name;
        //Replace blank space, this is to make the files work with ImageLoader as it doesn't accept certain characters in the file name
        subFolderName = subFolderName.replace(" ", "%");
        //Replace any "/"
        subFolderName = subFolderName.replace("/", "%");

        //Add file extension
        fileName += ".jpg";

        ///Path to parent folder
        File parentPath = new File(context.getFilesDir(), folderName);
        //Create child path for subfolder
        File childPath = new File(parentPath, subFolderName);
        //Create filepath of the image
        File file = new File(childPath, fileName);

        //Create final so can be used in function below
        final String finalFolderName = folderName;
        //Create sub folder
        final String finalSubFolderName = subFolderName;
        //Name the file thumbnail
        final String finalFileName = fileName;

        //If there is no file, download it, display it and save it to internal storage
        if (!file.exists()) {
            //If there is an url to download from
            if (imageUrl != null && !Objects.equals(imageUrl, "") && !Objects.equals(imageUrl, "null")) {

                try {
                    //Load image, after image has been loaded hide the progress spinner
                    imageLoader.loadImage(imageUrl, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            imageView.setImageBitmap(loadedImage);
                            if(progressBar != null) {
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                            //Save image
                            saveImage(thisContext, loadedImage, finalFileName, finalFolderName, finalSubFolderName);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            else {
                imageView.setImageResource(R.drawable.twitch_default_user);
            }
        }

        //If file exists then display the local version rather than downloading a new version
        else {
            try {
                //Local path for imageloader to load image from
                String filePath = getFilePath(context, finalFileName, finalFolderName, finalSubFolderName);
                //If file exists
                if(filePath != null) {
                    imageUrl = "file://" + filePath;

                    //Load image, after image has been loaded hide the progress spinner
                    imageLoader.loadImage(imageUrl, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            imageView.setImageBitmap(loadedImage);
                            if (progressBar != null) {
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
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

    //Recursive function to get size of image data from folders
    float getImageDataSize(File path) {
        float bytes = 0;

        //If neither folder exists, there is no data
        if (!path.exists()) {
            return 0;
        }

        try {
            File[] fileArray = path.listFiles();
            //Loop through each file in path
            for (File file: fileArray) {
                //If file is a directory, loop through each file within that
                if(file.isDirectory()) {
                    bytes += getImageDataSize(file);
                } else {
                    bytes += file.length();
                }
            }
            //Return size
            return bytes;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    //Recursive function to delete all files within a folder and then the folder
    boolean deleteFile(File path) {

        if(path.isDirectory()) {
            for(File childFile : path.listFiles()) {
                deleteFile(childFile);
            }
        }

        boolean success = path.delete();
        if(!success) {
            createToast("Failed to delete file");
        }

        return success;
    }

    //Deletes all possible images stored for a favourite
    void deleteFavouriteImages(String name) {
        //Delete folder, function handles deleting all files within the folder
        File file = new File(new File(thisContext.getFilesDir(), "favourites"), name);
        if(file.exists()) {
            deleteFile(file);
        }
    }

    //Have to create toast in UI thread
    void createToast(String message ) {
        final String toastText = message;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(thisContext, "ERROR: " + toastText, Toast.LENGTH_LONG).show();
            }
        });
    }
}
