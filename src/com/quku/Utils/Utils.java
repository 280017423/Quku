package com.quku.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.widget.Toast;

public class Utils {

	public static void getToast(Context context, String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	public static String data2String(Date date, String formact) {
		SimpleDateFormat formatter = new SimpleDateFormat(formact);
		return formatter.format(date);
	}

	public static Date String2Date(String dateString, String formact) {
		SimpleDateFormat formatter = new SimpleDateFormat(formact);
		try {
			return formatter.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
			return new Date();
		}
	}

	/**
	 * 根据URL对象返回inputStream
	 * 
	 * @return
	 */
	public static double getVersion() {
		InputStream is = null;
		URL url = null;
		try {
			url = new URL(SystemDef.System.URL_VERSION);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.connect();// 获取连接,也可以省略
			is = conn.getInputStream();
			if (null != is) {
				byte b[] = new byte[1024];
				int len = 0;
				int temp = 0; // 所有读取的内容都使用temp接收
				while ((temp = is.read()) != -1) { // 当没有读取完时，继续读取
					b[len] = (byte) temp;
					len++;
				}
				is.close();
				String version = new String(b, 0, len);
				System.out.println("version = " + version);
				if (null != version && version.contains("=")) {
					String value = version.split("=")[1];
					return Double.valueOf(value);
				}
			}
		} catch (MalformedURLException e) {
			System.out.println(" 获取URL对象失败");
			return 0;
		} catch (IOException e) {
			System.out.println(" 读取文件失败");
			return 0;
		}
		return 0;
	}

	/**
	 * 根据URL对象返回inputStream
	 * 
	 * @return
	 */
	public static String getVersionInfo() {
		InputStream is = null;
		URL url = null;
		BufferedReader br = null;
		try {
			url = new URL(SystemDef.System.URL_VERSION_INFO);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.connect();// 获取连接,也可以省略
			is = conn.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			StringBuffer sb = new StringBuffer();
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
			System.out.println(" sb = " + sb.toString());
			return sb.toString();
			// if(null != is){
			// byte b[] = new byte[1024];
			// int len = 0;
			// int temp=0; //所有读取的内容都使用temp接收
			// while((temp=is.read())!=-1){ //当没有读取完时，继续读取
			// b[len]=(byte)temp;
			// len++;
			// }
			// is.close();
			// String versionInfo = new String(b,0,len,"UTF-8");
			// System.out.println("versionInfo = " + versionInfo);
		} catch (MalformedURLException e) {
			System.out.println(" 获取URL对象失败");
			return null;
		} catch (IOException e) {
			System.out.println(" 读取文件失败");
			return null;
		}
	}
}
