package it.osm.gtfs.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadUtils {
	public static void downlod(String url, File dest) throws MalformedURLException, IOException{
		System.out.println("Downloading " + url);
		BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
		FileOutputStream fos = new FileOutputStream(dest);
		BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
		byte[] data = new byte[1024];
		int x=0;
		while((x=in.read(data,0,1024))>=0){
			bout.write(data,0,x);
		}
		bout.close();
		in.close();
	}
}
