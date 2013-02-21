package com.home.simplechatapplication;

import java.util.ArrayList;

import com.home.simplechatapplication.R;
import com.home.simplechatapplication.ContactTabActivity.Contact;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class GroupTabActivity extends Activity{
	public static final String TAG = "GroupTabActivity";
	private static groupsAdapter adapter;
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListView groupList = new ListView(this);
        ArrayList<Groups> data = new ArrayList<Groups>();
        adapter = new groupsAdapter(this,R.layout.grouplist_entry, data);
        groupList.setAdapter(adapter);
        setContentView(groupList);
        groupList.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				Log.i(TAG,"Clicked:"+adapter.getItem(arg2).name);
				if(MainActivity.getController().getGroupFSMState()!=FSMSTATES.Sending_Join)
					MainActivity.getController().setTalkGroup(adapter.getItem(arg2).name);
				MainActivity.getController().invokeGroupFSM(FSMEVENTS.Join);
			}});
    }
	public static groupsAdapter getAdapter()
	{
		return adapter;
	}
	public static void addNew(String name)
	{
		Groups cnt = new Groups(name,R.drawable.userstatus_online);
		adapter.add(cnt);
	}
	public static class Groups {
	    public String name;
	    public int image;
	    public Groups(){
	        super();
	    }
	    
	    public Groups(String name, int image) {
	        super();
	        this.name = name;
	    }
	}
	public class groupsAdapter extends ArrayAdapter<Groups>{
	    Context context; 
	    int layoutResourceId;    
	    ArrayList<Groups> data = new ArrayList<Groups>();
	    
	    public groupsAdapter(Context context, int layoutResourceId, ArrayList<Groups> data) {
	        super(context, layoutResourceId, data);
	        this.layoutResourceId = layoutResourceId;
	        this.context = context;
	        this.data = data;
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View row = convertView;
	        GroupsHolder holder = null;
	        
	        if(row == null)
	        {
	            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
	            row = inflater.inflate(layoutResourceId, parent, false);
	            
	            holder = new GroupsHolder();
	            holder.name = (TextView)row.findViewById(R.id.groupName);
	            holder.groupImg = (ImageView)row.findViewById(R.id.groupImg);
	            
	            row.setTag(holder);
	        }
	        else
	        {
	            holder = (GroupsHolder)row.getTag();
	        }
	        
	        Groups group = data.get(position);
	        holder.name.setText(group.name);
	        holder.groupImg.setImageResource(group.image);
	        
	        return row;
	    }
	    
	    public class GroupsHolder
	    {
	        TextView name;
	        ImageView groupImg;
	    }
	}
}
