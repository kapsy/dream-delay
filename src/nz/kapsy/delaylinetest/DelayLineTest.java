package nz.kapsy.delaylinetest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

import org.puredata.android.service.PdPreferences;
import org.puredata.android.service.PdService;
import org.puredata.core.PdBase;
import org.puredata.core.PdReceiver;
import org.puredata.core.utils.IoUtils;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;

import android.app.Activity;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class DelayLineTest extends Activity {

	private static final String TAG = "Pd Test";

	private PdService pdService = null;
	private TextView text;

	float bufferSize = 100;
	int sampleRate = 22050;
	int inChan = 0;
	int outChan = 2;

	private final ServiceConnection pdConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "onServiceConnected");
			pdService = ((PdService.PdBinder)service).getService();
			initPd();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// this method will never be called
		}
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		bindService(new Intent(this, PdService.class), pdConnection, BIND_AUTO_CREATE);

		Button bangvol = (Button)this.findViewById(R.id.button1);
		bangvol.setOnClickListener(new BangVolClickAdapter());

		//s vol_fader_rec
		SeekBar volfader = (SeekBar)this.findViewById(R.id.seekBar1);
		volfader.setOnSeekBarChangeListener(new VolChangeAdapter());

		text = (TextView)this.findViewById(R.id.textView1);

    };

    class BangVolClickAdapter implements OnClickListener {

    	@Override
    	public void onClick(View v) {
    		sendBang("bangvol");
    	}
    }

    class VolChangeAdapter implements OnSeekBarChangeListener {

    	@Override
    	public void onProgressChanged(SeekBar seekBar, int i, boolean flag) {

    		float d = (float)i / 100;
    		sendFloat("vol_fader_rec", d);
    		text.setText("現在の値：" + d);


		}

		@Override
		public void onStartTrackingTouch(SeekBar seekbar) {
			// TODO 自動生成されたメソッド・スタブ
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekbar) {
			// TODO 自動生成されたメソッド・スタブ
		}

    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cleanup();
	}


    	private void initPd() {

		try {
			File dir = getFilesDir();
			File patchFile = new File(dir, "sandbox_004_androidtest.pd");
			IoUtils.extractZipResource(getResources().openRawResource(R.raw.patch), dir, true);
			PdBase.openPatch(patchFile.getAbsolutePath());

			startAudio();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			finish();
		}
	}

		private void startAudio() {
		String name = getResources().getString(R.string.app_name);
		try {
			pdService.initAudio(sampleRate, inChan, outChan, bufferSize);   // negative values will be replaced with defaults/preferences
			pdService.startAudio(new Intent(this, DelayLineTest.class), R.drawable.icon, name, "Return to " + name + ".");
		} catch (IOException e) {
			//toast(e.toString());
			Log.d(TAG, e.toString());
		}
	}

		private void cleanup() {
		try {
			unbindService(pdConnection);
		} catch (IllegalArgumentException e) {
			// already unbound
			pdService = null;
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	 public void sendBang(String s) {
	   PdBase.sendBang(s);
	 }

	 public void sendFloat(String s, float f) {
		 PdBase.sendFloat(s, f);
	 }

 /* Here is a more complex example of how to send a list of data to Pd.
    Here we're assuming that s looks like a Pd-list,
    for example s="foo bar blah".
    See also PdBase.sendFloat() and PdBase.sendSymbol()
 public void send(String dest, String s) {
   String[] pieces = s.split(" ");
   Object[] list = new Object[pieces.length];

   for (int i=0; i < pieces.length; i++) {
     try {
       list[i] = Float.parseFloat(pieces[i]);
     } catch (NumberFormatException e) {
       list[i] = pieces[i];
     }
   }

   PdBase.sendList(dest, list);
 }*/









}
