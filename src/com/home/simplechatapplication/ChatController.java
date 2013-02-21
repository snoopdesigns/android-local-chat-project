package com.home.simplechatapplication;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.spec.SecretKeySpec;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class ChatController {
	public static final String TAG = "ChatController";
	private static boolean isActivePrivateTalk = false;
	private	ChatClient client;
	private MessageParser parser = new MessageParser();
	private UserInformation userInfo = new UserInformation();
	private GroupInformation groupInfo = new GroupInformation();
	private audioManager aManager;
	private Thread t;
	private MainActivity uiUpdate;	
	private FiniteStateMachine Initiate_Fsm;
	private FiniteStateMachine Group_Fsm;
	private String talkgroupName="";
	private Timer registerTimer;
	private Timer deregisterTimer;
	private Timer joinTimer;
	private Timer leftTimer;
	public String username;
	
	ChatController(String username,MainActivity ui)
	{
		Log.i(TAG,"ChatController init");
		this.username = username;
		this.uiUpdate = ui;
		String hostname = MainActivity.getConfiguration().getHostname();
        Log.i(TAG,"Hostname = "+hostname);
		int port = 54321;
        client = new ChatClient(hostname, port,this);
        aManager = new audioManager(this);
        InitStateMachines ();
        registerTimer = new Timer();
        deregisterTimer = new Timer();
        joinTimer = new Timer();
        leftTimer = new Timer();
	}
	private void InitStateMachines() 
	{
        this.Initiate_Fsm = new FiniteStateMachine("Initiate_Fsm");
        this.Group_Fsm = new FiniteStateMachine("Group_Fsm");
		
        this.Initiate_Fsm.addTransitionState(FSMSTATES.Null_state, FSMSTATES.Sending_Register, new FiniteStateMachineListener() {
			public void eventMethod(int event) { Register(); }}, FSMEVENTS.Register);
        this.Initiate_Fsm.addTransitionState(FSMSTATES.Registered, FSMSTATES.Sending_Deregister, new FiniteStateMachineListener() {
			public void eventMethod(int event) { Deregister(); }}, FSMEVENTS.Deregister);
        this.Initiate_Fsm.addTransitionState(FSMSTATES.Sending_Register, FSMSTATES.Registered, new FiniteStateMachineListener() {
			public void eventMethod(int event) { DoNothing(); }}, FSMEVENTS.Register_success);
        this.Initiate_Fsm.addTransitionState(FSMSTATES.Sending_Register, FSMSTATES.Null_state, new FiniteStateMachineListener() {
			public void eventMethod(int event) { RegisterFailed(); }}, FSMEVENTS.Register_Failed);
        this.Initiate_Fsm.addTransitionState(FSMSTATES.Sending_Deregister, FSMSTATES.Null_state, new FiniteStateMachineListener() {
			public void eventMethod(int event) { DoNothing(); }}, FSMEVENTS.Deregister_Success);
        this.Initiate_Fsm.addTransitionState(FSMSTATES.Sending_Deregister, FSMSTATES.Registered, new FiniteStateMachineListener() {
			public void eventMethod(int event) { DeregisterFailed(); }}, FSMEVENTS.Deregister_Failed);
        
        this.Group_Fsm.addTransitionState(FSMSTATES.Null_state, FSMSTATES.NotInGroup, new FiniteStateMachineListener() {
			public void eventMethod(int event) { DoNothing(); }}, FSMEVENTS.Init);
        this.Group_Fsm.addTransitionState(FSMSTATES.NotInGroup, FSMSTATES.Sending_Join, new FiniteStateMachineListener() {
			public void eventMethod(int event) { JoinGroup(); }}, FSMEVENTS.Join);
        this.Group_Fsm.addTransitionState(FSMSTATES.Sending_Join, FSMSTATES.InTalkGroup, new FiniteStateMachineListener() {
			public void eventMethod(int event) { DoNothing(); }}, FSMEVENTS.Join_Success);
        this.Group_Fsm.addTransitionState(FSMSTATES.Sending_Join, FSMSTATES.NotInGroup, new FiniteStateMachineListener() {
			public void eventMethod(int event) { JoinFailed(); }}, FSMEVENTS.Join_Failed);
        this.Group_Fsm.addTransitionState(FSMSTATES.InTalkGroup, FSMSTATES.Sending_Bye, new FiniteStateMachineListener() {
			public void eventMethod(int event) { LeftGroup(); }}, FSMEVENTS.Bye);
        this.Group_Fsm.addTransitionState(FSMSTATES.Sending_Bye, FSMSTATES.NotInGroup, new FiniteStateMachineListener() {
			public void eventMethod(int event) { DoNothing(); }}, FSMEVENTS.Bye_Success);
        this.Group_Fsm.addTransitionState(FSMSTATES.Sending_Bye, FSMSTATES.InTalkGroup, new FiniteStateMachineListener() {
			public void eventMethod(int event) { LeftFailed(); }}, FSMEVENTS.Bye_Failed);
        this.Group_Fsm.invokeFSM(FSMEVENTS.Init);
    }
	public void DoNothing()
	{
		
	}
	public void invokeInitFSM(int event)
	{
		this.Initiate_Fsm.invokeFSM(event);
	}
	public void invokeGroupFSM(int event)
	{
		this.Group_Fsm.invokeFSM(event);
	}
	public int getGroupFSMState()
	{
		return this.Group_Fsm.getCurrentState();
	}
	
	public boolean isRegistered()
	{
		if(this.Initiate_Fsm.getCurrentState()==FSMSTATES.Registered)
			return true;
		else
			return false;
	}
	public void setTalkGroup(String groupname)
	{
		this.talkgroupName = groupname;
	}
	public String getTalkGroup()
	{
		return this.talkgroupName;
	}
	public void setUsername(String username)
	{
		this.username = username;
	}
	public boolean isActivePrivateTalk()
	{
		return isActivePrivateTalk;
	}
	public void setActivePrivateTalk(boolean status)
	{
		isActivePrivateTalk = status;
	}
	public void handleMessage(String message)
	{
		tMessage mes = parser.parseMessage(message);
		if(mes.action.equals("ack"))
		{
			if(mes.data.equals("register"))
			{
				this.RegisterSuccess();
			}
			if(mes.data.equals("deregister"))
			{
				this.DeregisterSuccess();
			}
			if(mes.data.equals("join"))
			{
				this.JoinGroupSuccess();
			}
			if(mes.data.equals("left"))
			{
				this.LeftGroupSuccess();
			}
		}
		if(mes.action.equals("presence"))
		{
			this.updateUserPresence(mes.data);
		}
		if(mes.action.equals("groups"))
		{
			this.updateGroupList(mes.data);
		}
		if(mes.action.contains("groupmessage"))
		{
			this.processGroupMessage(mes.action.split("_")[1],mes.data);
		}
		if(mes.action.contains("privatemessage"))
		{
			this.processPrivateMessage(mes.action.split("_")[1], mes.data);
		}
		if(mes.action.equals("checkpresence"))
		{
			this.SendPresenceMessage();
		}
		if(mes.action.contains("sound"))
		{
			//uiUpdate.makeToastNotification("New sound message");
			aManager.putPacket(mes.data);
		}
		if(mes.action.contains("updatestatus"))
		{
			this.updateContactStatus(mes.action.split("_")[1],mes.data);
		}
	}
	
	public MainActivity getUiUpdater()
	{
		return this.uiUpdate;
	}
	
	public UserInformation getUserInfo()
	{
		return this.userInfo;
	}
	
	public GroupInformation getGroupInfo()
	{
		return this.groupInfo;
	}
	
	public audioManager getAudioManager()
	{
		return this.aManager;
	}
	
	public void updateContactStatus(String username,String status)
	{
		this.userInfo.setUserStatus(username, status);
		for(int i=0;i<ContactTabActivity.getAdapter().getCount();i++)
		{
			if(ContactTabActivity.getAdapter().getItem(i).name.equals(username))
			{
				uiUpdate.updateContactStatus(i, status);
			}
		}
	}
	public void updateUserPresence(final String data)
	{
		Handler refresh = new Handler(Looper.getMainLooper());
		refresh.post(new Runnable() {
		    public void run()
		    {
		    	String[] users = data.split(",");
				if(data.equals("")) return;
				Log.i(TAG,"Updating presence:"+users);
				for(int i=0;i<users.length;i++)
				{
					Log.i(TAG,"Adding user:"+users[i]);
					if(!ContactTabActivity.isUserInList(users[i]))
					{
						ContactTabActivity.addNew(users[i]);
						userInfo.addNewUser(users[i]);
					}
				}
				ContactTabActivity.getAdapter().notifyDataSetChanged();
		    }
		});	
	}
	public void updateGroupList(final String data)
	{
		Log.i(TAG,"Updating groups: "+data);
		Handler refresh = new Handler(Looper.getMainLooper());
		refresh.post(new Runnable() {
		    public void run()
		    {
		    	String[] groups = data.split(",");
				GroupTabActivity.getAdapter().clear();
				Log.i(TAG,"Updating groups:"+groups);
				for(int i=0;i<groups.length;i++)
				{
					Log.i(TAG,"Adding group:"+groups[i]);
					GroupTabActivity.addNew(groups[i]);
					groupInfo.addNewGroup(groups[i]);
				}
				GroupTabActivity.getAdapter().notifyDataSetChanged();
		    }
		});	
	}
	public void SendMessage(String message)
	{
		tMessage tMes = new tMessage(username,"",message);
    	String mes = parser.createMessage(tMes);
        mes.getBytes();
        client.sendSomeMessages(mes);
	}
	public void sendSoundMessage(String groupname,String data)
	{
		tMessage tMes = new tMessage(this.username,"sound",data);
    	String mes = parser.createMessage(tMes);
        mes.getBytes();
        client.sendSomeMessages(mes);
	}
	public void sendStatusUpdateMessage(String status)
	{
		tMessage tMes = new tMessage(username,"updatestatus",status);
    	String mes = parser.createMessage(tMes);
        mes.getBytes();
        client.sendSomeMessages(mes);
	}
	public void SendPresenceMessage()
	{
		tMessage tMes = new tMessage(username,"checkpresence","");
    	String mes = parser.createMessage(tMes);
        mes.getBytes();
        client.sendSomeMessages(mes);
	}
	public void sendPrivateMessage(String username,String message)
	{
		Log.i(TAG,"Added to messages:"+this.username+"_"+message);
		this.userInfo.addUserMessage(username, message,this.username);
		tMessage tMes = new tMessage(this.username,"privatemessage_"+username,message);
    	String mes = parser.createMessage(tMes);
        mes.getBytes();
        client.sendSomeMessages(mes);
	}
	public void sendGroupMessage(String message)
	{
		tMessage tMes = new tMessage(username,"groupmessage",message);
    	String mes = parser.createMessage(tMes);
        mes.getBytes();
        client.sendSomeMessages(mes);
	}
	public void processGroupMessage(String username,String message)
	{
		uiUpdate.updateGroupMessage(username,message);
		groupInfo.addUserGroupMessage(username, this.getTalkGroup(), message);
	}
	public void processPrivateMessage(String username,String message)
	{
		if(isActivePrivateTalk && PrivateTalkActivity.getCurrentTalker().equals(username))
		{
			if(PrivateTalkActivity.getCurrentTalker().equals(username))
			{
				uiUpdate.updatePrivateMessage(username,message);
				Log.i(TAG,"(PROC)Added to messages:"+username+"_"+message);
				this.userInfo.addUserMessage(username, message,username);
			}
			else
			{
				uiUpdate.makeToastNotification("New private message from "+username + ": "+message);
				this.userInfo.addUserMessage(username, message,username);
				this.userInfo.updateUnreadMessages(username);
				this.uiUpdate.updateUnreadMessages(username);
			}
		}
		else
		{
			uiUpdate.makeToastNotification("New private message from "+username + ": "+message);
			Log.i(TAG,"(PROC)Added to messages:"+username+"_"+message);
			this.userInfo.addUserMessage(username, message,username);
			this.userInfo.updateUnreadMessages(username);
			this.uiUpdate.updateUnreadMessages(username);
		}
	}
	public void RegisterSuccess()
	{
		this.registerTimer.cancel();
		uiUpdate.makeToastNotification("Register success!");
		uiUpdate.updateUserStatus("online");
		this.Initiate_Fsm.invokeFSM(FSMEVENTS.Register_success);
	}
	public void RegisterFailed()
	{
		uiUpdate.makeToastNotification("Register failed!");
	}
	public void DeregisterSuccess()
	{
		Log.i(TAG,"deregistering success");
		this.deregisterTimer.cancel();
		t.interrupt();
		client.isThreadStopped = true;
		client.Destroy();
		uiUpdate.makeToastNotification("Deregister success!");
		uiUpdate.updateUserStatus("offline");
		uiUpdate.clearContactsAndGroups();
		this.Initiate_Fsm.invokeFSM(FSMEVENTS.Deregister_Success);
	}
	public void DeregisterFailed()
	{
		uiUpdate.makeToastNotification("Deregister failed!");
	}
	public void JoinGroupSuccess()
	{
		this.joinTimer.cancel();
		uiUpdate.makeToastNotification("Join group success!");
		this.Group_Fsm.invokeFSM(FSMEVENTS.Join_Success);
		uiUpdate.OpenGroupTalkActivity();
	}
	public void LeftGroupSuccess()
	{
		this.leftTimer.cancel();
		this.Group_Fsm.invokeFSM(FSMEVENTS.Bye_Success);
		uiUpdate.makeToastNotification("Left group success!");
	}
	public void JoinFailed()
	{
		this.Group_Fsm.invokeFSM(FSMEVENTS.Join_Failed);
		uiUpdate.makeToastNotification("Join group failed!");
	}
	public void LeftFailed()
	{
		this.Group_Fsm.invokeFSM(FSMEVENTS.Bye_Failed);
		uiUpdate.makeToastNotification("Left group failed!");
	}
	public void Register()
	{
		uiUpdate.makeToastNotification("Registering..");
		if(client.initiateSocket(this.username)==1)
		{
			tMessage tMes = new tMessage(username,"register","data");
        	String mes = parser.createMessage(tMes);
        	client.sendSomeMessages(mes);
			Runnable r = new Runnable(){
				public void run() {
					client.readSomeMessages();
			}};
			t = new Thread(r);
			t.start();
			registerTimer = new Timer();
			registerTimer.schedule(new TimerTask() 
	        {
	    	    public void run() {
	    	    	Initiate_Fsm.invokeFSM(FSMEVENTS.Register_Failed);
	    	    }
	    	},5000);
		}
	}
	public void Deregister()
	{
		uiUpdate.makeToastNotification("Deregistering..");
		Log.i(TAG,"deregistering begin");
		client.destroyConnection(this.username);
		deregisterTimer = new Timer();
		deregisterTimer.schedule(new TimerTask() 
        {
    	    public void run() {
    	    	Initiate_Fsm.invokeFSM(FSMEVENTS.Deregister_Failed);
    	    }
    	},5000);
	}
	public void JoinGroup()
	{
		String groupname = this.talkgroupName;
		Log.i(TAG,"joining group:"+groupname);
		tMessage tMes = new tMessage(username,"join",groupname);
    	String mes = parser.createMessage(tMes);
        client.sendSomeMessages(mes);
        joinTimer = new Timer();
		joinTimer.schedule(new TimerTask() 
        {
    	    public void run() {
    	    	Group_Fsm.invokeFSM(FSMEVENTS.Join_Failed);
    	    }
    	},5000);
	}
	public void LeftGroup()
	{
		Log.i(TAG,"lefting group.");
		tMessage tMes = new tMessage(username,"left","");
    	String mes = parser.createMessage(tMes);
        mes.getBytes();
        client.sendSomeMessages(mes);
        leftTimer = new Timer();
		leftTimer.schedule(new TimerTask() 
        {
    	    public void run() {
    	    	Group_Fsm.invokeFSM(FSMEVENTS.Bye_Failed);
    	    }
    	},5000);
	}
	public void Destroy()
	{
		client.Destroy();
	}
}
