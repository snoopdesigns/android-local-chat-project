package com.home.simplechatapplication;

import android.util.Log;

public class MessageParser {
	public static final String TAG = "MessageParser";
	
    MessageParser()
    {
    	
    }
    public tMessage parseMessage(String message)
    {
    	String name = findSequenceBetweenTags(message, "[name]", "[data]");
    	String data = findSequenceBetweenTags(message, "[data]", "[action]");
    	String action = findSequenceBetweenTags(message, "[action]", "[end]");
    	tMessage mes = new tMessage(name,action,data);
    	return mes;
    }
    public String createMessage(tMessage mes)
    {
    	return "[name]"+mes.name+"[data]"+mes.data+"[action]"+mes.action+"[end]";
    }
    public String findSequenceBetweenTags(String message,String tag1, String tag2) 
    {
    	String name = "";
    	try {
    	int posBeg = message.lastIndexOf(tag1);
    	int posEnd = message.lastIndexOf(tag2);
    	String trimmed = message.subSequence(posBeg, posEnd).toString();
    	name = trimmed.replace(tag1, "").subSequence(0, trimmed.length()-tag1.length()).toString();
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	return name;
    }
}

