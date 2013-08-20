package edu.uci.ics.luci.p2p4java.p2pservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public abstract class AbstractService extends Service {
	
	/* Messages sent to client by Abstract Service */
	static public final long HEARTBEAT_FREQUENCY = 5000L;
	static public final int MSG_FROM_HEARTBEAT = 0;
	
	/* Message handled from clients by Abstract Service */
	static public final int MSG_TO_REGISTER_CLIENT = 0; 
	static public final int MSG_TO_UNREGISTER_CLIENT =  MSG_TO_REGISTER_CLIENT +1;

	
    /*****************************************************/
	
	/* Data structures for communicating with clients */

	/* List of clients registered for messages */
    private ArrayList<Messenger> clients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    Timer selfdestruct = null;
    /**
     * Send a message to all registered clients
     * @param msg
     */
    protected void send(Message msg) {
    	
    	List<Messenger> myClients = new ArrayList<Messenger>(clients);
    	
    	Log.i(this.getClass().getCanonicalName(), "Sending message to "+myClients.size()+" clients: "+msg);
    	
    	for(Messenger client:myClients){
    		try {
    			client.send(msg);
    		}
    		catch (RemoteException e) {
    			// The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
    			Log.e(this.getClass().getCanonicalName(), "Client is dead. Removing from list ");
    			clients.remove(client);
            }
        }    	
    	/* If it turns out that we have no clients,
    	 *  then start a self-destruct timer to shut down after 120 seconds
    	*/
    	if(clients.size() == 0){
    		if(selfdestruct == null){
    			Log.i(this.getClass().getCanonicalName(), "Starting self-destruct timer ");
    			selfdestruct = new Timer();
    			selfdestruct.schedule(new TimerTask(){
    				@Override
    				public void run() {
    					if(clients.size() == 0){
    						Log.i(this.getClass().getCanonicalName(), "Self-destructing");
    						stopSelf();
    					}
    					else{
    						Log.i(this.getClass().getCanonicalName(), "Cancel self-destructing");
    						selfdestruct = null;
    					}
    				}}, 120000L);
    		}
    	}
    }
    
    /* Data structures for receiving message from clients */
    private final Messenger mMessenger = new Messenger(new IncomingHandler(this)); // This is what we publish for clients to send messages to our IncomingHandler.
    
    private static class IncomingHandler extends Handler {
    	
    	private AbstractService parent;
    	public IncomingHandler(AbstractService parent){
    		this.parent = parent;
    	}
    	
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_TO_REGISTER_CLIENT:
            	Log.i(this.getClass().getCanonicalName(), "Client registered: "+msg.replyTo);
                parent.clients.add(msg.replyTo);
                break;
            case MSG_TO_UNREGISTER_CLIENT:
            	Log.i(this.getClass().getCanonicalName(), "Client un-registered: "+msg.replyTo);
                parent.clients.remove(msg.replyTo);
                break;            
            default:
            	/* Pass messages on to subclass handlers */
            }
           	parent.onReceiveMessage(msg);
        }
    }
    

	/****************************************************************/
	
	/*
	 *  Lifecyle 
	 *
	 */
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(this.getClass().getCanonicalName(), "Service Created - mark");
        
        startHeartbeat();
        
        /* tell subclass we've started */
        onCreateService();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(this.getClass().getCanonicalName(), "Received start id " + startId + ": " + intent + " - mark");
        return START_STICKY; // run until explicitly stopped.
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(this.getClass().getCanonicalName(), "onBind - mark");
        return mMessenger.getBinder();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(this.getClass().getCanonicalName(), "onDestroy - mark");
        
        /* tell subclass we're dying */
        onDestroyService();
        
        stopHeartbeat();
    }
 
    /****************************************************************/
    
    Timer heartbeat = null;
    /**
     * The heartbeat lets the clients know that the AbstractService is still running.
     * This is taken care of by the Android OS as well.  This heartbeat,
     * because it sends to all registered clients, also is checking to see if it has
     * live registered clients.  If there are no living registered clients, then the clients
     * list will be cleared and the service will shut down.  This prevents against clients
     * crashing without unregistering and leaving us thinking we are still needed.
     */
    private synchronized void startHeartbeat(){
    	if( heartbeat != null){
    		heartbeat.cancel();
    	}
    	heartbeat = new Timer();
    	heartbeat.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
				send(Message.obtain(null, MSG_FROM_HEARTBEAT, System.currentTimeMillis()));
			}
    		
    	}, 0, HEARTBEAT_FREQUENCY);
    }

	private synchronized void stopHeartbeat() {
		heartbeat.cancel();
        heartbeat = null;
	}   
    	
    public abstract void onCreateService();
    public abstract void onDestroyService();
    public abstract void onReceiveMessage(Message msg);

}
