package com.bbpp.shashinshu;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import cn.waps.AppConnect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		AppConnect.getInstance(this);
		
		createInitAlbum();
		
		
	}	

	@Override
	protected void onDestroy() {
		AppConnect appConn = AppConnect.getInstance(this);
		if (appConn != null)
			appConn.finalize();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void createInitAlbum() {
		new initializeTask().execute();
	}

	public class initializeTask extends AsyncTask<Void,Void,Void>{

		@Override
		protected void onPostExecute(Void result) {
			dialog.dismiss();
			Intent intent = new Intent(MainActivity.this, AlbumActivity.class);
			intent.putExtra(AlbumActivity.PHOTO_FOLDER, "shashinshu");
			startActivity(intent);
		}

		ProgressDialog dialog;		

		protected void onPreExecute(){
			dialog = new ProgressDialog(MainActivity.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			//dialog.setMessage(getResources().getString(R.string.init_message));
			dialog.setMax(100);			
			dialog.show();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			
			File dir = new File(getExternalFilesDir(null), "shashinshu");			
			dir.mkdir();

			DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
			int screenWidth = dm.widthPixels;
			int screenHeight = dm.heightPixels;

			Resources myResources = getResources();
			
			try {
				BitmapUtils.saveBitmapToFile(
						BitmapUtils.decodeSampledBitmapFromResource(myResources, R.raw.p1, screenWidth, screenHeight),
						new File(dir, "p1").getAbsolutePath());
				BitmapUtils.saveBitmapToFile(
						BitmapUtils.decodeSampledBitmapFromResource(myResources, R.raw.p2, screenWidth, screenHeight),
						new File(dir, "p2").getAbsolutePath());
				BitmapUtils.saveBitmapToFile(
						BitmapUtils.decodeSampledBitmapFromResource(myResources, R.raw.p3, screenWidth, screenHeight),
						new File(dir, "p3").getAbsolutePath());
				BitmapUtils.saveBitmapToFile(
						BitmapUtils.decodeSampledBitmapFromResource(myResources, R.raw.p4, screenWidth, screenHeight),
						new File(dir, "p4").getAbsolutePath());
				BitmapUtils.saveBitmapToFile(
						BitmapUtils.decodeSampledBitmapFromResource(myResources, R.raw.p5, screenWidth, screenHeight),
						new File(dir, "p5").getAbsolutePath());
				BitmapUtils.saveBitmapToFile(
						BitmapUtils.decodeSampledBitmapFromResource(myResources, R.raw.p6, screenWidth, screenHeight),
						new File(dir, "p6").getAbsolutePath());
				BitmapUtils.saveBitmapToFile(
						BitmapUtils.decodeSampledBitmapFromResource(myResources, R.raw.p7, screenWidth, screenHeight),
						new File(dir, "p7").getAbsolutePath());
				BitmapUtils.saveBitmapToFile(
						BitmapUtils.decodeSampledBitmapFromResource(myResources, R.raw.p8, screenWidth, screenHeight),
						new File(dir, "p8").getAbsolutePath());
				BitmapUtils.saveBitmapToFile(
						BitmapUtils.decodeSampledBitmapFromResource(myResources, R.raw.p9, screenWidth, screenHeight),
						new File(dir, "p9").getAbsolutePath());				
			} catch (IOException e) {			
				e.printStackTrace();
			}
					
			return null;
		}
	}

}
