package uk.ac.lincoln.bwaterman.assignmentapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class GridAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> textList = new ArrayList<>();
    private ArrayList<String> imageUrlList = new ArrayList<>();
    private ArrayList<String> titleList = new ArrayList<>();
    private boolean isLarge;
    private ImageLoader imageLoader;

    public GridAdapter(Context c, ArrayList<String> _textList, ArrayList<String> _imageUrlList, ArrayList<String> _titleList, boolean _isLarge) {
        mContext = c;
        textList = _textList;
        imageUrlList = _imageUrlList;
        titleList = _titleList;
        isLarge = _isLarge;

        if(imageLoader == null)
            imageLoader = ImageLoader.getInstance();
    }

    @Override
    public int getCount() {
        return imageUrlList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            grid = new View(mContext);
            //Use different layout for different activities
            if(isLarge)
                grid = inflater.inflate(R.layout.grid_single_large, null);
            else
                grid = inflater.inflate(R.layout.grid_single, null);
        } else {
            grid = (View) convertView;
        }

        TextView textView = (TextView) grid.findViewById(R.id.grid_text);
        final ImageView imageView = (ImageView)grid.findViewById(R.id.grid_image);

        //Show progress spinner while image loads
        final ProgressBar progressBar = (ProgressBar)grid.findViewById(R.id.progress);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);

        //Use fromHtml in order to get multiple styles within one textView (such as bold and non bold text)
        textView.setText(Html.fromHtml(textList.get(position)));
        String imageUrl = imageUrlList.get(position);

        FileOutputStream outputStream;
        String filename;
        //Save image name as title of stream/game
        filename = titleList.get(position);
        //Replace blank space
        filename = filename.replace(" ", "%");
        //Replace any "/"
        filename = filename.replace("/", "%");
        //Add file extension
        filename += ".jpg";
        //Create file path
        File file = mContext.getFileStreamPath(filename);

        //Create final so can be used in function below
        final String finalFileName = filename;

        //If there is no file, download it, display it and save it to internal storage
        if(file == null || !file.exists()) {
            try {
                //Load image, after image has been loaded hide the progress spinner
                imageLoader.loadImage(imageUrl, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        imageView.setImageBitmap(loadedImage);
                        progressBar.setVisibility(View.INVISIBLE);
                        //Save image
                        saveImage(mContext, loadedImage, finalFileName);
                        //Record saved image to savedImages.txt
                        saveText(mContext, "savedImages.txt", finalFileName);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //If file exists then display the local version rather than downloading a new version
        else {
            try {
                //Local path for imageloader to load image from
                imageUrl = "file://" + file.getCanonicalPath();

                //Load image, after image has been loaded hide the progress spinner
                imageLoader.loadImage(imageUrl, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        imageView.setImageBitmap(loadedImage);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return grid;
    }

    //Function to save image to internal storage
    public void saveImage(Context context, Bitmap bm, String name){
        FileOutputStream out;
        try {
            out = context.openFileOutput(name, Context.MODE_PRIVATE);
            bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
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
}


