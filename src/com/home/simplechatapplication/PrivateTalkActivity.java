package com.home.simplechatapplication;

import java.util.ArrayList;
import java.util.Arrays;

import com.home.simplechatapplication.R;
import com.home.simplechatapplication.GroupTalkActivity.Message;
import com.home.simplechatapplication.GroupTalkActivity.messageAdapter;
import com.home.simplechatapplication.GroupTalkActivity.messageAdapter.MessageHolder;
import com.home.simplechatapplication.UserInformation.userInfo.privateMessage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PrivateTalkActivity extends Activity{
    final static String TAG = "PrivateTalkActivity";
    private static messageAdapter adapter;
    private ListView talkList; 
    private static String privateName = "";
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	MainActivity.getController().setActivePrivateTalk(true);
    	setContentView(R.layout.privatetalk_layout);
    	talkList = (ListView)findViewById(R.id.privateMessageList);
    	
    	View header = (View)getLayoutInflater().inflate(R.layout.listview_header_layout, null);
        talkList.addHeaderView(header);
        ArrayList<Message> data = new ArrayList<Message>();
    	adapter = new messageAdapter(this,R.layout.talklist_entry, data);
        talkList.setAdapter(adapter);
    	
		Button sendPrivateBtn = (Button)findViewById(R.id.sendPrivateButton);
		final TextView sendPrivateTxt = (EditText)findViewById(R.id.sendPrivateTxt);
		sendPrivateBtn.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				MainActivity.getController().sendPrivateMessage(privateName, sendPrivateTxt.getText().toString());
				addMessage(MainActivity.getController().username,sendPrivateTxt.getText().toString());
				sendPrivateTxt.setText("");
			}});
		Intent intent = this.getIntent();
		privateName = intent.getStringExtra("name");
		TextView txtHeader = (TextView)header.findViewById(R.id.txtHeader);
		txtHeader.setText("Talking with: "+privateName);
		adapter.notifyDataSetChanged();
		
		ArrayList<privateMessage> arr = MainActivity.getController().getUserInfo().getMessages(privateName);
		for(int i=0;i<arr.size();i++)
		{
			addMessage(arr.get(i).getName(),arr.get(i).getMessage());
		}
		adapter.notifyDataSetChanged();
    }
    public static messageAdapter getAdapter()
    {
    	return adapter;
    }
    public static String getCurrentTalker()
    {
    	return privateName;
    }
    public static void addMessage(String username,String message)
    {
    	Message mes = new Message(username+":",message);
    	adapter.add(mes);
    	adapter.notifyDataSetChanged();
    }
    public boolean onKeyDown(int keyCode, KeyEvent event)
	{
    	Log.i(TAG,"Key pressed!");
    	if(keyCode == KeyEvent.KEYCODE_BACK)
    	{
    		MainActivity.getController().setActivePrivateTalk(false);
    	}
		return super.onKeyDown(keyCode, event);
	}
    public static class Message {
	    public String name;
	    public String message;
	    public Message(){
	        super();
	    }
	    
	    public Message(String name, String message) {
	        super();
	        this.name = name;
	        this.message = message;
	    }
	}
	public class messageAdapter extends ArrayAdapter<Message>{
	    Context context; 
	    int layoutResourceId;    
	    ArrayList<Message> data = new ArrayList<Message>();
	    
	    public messageAdapter(Context context, int layoutResourceId, ArrayList<Message> data) {
	        super(context, layoutResourceId, data);
	        this.layoutResourceId = layoutResourceId;
	        this.context = context;
	        this.data = data;
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View row = convertView;
	        MessageHolder holder = null;
	        
	        if(row == null)
	        {
	            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
	            row = inflater.inflate(layoutResourceId, parent, false);
	            
	            holder = new MessageHolder();
	            holder.name = (TextView)row.findViewById(R.id.talkerName);
	            holder.message = (TextView)row.findViewById(R.id.talkerMessage);
	            
	            row.setTag(holder);
	        }
	        else
	        {
	            holder = (MessageHolder)row.getTag();
	        }
	        
	        Message message = data.get(position);
	        holder.name.setText(message.name);
	        holder.message.setText(message.message);
	        
	        return row;
	    }
	    
	    public class MessageHolder
	    {
	        TextView name;
	        TextView message;
	    }
	}
}
