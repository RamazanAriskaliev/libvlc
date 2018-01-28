package org.videolan.libvlc.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.util.Log;

public class DateUtil {
/*	public static boolean isOverTimeS(String dateString) {
		Date d;
		try {
			Date nowdate = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss",
					Locale.CHINA);
			d = sdf.parse(dateString);
			boolean flag = d.before(nowdate);
			return flag;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean isOverTimeD(String dateString) {
		Date d;
		try {
			Date nowdate = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
					Locale.CHINA);
			d = sdf.parse(dateString);
			boolean flag = d.before(nowdate);
			return flag;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	
	public static boolean isWillOver(String dateString,String hours){
		try {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss",
				Locale.CHINA);
			Date date= sdf.parse(dateString);
			long datelong=date.getTime();
			Date dateNow=new Date();
			long dateNowlong=dateNow.getTime();
			long hourlong=Long.parseLong(hours)*60*60*1000;
			if(datelong-dateNowlong<hourlong){
				return true;
			}
			
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			return false;	
	}
	
	public static String getCurrentTime(){
		Date now = new Date(); 
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd HH:mm");//可以方便地修改日期格式
		String time = dateFormat.format(now);
		return time;
	}
	
	public static String makeMintoTime(String timeStr){
		int time=Integer.parseInt(timeStr);
		int hour=time/3600;
		String houtStr=hour+"";
		 if(hour<=9){
			 houtStr= "0"+hour;
		    }
	    int min=(time-(hour*3600))/60;
	    String minStr=min+"";
	    if(min<=9){
	    	minStr= "0"+min;
		    }
	    int second=time-(hour*3600)-(min*60);
	    String secondStr=second+"";
	    if(second<=9){
	    	secondStr="0"+second;
		    }
	    
	    return houtStr+":"+minStr+":"+secondStr;
	}
	
	public static String makesecondtoMin(String secondStr){
		int second=Integer.parseInt(secondStr);
	    int min=second/60;
	    return min+"";
	
	}*/

	//格式化时间
	public static CharSequence formatTime(long time) {
		Date date = new Date(time);
		SimpleDateFormat aFormat = new SimpleDateFormat("mm:ss ");
		aFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
		return aFormat.format(date);
	}
}
