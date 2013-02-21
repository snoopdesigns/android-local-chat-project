package com.home.simplechatapplication;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.codec.binary.Base64;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

public class audioManager {
	public static final String TAG = "AudioManager";
	private AudioRecord recorder = null;
	private Thread t;
    private AudioTrack track = null;
    private ChatController controller;
    private boolean stopped = false;
    private UpdateListener listener;
    int ix = 0;
    int N = AudioRecord.getMinBufferSize(8000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
    public byte[][]   buffers  = new byte[15][N];
    private int bufferNum = 0;
    private String groupname;
    audioManager(ChatController controller) 
	{
    	this.controller = controller;
		Log.i(TAG,"AudioManager init");
		Log.i(TAG,"Buffer size = "+N);
		recorder = new AudioRecord(AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, N);
		track = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, 
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, N, AudioTrack.MODE_STREAM);
	}
    public void setGroupName(String groupname)
    {
    	this.groupname = groupname;
    }
    public byte[] getBuffer(int num)
    {
    	return this.buffers[num];
    }
    public void putPacket(String data)
    {
    	listener.putPacket(data);
    }
    public boolean buffersEqual(byte[] b1, byte[] b2)
    {
    	for(int i=0;i<b1.length;i++)
    	{
    		if(b1[i]!=b2[i])
    			return false;
    	}
    	return true;
    }
	public void startRecord()
	{
		Log.i(TAG,"startRecord enter");
		stopped = false;
		bufferNum = 0;
		Runnable r = new Runnable() {
			public void run() {
				
				recorder.startRecording();
		        while(!stopped)
		        {
		            N = recorder.read(buffers[bufferNum],0,buffers[bufferNum].length);
		            Log.i(TAG,"Recorded:" +buffers[bufferNum] + "in "+bufferNum);
		            byte[] base64String = Base64.encodeBase64(buffers[bufferNum]);
		            String str = new String(base64String);
		            Log.i(TAG,"Sound message length: "+str.length());
		            byte[] bytes = Base64.decodeBase64(str.getBytes());
		            controller.sendSoundMessage(groupname, str);
		            if(bufferNum>15)
		            	bufferNum=0;
		            else
		            	bufferNum++;
		        }
			}};
		t = new Thread(r);
		t.start();
	}
	public void stopRecord()
	{
		this.stopped = true;
		recorder.stop();
	}
	public void startPlay()
	{
		Log.i(TAG,"Starting player, bufferNum = "+bufferNum);
		listener = new UpdateListener(N,this,track);
		track.setPlaybackPositionUpdateListener(listener);
		track.setNotificationMarkerPosition(N/2);
		track.play();
	}
	public void stopPlay()
	{
		track.stop();
	}
	public class UpdateListener implements OnPlaybackPositionUpdateListener {
		private int buffersize;
		audioManager manager;
		private AudioTrack track;
		private int i = 1;
		private Queue<String> bufferQueue=new LinkedList<String>();
		private boolean isWaitingForPacket = true;
		UpdateListener(int buffersize,audioManager manager,AudioTrack track)
		{
			this.buffersize = buffersize;
			this.manager = manager;
			this.track = track;
		}
		void putPacket(String data)
		{
			if(this.isWaitingForPacket)
			{
				byte[] bytes = Base64.decodeBase64(data.getBytes());
				Log.i(TAG,"New buffer put to play :"+bytes + " " + bytes.length);
				track.write(bytes, 0, bytes.length);
				track.setNotificationMarkerPosition(buffersize/2);
				track.play();
				this.isWaitingForPacket = false;
			}
			else
			{
				Log.i(TAG,"New buffer put to queue");
				this.bufferQueue.add(data);
			}
				
		}
		@Override
		public void onMarkerReached(AudioTrack arg0) {
			// TODO Auto-generated method stub
			Log.i(TAG,"Marker reached");
			if(this.bufferQueue.isEmpty()) {
				arg0.stop();
				Log.i(TAG,"Wating for packet");
				this.isWaitingForPacket = true;
				return;
			}
			else
			{
				Log.i(TAG,"Write buffer "+i);
				arg0.play();
				String data = this.bufferQueue.poll();
				byte[] bytes = Base64.decodeBase64(data.getBytes());
				arg0.write(bytes,0,bytes.length);
				arg0.setNotificationMarkerPosition(buffersize/2);
			}
		}

		@Override
		public void onPeriodicNotification(AudioTrack arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
