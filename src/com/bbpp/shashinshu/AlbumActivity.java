package com.bbpp.shashinshu;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cn.waps.AppConnect;
import cn.waps.UpdatePointsNotifier;

import com.aphidmobile.flip.FlipViewController;
import com.bbpp.shashinshu.MainActivity.initializeTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class AlbumActivity extends Activity implements UpdatePointsNotifier {

	//public static final String PHOTO_FOLDER = "com.bbpp.shashinshu.albumactivity.PHOTOFOLDER";
	public static final String PHOTO_FOLDER = "shashinshu";

	private FlipViewController flipView;

	private ArrayList<String> photoList;

	private String lastUpdate;
	private String today = "";

	private int score = 990;
	public static int coin = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);

		AppConnect.getInstance(this);		
		AppConnect appConn = AppConnect.getInstance(this);
		if (appConn != null)
			appConn.initPopAd(this);


		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		flipView = new FlipViewController(this, FlipViewController.VERTICAL);

		File dir = new File(getExternalFilesDir(null), PHOTO_FOLDER);

		photoList = new ArrayList<String>();		
		File[] fileList = dir.listFiles();
		if (fileList != null) {
			for (int i = 0; i < fileList.length; ++i) {
				photoList.add(fileList[i].getAbsolutePath());
			}
		}		

		flipView.setAdapter(new AlbumAdapter(this, flipView, photoList));

		SharedPreferences pref = getPreferences(MODE_PRIVATE);
		int bookmark = pref.getInt("BOOKMARK", 0);
		if (bookmark < photoList.size() && bookmark > 0)
			flipView.setSelection(bookmark);			
		lastUpdate = pref.getString("LASTUPDATE", "");
		score = pref.getInt("SCORE", 1);
		coin = pref.getInt("COIN", 0);

		appConn = AppConnect.getInstance(this);
		if (appConn != null)
			appConn.getPoints(this);

		if (score % 10 == 0) {
			appConn = AppConnect.getInstance(this);
			if (appConn != null)
				appConn.showPopAd(this);
		}

		setContentView(flipView);

		today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

		if (!today.equals(lastUpdate)) {

			WifiManager wifiManager = (WifiManager)AlbumActivity.this.getSystemService(Context.WIFI_SERVICE);

			if (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
				AlertDialog dialog = new AlertDialog.Builder(AlbumActivity.this)
				.setMessage(getResources().getString(R.string.wifi_message))
				.setPositiveButton(getResources().getString(R.string.yes_button), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						new updateTask().execute();								
					}
				})
				.setNegativeButton(getResources().getString(R.string.no_button), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {													
					}
				})
				.create();
				dialog.show();
			}
			else {
				new updateTask().execute();	
			}
		} else {
			Toast toast = Toast.makeText(AlbumActivity.this, getResources().getString(R.string.update_message4), Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	@Override
	protected void onDestroy() {
		AppConnect appConn = AppConnect.getInstance(this);
		if (appConn != null)
			appConn.finalize();
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		Editor edit = getPreferences(MODE_PRIVATE).edit();
		edit.putInt("BOOKMARK", flipView.getSelectedItemPosition());
		edit.putString("LASTUPDATE", lastUpdate);
		edit.putInt("SCORE", ++score);
		edit.putInt("COIN", coin);
		edit.commit();
		super.onStop();
	}


	@Override
	protected void onPause() {		
		super.onPause();
		flipView.onPause();
	}

	@Override
	protected void onResume() {		
		super.onResume();
		flipView.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {	
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		switch (item.getItemId()) {
		case R.id.action_delete_image:
			int pos = flipView.getSelectedItemPosition();
			String filePath = null;
			synchronized (photoList) {
				if (pos >= 0 && pos < photoList.size()) {					
					filePath = photoList.remove(pos);					
				}				
			}
			if (filePath != null) {
				if (photoList.size() > 0) {
					((AlbumAdapter)flipView.getAdapter()).notifyDataSetChanged();
				} 
				new deleteFileTask().execute(filePath);				
			}
			return true;
		case R.id.action_recommend:
			AppConnect.getInstance(this).showOffers(this);
			return true;			
		case R.id.action_feedback:
			AppConnect.getInstance(this).showFeedback();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public class deleteFileTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... arg0) {
			if (arg0.length == 0)
				return null;
			File file = new File(arg0[0]);
			file.delete();			
			return null;
		}

	}

	public class updateTask extends AsyncTask<Void,Void,Void>{		

		private int updateCount = 0;

		protected void onPreExecute() {			
			Toast toast = Toast.makeText(AlbumActivity.this, getResources().getString(R.string.update_message3), Toast.LENGTH_LONG);
			toast.show();
		}

		@Override
		protected Void doInBackground(Void... arg0) {			

			File dir = new File(getExternalFilesDir(null), PHOTO_FOLDER);

			DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
			int screenWidth = dm.widthPixels;
			int screenHeight = dm.heightPixels;

			Resources myResources = getResources();

			if (!dir.exists()) {
				dir.mkdir();
				for (int i = R.raw.p1; i <= R.raw.p9; ++i) {
					try {
						String filePath = new File(dir, "inline" + i).getAbsolutePath();
						BitmapUtils.saveBitmapToFile(
								BitmapUtils.decodeSampledBitmapFromResource(myResources, i, screenWidth, screenHeight),
								filePath);
						synchronized (photoList) {
							photoList.add(filePath);	
						}					
						++updateCount;
					} catch (IOException e) {			
						e.printStackTrace();
					}
				}
			} else {		
				lastUpdate = today;
				Editor edit = getPreferences(MODE_PRIVATE).edit();
				edit.putString("LASTUPDATE", lastUpdate);				
				edit.commit();
				ArrayList<String> imageList = ImageDownloader.getImageURL();
				if (imageList != null) {					

					for (int i = 0; i < imageList.size(); ++i) {
						InputStream is = null;
						try {
							URL url = new URL(imageList.get(i));	

							HttpURLConnection conn = (HttpURLConnection)url.openConnection();
							conn.setReadTimeout(30000 /* milliseconds */);
							conn.setConnectTimeout(30000 /* milliseconds */);
							conn.setRequestMethod("GET");
							conn.setDoInput(true);
							conn.connect();

							if (HttpURLConnection.HTTP_OK == conn.getResponseCode()) {
								is = conn.getInputStream();
								String filePath = new File(dir, System.currentTimeMillis() + "_" + i).getAbsolutePath();

								BitmapUtils.saveBitmapToFile(
										BitmapUtils.decodeSampledBitmapFromStream(is, screenWidth, screenHeight),
										filePath);
								synchronized (photoList) {
									photoList.add(filePath);	
								}					
								++updateCount;
							}
						} catch (IOException e) {

						} finally {
							if (is != null)
								try {
									is.close();
									is = null;
								} catch (IOException e) {}
						}
					}
				}

			}
			return null;
		}	

		@Override
		protected void onProgressUpdate(Void... values) {

		}

		@Override
		protected void onPostExecute(Void result) {
			if (updateCount > 0) {
				((AlbumAdapter)flipView.getAdapter()).notifyDataSetChanged();
				Toast toast = Toast.makeText(AlbumActivity.this, getResources().getString(R.string.update_message1) + updateCount + getResources().getString(R.string.update_message2), Toast.LENGTH_SHORT);
				toast.show();
			}
		}

	}

	@Override
	public void getUpdatePoints(String arg0, int arg1) {
		coin = arg1;		
	}

	@Override
	public void getUpdatePointsFailed(String arg0) {
		// TODO Auto-generated method stub

	}

}
