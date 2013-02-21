package com.home.simplechatapplication;

import java.util.Hashtable;
import java.util.Map;

import android.util.Log;

public class FiniteStateMachine {

	final static String TAG = "FSM";
	private String instanceName;

	public FiniteStateMachine (String instanceName) {
		this.instanceName = instanceName;
	}
	
	public void addTransitionState(int currentState, int nextState, FiniteStateMachineListener listener, 
			int event) {
		Map<Integer, FSMState> eventMap = this.FSMTable.get(currentState);
		if (eventMap == null) {
			eventMap = new Hashtable<Integer, FSMState>();
			this.FSMTable.put(currentState, eventMap);
		}
		eventMap.put(event, new FSMState(nextState, listener));
	}
	
	public void invokeFSM (int event) {
		Map<Integer, FSMState> eventMap = this.FSMTable.get(this.currentState);
		if (eventMap != null) {
			FSMState objState = eventMap.get(event);
			if (objState != null) {
				Log.v (TAG, this.instanceName + " from state " + this.currentState +
						" to state " + objState.nextState + " by " + event + " event");
				if (objState.listener != null) {
					objState.listener.eventMethod(event);
				}
				this.currentState = objState.nextState;
			}
			else {
				Log.e(TAG, "Unexpected event = " + event + " for state = " + this.currentState +
						" in " + this.instanceName);
			}
		}
		else {
			Log.e(TAG, "Missed map for current state = " + this.currentState +
					" in " + this.instanceName);
		}
	}
	
	public void setNullState () {
		this.currentState = FSMSTATES.Null_state;
	}
	
	public int getCurrentState () {
		return this.currentState;
	}

	private class FSMState {
		public int nextState;
		public FiniteStateMachineListener listener;
		
		public FSMState (int state, FiniteStateMachineListener listener) {
			this.nextState = state;
			this.listener = listener;
		}
	}

	private int currentState = FSMSTATES.Null_state;
	private Map<Integer, Map<Integer, FSMState>> FSMTable = new Hashtable<Integer, Map<Integer, FSMState>>();
}

