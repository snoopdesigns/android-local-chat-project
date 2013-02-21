package com.home.simplechatapplication;

import java.util.ArrayList;

import com.home.simplechatapplication.UserInformation.userInfo.privateMessage;

import android.util.Log;

public class UserInformation {
	public static final String TAG = "UserInformation";
	public class userInfo {
		private String name;
		private int unreadMessages;
		private String status;
		ArrayList<privateMessage> messages = new ArrayList<privateMessage>();
		public class privateMessage {
			private String name;
			private String message;
			privateMessage(String username,String message)
			{
				this.name = username;
				this.message = message;
			}
			public String getName()
			{
				return this.name;
			}
			public String getMessage()
			{
				return this.message;
			}
		}
		userInfo(String name,String status,int unread)
		{
			this.name = name;
			this.status = status;
			this.unreadMessages = unread;
		}
		public void addMessage(String username, String message)
		{
			privateMessage mes = new privateMessage(username,message);
			this.messages.add(mes);
		}
		public ArrayList<privateMessage> getMessages()
		{
			return this.messages;
		}
	}
	private ArrayList<userInfo> userInfoList = new ArrayList<userInfo>();
	UserInformation()
	{
		Log.i(TAG,"UserInformation init");
	}
	public int getUserIdByName(String name)
	{
		for(int i=0;i<this.userInfoList.size();i++)
		{
			if(this.userInfoList.get(i).name.equals(name))
				return i;
		}
		return -1;
	}
	public void addUserMessage(String user, String message, String fromUser)
	{
		int userId = this.getUserIdByName(user);
		if(userId != -1)
		{
			this.userInfoList.get(userId).addMessage(fromUser,message);
		}
	}
	public void addNewUser(String name)
	{
		userInfo user = new userInfo(name,"",0);
		this.userInfoList.add(user);
	}
	public String getUserStatus(String name)
	{
		int userId = this.getUserIdByName(name);
		if(userId != -1)
		{
			return this.userInfoList.get(userId).status;
		}
		else
			return "";
	}
	public void setUserStatus(String name,String status)
	{
		int userId = this.getUserIdByName(name);
		if(userId != -1)
		{
			this.userInfoList.get(userId).status = status;
		}
	}
	public void updateUnreadMessages(String name)
	{
		int userId = this.getUserIdByName(name);
		if(userId != -1)
		{
			this.userInfoList.get(userId).unreadMessages++;
		}
	}
	public void resetUnreadMessages(String name)
	{
		int userId = this.getUserIdByName(name);
		if(userId != -1)
		{
			this.userInfoList.get(userId).unreadMessages = 0;
		}
	}
	public int getUnreadMessages(String name)
	{
		int userId = this.getUserIdByName(name);
		if(userId != -1)
		{
			return this.userInfoList.get(userId).unreadMessages;
		}
		else
			return 0;
	}
	public ArrayList<privateMessage> getMessages(String username)
	{
		int userId = this.getUserIdByName(username);
		if(userId != -1)
		{
			return this.userInfoList.get(userId).getMessages();
		}
		return null;
	}
}
