package uk.ac.lincoln.bwaterman.assignmentapp;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class GridAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> textList = new ArrayList<>();
    private ArrayList<String> imageUrlList = new ArrayList<>();
    private ArrayList<String> titleList = new ArrayList<>();
    private ImageType imageType;
    private ImageLoader imageLoader;
    private FileHelper fileHelper;
    private boolean isFavourite;

    public GridAdapter(Context c, ArrayList<String> _textList, ArrayList<String> _imageUrlList, ArrayList<String> _titleList, ImageType _imageType, boolean _isFavourite) {
        mContext = c;
        textList = _textList;
        imageUrlList = _imageUrlList;
        titleList = _titleList;
        imageType = _imageType;
        isFavourite = _isFavourite;

        fileHelper = new FileHelper(c);

        if (imageLoader == null)
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

            switch(imageType) {
                case GAME:
                    grid = inflater.inflate(R.layout.grid_single, null);
                    break;

                case STREAM:
                    grid = inflater.inflate(R.layout.grid_single_large, null);
                    break;

                case LOGO:
                    grid = inflater.inflate(R.layout.grid_single_logo, null);
                    break;

                default:
                    grid = inflater.inflate(R.layout.grid_single, null);
                    break;
            }
        } else {
            grid = (View) convertView;
        }

        TextView textView = (TextView) grid.findViewById(R.id.grid_text);
        ImageView imageView = (ImageView) grid.findViewById(R.id.grid_image);

        //Show progress spinner while image loads
        ProgressBar progressBar = (ProgressBar) grid.findViewById(R.id.progress);

        //Use fromHtml in order to get multiple styles within one textView (such as bold and non bold text)
        textView.setText(Html.fromHtml(textList.get(position)));
        String imageUrl = imageUrlList.get(position);

        //Display the image
        fileHelper.displayImage(mContext, imageType, imageUrl, titleList.get(position), imageView, progressBar, isFavourite);

        //Return the grid to be displayed
        return grid;
    }
}