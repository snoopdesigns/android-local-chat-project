package com.home.simplechatapplication;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.home.simplechatapplication.R;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LocalActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnKeyListener{
	public final static String TAG = "MainActivity";
	public final static int STATE_CONNECTED = 0;
    public final static int STATE_LOST_CONNECTION = 1;
    public final static int STATE_LOST_CONNECTION_CONFIRMED = 2;
	private final int checkConnectionTimerDelay = 5000;
	private final int exitTimerDelay = 2000;
    private int connectionState = STATE_CONNECTED;
	private String myName = "";
	private AlertDialog.Builder changeStatusalert;
	private Timer checkConnectionTimer;
	private Timer exitTimer;
	private boolean exitRequested = false;
	private TabHost tabHost;
	private TextView userNameTxt;
	private TextView userStatusTxt;
	private ImageView userStatusImg;
	private static ChatController controller;
	private static Configuration uiConf;
	public static String privateName = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		tabHost = (TabHost)findViewById(R.id.tabHost);
		LocalActivityManager mlam = new LocalActivityManager(this, false);
		mlam.dispatchCreate(savedInstanceState);
		tabHost.setup(mlam);
		
        TabSpec contactsspec = tabHost.newTabSpec("Contacts");
        contactsspec.setIndicator("Contacts", getResources().getDrawable(R.drawable.i_listening_simple));
        Intent photosIntent = new Intent(this, ContactTabActivity.class);
        contactsspec.setContent(photosIntent);
 
        TabSpec groupsspec = tabHost.newTabSpec("Groups");
        groupsspec.setIndicator("Groups", getResources().getDrawable(R.drawable.i_talking));
        Intent songsIntent = new Intent(this, GroupTabActivity.class);
        groupsspec.setContent(songsIntent);
        
        tabHost.addTab(contactsspec);
        tabHost.addTab(groupsspec);
        tabHost.setCurrentTab(1);
        tabHost.setCurrentTab(0);
        this.setTabColor();
        tabHost.setOnTabChangedListener(new OnTabChangeListener(){
			public void onTabChanged(String arg0) {
				Log.i(TAG,"Tab selected: "+arg0);
				setTabColor();
			}});
        
		initConfiguration("/configuration.xml");
		myName = uiConf.getValueFromSection("UIConfiguration", "username", "defaultuser");
		Log.i(TAG,"username = "+myName);
		controller = new ChatController(myName,this);
		
		this.userNameTxt = (TextView)findViewById(R.id.userName);
		this.userStatusTxt = (TextView)findViewById(R.id.userStatus);
		this.userStatusImg = (ImageView)findViewById(R.id.userStatusImg);
		this.updateUserName(controller.username);
		
		changeStatusalert = new AlertDialog.Builder(this);
		this.userStatusImg.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				
				changeStatusalert.setTitle("Update userstatus");
				changeStatusalert.setMessage("type you status here");
				final EditText input = new EditText(getApplicationContext());
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				        LinearLayout.LayoutParams.FILL_PARENT,
				        LinearLayout.LayoutParams.FILL_PARENT);
				input.setLayoutParams(lp);
				changeStatusalert.setView(input);

				changeStatusalert.setPositiveButton("Set status", new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int whichButton) {
				    	userStatusTxt.setText(input.getText().toString());
				    	controller.sendStatusUpdateMessage(input.getText().toString());
				    }
				});
				changeStatusalert.show();
			}});
		checkConnectionTimer = new Timer();
        checkConnectionTimer.schedule(new TimerTask() 
        {
    	    public void run() {
    	    	checkConnectionTimerMethod();
    	    }
    	},this.checkConnectionTimerDelay,this.checkConnectionTimerDelay);
        exitTimer = new Timer();
	}
	 protected void onDestroy() {
		Log.i(TAG, "onDestroy");
		if(controller.isRegistered())
			controller.Deregister();
	    uiConf.write(uiConf.getFileName());
	    controller.Destroy();
	    super.onDestroy();
	}
	public void OpenGroupTalkActivity()
	{
		Intent grouptalkActivity = new Intent(MainActivity.this,GroupTalkActivity.class);
		grouptalkActivity.putExtra("name", controller.getTalkGroup());
		MainActivity.this.startActivity(grouptalkActivity);
	}
	public void setTabColor() {
	    for(int i=0;i<tabHost.getTabWidget().getChildCount();i++)
	    {
	    	final View view = tabHost.getTabWidget().getChildTabViewAt(i);
	        tabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#33B5E5")); //unselected
	        final TextView textView = (TextView)view.findViewById(android.R.id.title);
	        textView.setTextColor(Color.parseColor("#ffffff"));
	    }
	    
	    tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).setBackgroundColor(Color.parseColor("#0099CC")); // selected
	}
	private void exitTimerMethod()
	{
	    this.runOnUiThread(checkExit);
	}
	private Runnable checkExit = new Runnable() {
		public void run() {
		    exitRequested = false;
		}
	};
	private void checkConnectionTimerMethod()
	{
	    this.runOnUiThread(checkConnection);
	}
	private Runnable checkConnection = new Runnable() {
		public void run() {
			Log.v(TAG,"Check connection timer called");
			ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
	    	NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    	if (mWifi.isConnected() == false) 
	    	{
	    		if(connectionState == MainActivity.STATE_CONNECTED)
	    		{
	    			connectionState = MainActivity.STATE_LOST_CONNECTION;
	    		}
	    		else if(connectionState == MainActivity.STATE_LOST_CONNECTION)
	    		{
	    			connectionState = MainActivity.STATE_LOST_CONNECTION_CONFIRMED;
	    			makeToastNotification("Wifi connection is missing");
	    			//MainActivity.getService().GetController().ReinitController();
	    		}  
	    	}
	    	else if(mWifi.isConnected())
	    	{
	    		if(connectionState == MainActivity.STATE_LOST_CONNECTION_CONFIRMED)
	    		{
	    			connectionState = MainActivity.STATE_CONNECTED;
	    		}
	    	}
		}
	};
	public void Reinit()
	{
		myName = uiConf.getValueFromSection("UIConfiguration", "username", "defaultuser");
		controller.Destroy();
		controller = new ChatController(myName,this);
		this.updateUserStatus("offline");
		this.updateUserName(myName);
	}
	public void initConfiguration(String m_filename)
	{
		String fullname = Environment.getExternalStorageDirectory().getPath() + m_filename;
    	uiConf = new Configuration(fullname);
	}
	public static Configuration getConfiguration()
	{
		return uiConf;
	}
	public static ChatController getController()
	{
		return controller;
	}
	public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.layout.menu, menu);
        return true;
    }
	public boolean onOptionsItemSelected(MenuItem item)
    {
 
        switch (item.getItemId())
        {
        case R.id.menu_register:
            controller.invokeInitFSM(FSMEVENTS.Register);
            return true;
 
        case R.id.menu_deregister:
            controller.invokeInitFSM(FSMEVENTS.Deregister);
            return true;
        case R.id.menu_settings:
        	Intent settingsActivity = new Intent(getBaseContext(),PreferencesActivity.class);
        	startActivity(settingsActivity);
            return true;
        case R.id.menu_exit:
        	this.onDestroy();
        	this.finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	public void makeToastNotification(final String message)
	{
		runOnUiThread(new Runnable() {
		    public void run() {
		    	Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
		    }
		});
	}
	public void updatePrivateMessage(final String username,final String message)
	{
		runOnUiThread(new Runnable() {
		    public void run() {
		    	PrivateTalkActivity.addMessage(username, message);
		    }
		});
	}
	public void updateUnreadMessages(final String username)
	{
		Log.i(TAG,"updateUnreadMessages() for user = "+username);
		runOnUiThread(new Runnable() {
		    public void run() {
		    	int num = controller.getUserInfo().getUnreadMessages(username);
		    	int id = ContactTabActivity.getUserIdByName(username);
		    	if(num == 0)
		    	{
		    		String status = controller.getUserInfo().getUserStatus(username);
		    		ContactTabActivity.getAdapter().getItem(id).status = status;
		    	}
		    	else
		    	{
		    		int unreadMessages = controller.getUserInfo().getUnreadMessages(username);
		    		ContactTabActivity.getAdapter().getItem(id).status = unreadMessages+" unread messages";;
		    	}
		    	ContactTabActivity.getAdapter().notifyDataSetChanged();
		    }
		});
	}
	public void updateContactStatus(final int userId,final String status)
	{
		runOnUiThread(new Runnable() {
		    public void run() {
		    	ContactTabActivity.updateStatus(userId, status);
		    }
		});
	}
	public void updateGroupMessage(final String username,final String message)
	{
		runOnUiThread(new Runnable() {
		    public void run() {
		    	GroupTalkActivity.addMessage(username, message);
		    }
		});
	}
	public void updateUserStatus(final String status)
	{
		runOnUiThread(new Runnable() {
		    public void run() {
		    	userStatusTxt.setText(status);
		    	if(status.equals("online"))
		    	{
		    		userStatusImg.setImageResource(R.drawable.i_online);
		    	}
		    	else
		    	{
		    		userStatusImg.setImageResource(R.drawable.i_offline);
		    	}
		    }
		});
	}
	public void updateUserName(final String name)
	{
		runOnUiThread(new Runnable() {
		    public void run() {
		    	userNameTxt.setText(name);
		    }
		});
	}
	public void clearContactsAndGroups()
	{
		runOnUiThread(new Runnable() {
		    public void run() {
		    	ContactTabActivity.getAdapter().clear();
		    	GroupTabActivity.getAdapter().clear();
		    	ContactTabActivity.getAdapter().notifyDataSetChanged();
		    	GroupTabActivity.getAdapter().notifyDataSetChanged();
		    }
		});
	}
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
	    int action = event.getAction();
	    int keyCode = event.getKeyCode();
		Log.i(TAG, "Key pressed");
		if (action == KeyEvent.ACTION_DOWN) 
	    {
	    	if (keyCode == KeyEvent.KEYCODE_BACK)
			{
				if(exitRequested == false)
				{
		            Log.v(TAG,"back key pressed");
		            Toast.makeText(getApplicationContext(), "Press back key again to exit", Toast.LENGTH_SHORT).show();
		            exitRequested = true;
		            exitTimer.schedule(new TimerTask() 
		            {
			        	    @Override
			        	    public void run() {
			        	    	exitTimerMethod();
		        	}
		        	},this.exitTimerDelay);
		            return true;
				}
				else
				{
					Log.v(TAG,"back key pressed 2 time");
					this.onDestroy();
					System.runFinalizersOnExit(true);
					System.exit(0);
				}
			}
	    }
		return false;
	}
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
}
