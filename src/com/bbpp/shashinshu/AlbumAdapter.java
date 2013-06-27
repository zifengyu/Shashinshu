package com.bbpp.shashinshu;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.aphidmobile.flip.FlipViewController;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class AlbumAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private FlipViewController controller;
	private Context context;
	
	ExecutorService executorService =Executors.newCachedThreadPool();

	private ArrayList<String> photoList;
	private static Bitmap placeholderBitmap = null;

	private LruCache<String, Bitmap> mMemoryCache;

	public AlbumAdapter(Context context, FlipViewController controller, ArrayList<String> photoList) {
		inflater = LayoutInflater.from(context);
		this.context = context;
		this.controller = controller;

		//if (placeholderBitmap == null)
		//placeholderBitmap = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_gallery);

		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		// Use 1/8th of the available memory for this memory cache.
		final int cacheSize = maxMemory / 8;
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
			}
		};
		
		this.photoList = photoList;

	}

	@Override
	public int getCount() {		
		return photoList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View layout = convertView;
		if (convertView == null) {
			layout = inflater.inflate(R.layout.album_page, parent, false);	      
		}

		ImageView photoView = (ImageView)layout.findViewById(R.id.photoView);

		//Use an async task to load the bitmap
		String imageName = photoList.get(position);
		boolean needReload = true;
		AsyncImageTask previousTask = AsyncDrawable.getTask(photoView);
		if (previousTask != null) {
			if (previousTask.getPageIndex() == position && previousTask.getImageName().equals(imageName)) //check if the convertView happens to be previously used
			{
				needReload = false;
			} else {
				previousTask.cancel(true);
			}
		}

		if (needReload) {
			final Bitmap bitmap = mMemoryCache.get(imageName);
			if (bitmap != null) {
				photoView.setImageBitmap(bitmap);
			} else {				
				AsyncImageTask task = new AsyncImageTask(photoView, controller, position, imageName, context.getApplicationContext().getResources().getDisplayMetrics());
				photoView.setImageDrawable(new AsyncDrawable(context.getResources(), placeholderBitmap, task));
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
					task.executeOnExecutor(executorService);
				else
					task.execute();
			}
		}

		return layout;
	}

	private static final class AsyncDrawable extends BitmapDrawable {

		private final WeakReference<AsyncImageTask> taskRef;

		public AsyncDrawable(Resources res, Bitmap bitmap, AsyncImageTask task) {
			super(res, bitmap);
			this.taskRef = new WeakReference<AsyncImageTask>(task);
		}

		public static AsyncImageTask getTask(ImageView imageView) {
			Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				return ((AsyncDrawable) drawable).taskRef.get();
			}

			return null;
		}
	}


	private final class AsyncImageTask extends AsyncTask<Void, Void, Bitmap> {

		private final WeakReference<ImageView> imageViewRef;
		private final WeakReference<FlipViewController> controllerRef;
		private final int pageIndex;
		private final String imageName;
		DisplayMetrics dm;

		public AsyncImageTask(ImageView imageView, FlipViewController controller, int pageIndex, String imageName, DisplayMetrics dm) {

			imageViewRef = new WeakReference<ImageView>(imageView);
			controllerRef = new WeakReference<FlipViewController>(controller);
			this.pageIndex = pageIndex;
			this.imageName = imageName;
			this.dm = dm;			

		}

		public int getPageIndex() {
			return pageIndex;
		}

		public String getImageName() {
			return imageName;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			int screenWidth = dm.widthPixels;
			int screenHeight = dm.heightPixels;
			Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromFile(imageName, screenWidth, screenHeight);
			if (mMemoryCache.get(imageName) == null) {
				mMemoryCache.put(imageName, bitmap);
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()) {
				return;
			}

			ImageView imageView = imageViewRef.get();
			if (imageView != null && AsyncDrawable.getTask(imageView) == this) { //the imageView can be reused for another page, so it's necessary to check its consistence
				imageView.setImageBitmap(bitmap);
				FlipViewController controller = controllerRef.get();
				if (controller != null) {
					controller.refreshPage(pageIndex);
				}
			}
		}		
	}


}
