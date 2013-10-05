package com.home.simplechatapplication;

import java.util.ArrayList;
import java.util.Arrays;

import com.home.simplechatapplication.GroupInformation.groupInfo.groupMessage;
import com.home.simplechatapplication.R;
import com.home.simplechatapplication.GroupTabActivity.Groups;
import com.home.simplechatapplication.GroupTabActivity.groupsAdapter;
import com.home.simplechatapplication.GroupTabActivity.groupsAdapter.GroupsHolder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class GroupTalkActivity extends Activity implements OnKeyListener{
    final static String TAG = "GroupTalkActivity";
    private static messageAdapter adapter;
    private ListView talkList; 
    private String groupName = "";
    private boolean isActiveTalk = false;
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	setContentView(R.layout.grouptalk_layout);
    	talkList = (ListView)findViewById(R.id.groupMessageList);
    	View header = (View)getLayoutInflater().inflate(R.layout.listview_header_layout, null);
        talkList.addHeaderView(header);
        ArrayList<Message> data = new ArrayList<Message>();
    	adapter = new messageAdapter(this,R.layout.talklist_entry, data);
        talkList.setAdapter(adapter);
    	
		final Button sendGroupBtn = (Button)findViewById(R.id.sendGroupButton);
		final TextView sendGroupTxt = (EditText)findViewById(R.id.sendGroupTxt);
		sendGroupBtn.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				MainActivity.getController().sendGroupMessage(sendGroupTxt.getText().toString());
				sendGroupTxt.setText("");
			}});
		Intent intent = this.getIntent();
		groupName = intent.getStringExtra("name");
		TextView txtHeader = (TextView)header.findViewById(R.id.txtHeader);
		txtHeader.setText("Talking in: "+groupName);
		ArrayList<groupMessage> arr = MainActivity.getController().getGroupInfo().getMessages(groupName);
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
    public static void addMessage(String username,String message)
    {
    	Message mes = new Message(username+":",message);
    	adapter.add(mes);
    	adapter.notifyDataSetChanged();
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
	@Override
	public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean dispatchKeyEvent(KeyEvent event) {
	    int action = event.getAction();
	    int keyCode = event.getKeyCode();
	    if (action == KeyEvent.ACTION_DOWN) 
	    {
	    	if(keyCode == KeyEvent.KEYCODE_BACK)
	    	{
	    		Log.i(TAG,"Back button pressed");
	    		MainActivity.getController().invokeGroupFSM(FSMEVENTS.Bye);
	    		this.finish();
	    	}
	    }            
	    return true;
	}
}
