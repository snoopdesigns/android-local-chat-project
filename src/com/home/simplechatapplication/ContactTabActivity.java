package com.home.simplechatapplication;

import java.util.ArrayList;

import com.home.simplechatapplication.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ContactTabActivity extends Activity{
	public static final String TAG = "ContactTabActivity";
	private static ContactsAdapter adapter;
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListView contactList = new ListView(this);
        ArrayList<Contact> data = new ArrayList<Contact>();
        adapter = new ContactsAdapter(this,R.layout.contactlist_entry, data);
        contactList.setAdapter(adapter);
        setContentView(contactList);
        contactList.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				Log.i(TAG,"Clicked:"+adapter.getItem(arg2).name);
				String privateName = adapter.getItem(arg2).name;
				Intent privatetalkActivity = new Intent(ContactTabActivity.this,PrivateTalkActivity.class);
				privatetalkActivity.putExtra("name", adapter.getItem(arg2).name.toString());
				ContactTabActivity.this.startActivity(privatetalkActivity);
				MainActivity.getController().getUiUpdater().updateUnreadMessages(privateName);
				MainActivity.getController().getUserInfo().resetUnreadMessages(privateName);
				String status = MainActivity.getController().getUserInfo().getUserStatus(privateName);
				MainActivity.getController().updateContactStatus(privateName, status);
			}});
    }
	public static ContactsAdapter getAdapter()
	{
		return adapter;
	}
	public static void addNew(String name)
	{
		Contact cnt = new Contact(name,"");
		adapter.add(cnt);
	}
	public static boolean isUserInList(String username)
	{
		for(int i=0;i<adapter.getCount();i++)
		{
			if(adapter.getItem(i).name.equals(username))
				return true;
		}
		return false;
	}
	public static int getUserIdByName(String username)
	{
		for(int i=0;i<adapter.getCount();i++)
		{
			if(adapter.getItem(i).name.equals(username))
				return i;
		}
		return 0;
	}
	public static void updateStatus(int userId,String status)
	{
		adapter.getItem(userId).status=status;
		adapter.notifyDataSetChanged();
	}
	public static class Contact {
	    public String name;
	    public String status;
	    public Contact(){
	        super();
	    }
	    
	    public Contact(String name, String status) {
	        super();
	        this.name = name;
	        this.status = status;
	    }
	}
	public class ContactsAdapter extends ArrayAdapter<Contact>{
	    Context context; 
	    int layoutResourceId;    
	    ArrayList<Contact> data = new ArrayList<Contact>();
	    
	    public ContactsAdapter(Context context, int layoutResourceId, ArrayList<Contact> data) {
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
	            holder.name = (TextView)row.findViewById(R.id.contactName);
	            holder.status = (TextView)row.findViewById(R.id.contactStatus);
	            
	            row.setTag(holder);
	        }
	        else
	        {
	            holder = (GroupsHolder)row.getTag();
	        }
	        
	        Contact contact = data.get(position);
	        holder.name.setText(contact.name);
	        holder.status.setText(contact.status);
	        
	        return row;
	    }
	    
	    public class GroupsHolder
	    {
	        TextView name;
	        TextView status;
	    }
	}
}
