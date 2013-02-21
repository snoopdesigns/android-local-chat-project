package com.home.simplechatapplication;

import java.util.ArrayList;

import com.home.simplechatapplication.GroupInformation.groupInfo.groupMessage;

public class GroupInformation {
	private ArrayList<groupInfo> groupInfoList = new ArrayList<groupInfo>();
	public class groupInfo {
		private String name;
		ArrayList<groupMessage> messages = new ArrayList<groupMessage>();
		groupInfo(String name)
		{
			this.name = name;
		}
		public class groupMessage {
			private String name;
			private String message;
			groupMessage(String username,String message)
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
		public void addMessage(String username, String message)
		{
			groupMessage mes = new groupMessage(username,message);
			this.messages.add(mes);
		}
		public ArrayList<groupMessage> getMessages()
		{
			return this.messages;
		}
	}
	public int getIdByName(String name)
	{
		for(int i=0;i<this.groupInfoList.size();i++)
		{
			if(this.groupInfoList.get(i).name.equals(name))
				return i;
		}
		return 0;
	}
	public void addNewGroup(String name)
	{
		groupInfo info = new groupInfo(name);
		this.groupInfoList.add(info);
	}
	public ArrayList<groupMessage> getMessages(String groupname)
	{
		int id = this.getIdByName(groupname);
		return this.groupInfoList.get(id).getMessages();
	}
	public void addUserGroupMessage(String username, String groupname,String message)
	{
		int id = this.getIdByName(groupname);
		this.groupInfoList.get(id).addMessage(username, message);
	}
}
