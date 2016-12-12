package uk.ac.lincoln.bwaterman.assignmentapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TabHost;
import android.widget.TextView;

public class TwitchChannel extends AppCompatActivity {

    String channelName;
    String channelFollowers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitch_channel);

        TabHost tabHost = (TabHost)findViewById(R.id.channelTabs);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("tag1");
        tabSpec.setContent(R.id.infoTab);
        tabSpec.setIndicator("INFO");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setContent(R.id.videosTab);
        tabSpec.setIndicator("VIDEOS");
        tabHost.addTab(tabSpec);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            channelName = extras.getString("channelName");
            channelFollowers = extras.getString("channelFollowers");
        }
        TextView titleText = (TextView) findViewById(R.id.channelTitleText);
        titleText.setText(channelName);
        titleText = (TextView) findViewById(R.id.channelSubtitleText);
        titleText.setText(channelFollowers + " followers");
    }
}
