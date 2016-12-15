package uk.ac.lincoln.bwaterman.assignmentapp;

import android.content.Context;
import android.graphics.Bitmap;
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
import java.util.ArrayList;

public class GridAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> textList = new ArrayList<>();
    private ArrayList<String> imageUrlList = new ArrayList<>();
    private ArrayList<String> titleList = new ArrayList<>();
    private boolean isLarge;
    private ImageLoader imageLoader;
    private FileHelper fileHelper;

    public GridAdapter(Context c, ArrayList<String> _textList, ArrayList<String> _imageUrlList, ArrayList<String> _titleList, boolean _isLarge) {
        mContext = c;
        textList = _textList;
        imageUrlList = _imageUrlList;
        titleList = _titleList;
        isLarge = _isLarge;
        fileHelper = new FileHelper();

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

        //Name folder depending on if games or streamers
        String folderName;
        if(isLarge) {
            //For streamers
            folderName = "streamers";
        }
        else {
            //For games
            folderName = "games";
        }

        //Save subfolder name as title of stream/game
        String subFolderName = titleList.get(position);
        //Replace blank space
        subFolderName = subFolderName.replace(" ", "%");
        //Replace any "/"
        subFolderName = subFolderName.replace("/", "%");

        //Name file
        String fileName = "thumbnail";
        //Add file extension
        fileName += ".jpg";

        ///Path to parent folder
        File parentPath = new File(mContext.getFilesDir(), folderName);
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
        if(!file.exists()) {
            try {
                //Load image, after image has been loaded hide the progress spinner
                imageLoader.loadImage(imageUrl, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        imageView.setImageBitmap(loadedImage);
                        progressBar.setVisibility(View.INVISIBLE);
                        //Save image
                        fileHelper.saveImage(mContext, loadedImage, finalFileName, finalFolderName, finalSubFolderName);
                        //Record saved image to savedImages.txt
                        fileHelper.saveText(mContext, "savedImages.txt", finalFolderName + "\\" + finalFileName);
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
                //imageUrl = "file://" + file.getCanonicalPath();
                imageUrl = "file://" + fileHelper.getFilePath(mContext, finalFileName, finalFolderName, finalSubFolderName);

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
}


