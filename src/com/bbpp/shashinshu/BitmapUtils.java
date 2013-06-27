package com.bbpp.shashinshu;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class BitmapUtils {
	
	public static Bitmap decodeSampledBitmapFromStream(InputStream is, int reqWidth, int reqHeight) {
		// First decode with inJustDecodeBounds=true to check dimensions
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				
				Bitmap bitmap = BitmapFactory.decodeStream(is);
				
				if (bitmap == null)
					return null;

				boolean rotation = false;
				if ((options.outHeight > options.outWidth && reqHeight < reqWidth) || (options.outHeight < options.outWidth && reqHeight > reqWidth)) {
					rotation = true;
				}
				
				options.outWidth = bitmap.getWidth();
				options.outHeight = bitmap.getHeight();

				// Calculate inSampleSize
				if (rotation) {
					if (bitmap.getWidth() > reqHeight || bitmap.getHeight() > reqWidth) {
						float rate1 = ((float)bitmap.getWidth()) / reqHeight;
						float rate2 = ((float)bitmap.getHeight()) / reqWidth;
						if (rate2 > rate1)
							rate1 = rate2;
						reqHeight = Math.round(rate1 * bitmap.getWidth());
						reqWidth = Math.round(rate1 * bitmap.getHeight());
						bitmap = Bitmap.createScaledBitmap(bitmap, reqHeight, reqWidth, false);
					}
				} else {					
					if (bitmap.getWidth() > reqWidth || bitmap.getHeight() > reqHeight) {
						float rate1 = ((float)bitmap.getWidth()) / reqWidth;
						float rate2 = ((float)bitmap.getHeight()) / reqHeight;
						if (rate2 > rate1)
							rate1 = rate2;
						reqWidth = Math.round(rate1 * bitmap.getWidth());
						reqHeight = Math.round(rate1 * bitmap.getHeight());
						bitmap = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, false);
					}
				}
									
				if (rotation) {
					Matrix m = new Matrix();
					m.setRotate(90);
					bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, false);
				}
				
				return bitmap;
	}

	public static Bitmap decodeSampledBitmapFromResource(Resources res, int id, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, id, options);

		boolean rotation = false;
		if ((options.outHeight > options.outWidth && reqHeight < reqWidth) || (options.outHeight < options.outWidth && reqHeight > reqWidth)) {
			rotation = true;
		}

		// Calculate inSampleSize
		if (rotation)
			options.inSampleSize = calculateInSampleSize(options, reqHeight, reqWidth);
		else 
			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeResource(res, id, options);
		
		if (rotation) {
			if (bitmap.getWidth() > reqHeight || bitmap.getHeight() > reqWidth) {
				float rate1 = ((float)bitmap.getWidth()) / reqHeight;
				float rate2 = ((float)bitmap.getHeight()) / reqWidth;
				if (rate2 > rate1)
					rate1 = rate2;
				reqHeight = Math.round(rate1 * bitmap.getWidth());
				reqWidth = Math.round(rate1 * bitmap.getHeight());
				bitmap = Bitmap.createScaledBitmap(bitmap, reqHeight, reqWidth, false);
			}
		} else {					
			if (bitmap.getWidth() > reqWidth || bitmap.getHeight() > reqHeight) {
				float rate1 = ((float)bitmap.getWidth()) / reqWidth;
				float rate2 = ((float)bitmap.getHeight()) / reqHeight;
				if (rate2 > rate1)
					rate1 = rate2;
				reqWidth = Math.round(rate1 * bitmap.getWidth());
				reqHeight = Math.round(rate1 * bitmap.getHeight());
				bitmap = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, false);
			}
		}
		
		if (rotation) {
			Matrix m = new Matrix();
			m.setRotate(90);
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, false);
		}
		
		return bitmap;
	}


	public static Bitmap decodeSampledBitmapFromFile(String imagePath, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, options);

		boolean rotation = false;
		if ((options.outHeight > options.outWidth && reqHeight < reqWidth) || (options.outHeight < options.outWidth && reqHeight > reqWidth)) {
			rotation = true;
		}
		// Calculate inSampleSize
		if (rotation)
			options.inSampleSize = calculateInSampleSize(options, reqHeight, reqWidth);
		else 
			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

		if (rotation) {
			Matrix m = new Matrix();
			m.setRotate(90);
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, false);
		}		
		return bitmap;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	public static void saveBitmapToFile(Bitmap bitmap, String filePath) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes);

		File f = new File(filePath);
		FileOutputStream fo = null;
		try {
			f.createNewFile();			
			fo = new FileOutputStream(f);
			fo.write(bytes.toByteArray());
		} finally {
			if (fo != null) {
				fo.close();
			}
		}
	}

}
