package com.home.simplechatapplication;

import android.util.Log;

public class tMessage
{
	public static final String TAG = "MessageParser";
	String name;
	String data;
	String action;
	tMessage(String name,String action,String data)
	{
		this.name = name;
		this.action = action;
		this.data = data;
	}
	public void printMessage()
	{
		Log.i(TAG,"Name = "+name+", data = "+data);
	}
}
