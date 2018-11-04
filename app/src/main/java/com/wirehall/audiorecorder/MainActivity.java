package com.wirehall.audiorecorder;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.wirehall.audiorecorder.explorer.FileListFragment;
import com.wirehall.audiorecorder.explorer.model.Recording;
import com.wirehall.audiorecorder.mp.MediaPlayerController;
import com.wirehall.audiorecorder.mr.MediaRecorderState;
import com.wirehall.audiorecorder.mr.RecordingController;
import com.wirehall.audiorecorder.setting.SettingActivity;
import com.wirehall.audiorecorder.visualizer.VisualizerFragment;

public class MainActivity extends AppCompatActivity implements VisualizerFragment.VisualizerMPSession, FileListFragment.FileListFragmentListener {

    public static final int PERMISSION_REQUEST_CODE = 111;
    public static final String[] APP_PERMS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private RecordingController recordingController = RecordingController.getInstance();
    private MediaPlayerController mediaPlayerController = MediaPlayerController.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ActivityCompat.requestPermissions(this, APP_PERMS, PERMISSION_REQUEST_CODE);
        mediaPlayerController.init(this);
        recordingController.init(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Read settings
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean switchPref = sharedPref.getBoolean(SettingActivity.KEY_PREF_CONFIRM_DELETE, false);
        Toast.makeText(this, switchPref.toString(), Toast.LENGTH_SHORT).show();
    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void recordPauseBtnClicked(View view) {
        mediaPlayerController.stopPlaying(this);
        recordingController.startPauseRecording(this);
    }

    /**
     * @param view The method is the click handler for recorder delete button
     */
    public void deleteBtnClicked(View view) {
        ImageButton btnDelete = (ImageButton) view;
    }


    /**
     * @param view The method is the click handler for recorder stop button
     */
    public void stopBtnClicked(View view) {
        recordingController.stopRecording(this);
    }

    @Override
    public int getAudioSessionIdOfMediaPlayer() {
        return mediaPlayerController.getAudioSessionId();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isPermissionAccepted = false;
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                isPermissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (isPermissionAccepted) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.list_fragment_container, FileListFragment.newInstance());
            ft.add(R.id.visualizer_fragment_container, VisualizerFragment.newInstance());
            ft.commit();
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_item_settings:
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFileItemClicked(Recording recording) {
        if (recordingController.getMediaRecorderState() != MediaRecorderState.STOPPED) {
            Toast.makeText(getApplicationContext(), "Can't play while recording. Stop the recording first!", Toast.LENGTH_SHORT).show();
            return;
        }
        mediaPlayerController.playPauseAudio(this, recording);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayerController.releaseMediaPlayer();
        recordingController.releaseRecorder();
    }
}
