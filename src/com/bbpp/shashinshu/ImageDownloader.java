package com.bbpp.shashinshu;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;


public class ImageDownloader {	

	public static ArrayList<String> getImageURL() {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String today = format.format(new Date());

		ArrayList<ImageInfo> imageInfo = new ArrayList<ImageInfo>();
		ArrayList<String> result = new ArrayList<String>();
		InputStream is = null;

		try {
			URL url = new URL("http://image.baidu.com/channel/listjson?fr=channel&tag1=%E7%BE%8E%E5%A5%B3&tag2=%E5%85%A8%E9%83%A8&sorttype=0&pn=" + "0" + "&rn=1024&ie=utf8&oe=utf-8&" + System.currentTimeMillis());

			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setReadTimeout(30000 /* milliseconds */);
			conn.setConnectTimeout(30000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.connect();
			
			if (HttpURLConnection.HTTP_OK == conn.getResponseCode()) {
				is = conn.getInputStream();
				JsonFactory f = new JsonFactory();
				JsonParser jp = f.createParser(is);
			
				jp.nextToken();
				while (jp.nextToken() != JsonToken.END_OBJECT) {

					String fieldname = jp.getCurrentName();
					jp.nextToken();
					if ("data".equalsIgnoreCase(fieldname)) {
						while (jp.nextToken() != JsonToken.END_ARRAY) {	
							ImageInfo ii = new ImageInfo();
							//Date date = dby;
							//Date date = new Date();
							String date = "";
							while (jp.nextToken() != JsonToken.END_OBJECT) {
								fieldname = jp.getCurrentName();								
								jp.nextToken();
								if ("download_url".equalsIgnoreCase(fieldname)) {
									ii.url = jp.getText().trim();
								} else if ("date".equalsIgnoreCase(fieldname)) {
									date = jp.getText();
								} else if ("download_num".equalsIgnoreCase(fieldname)) {
									ii.download_num = jp.getIntValue();
								}
							}
							if (date.equals(today)) {
								
								imageInfo.add(ii);
							}
						}
					}
				}
				jp.close();
			}

		} catch (IOException ex) {
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {}
		}
		
		Collections.sort(imageInfo);
		for (int i = 0; i < 10 + AlbumActivity.coin / 10 && i < imageInfo.size(); ++i) {
			result.add(imageInfo.get(i).url);
		}
		return result;

	}

	private static class ImageInfo implements Comparable<ImageInfo> {
		public String url;
		public int download_num;
		
		@Override
		public int compareTo(ImageInfo o) {
			return o.download_num - download_num;
		}		
	}
}
