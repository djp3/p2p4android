package edu.uci.ics.luci.p2p4java.p2p4android.lib;

import java.io.IOException;
import java.util.Timer;

import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;
import edu.uci.ics.luci.p2p4java.peergroup.PeerGroup;
import edu.uci.ics.luci.p2p4java.pipe.OutputPipe;
import edu.uci.ics.luci.p2p4java.pipe.OutputPipeEvent;
import edu.uci.ics.luci.p2p4java.pipe.OutputPipeListener;
import edu.uci.ics.luci.p2p4java.pipe.PipeService;
import edu.uci.ics.luci.p2p4java.platform.NetworkManager;
import edu.uci.ics.luci.p2p4java.protocol.PipeAdvertisement;
import edu.uci.ics.luci.p2p4java.util.luci.P2p4java;

public class P2PService extends AbstractService implements OutputPipeListener{
	
	/* sent to clients when the P2P service changes state */
	public static final int MSG_FROM_SET_STATUS_VALUE = MSG_FROM_HEARTBEAT +1;
	private P2PStatus p2pStatus = P2PStatus.OFF;
	
	/* sent to the Service when data should be sent out on the wire */
	public static final int MSG_TO_SEND_DATA = MSG_TO_UNREGISTER_CLIENT + 1;
	
	/* last task in the chain of asynchronous tasks being run */
    private AsyncTask<Void, String, Exception> lastTask = null;
    
    /* P2P data structures */
	private NetworkManager manager = null;
	private PeerGroup netPeerGroup = null;
	private PipeService pipeService = null;
    private PipeAdvertisement pipeAdv = null;
    //private InputPipe inputPipe = null;
    private OutputPipe outputPipe = null;
    
	
    /*****************************************************/
    
    /* Getters and Setters */
    
	public synchronized P2PStatus getStatus(){
		return p2pStatus;
	}
	
	public synchronized String getStatusString() {
		return P2PStatus.translate(p2pStatus);
	}

	public synchronized void setStatus(P2PStatus status) {
		this.p2pStatus = status;
		Log.i(this.getClass().getCanonicalName(),status.translate());
		
		send(Message.obtain(null, MSG_FROM_SET_STATUS_VALUE, status));
	}
	
	
	
	public NetworkManager getManager() {
		return manager;
	}

	public void setManager(NetworkManager manager) {
		this.manager = manager;
	}
	
	
	
	public synchronized void setNetPeerGroup(PeerGroup netPeerGroup) {
		this.netPeerGroup = netPeerGroup;
	}
	
	public synchronized PeerGroup getNetPeerGroup(){
		return this.netPeerGroup;
	}
	


	public synchronized void setPipeService(PipeService pipeService) {
		this.pipeService = pipeService;
	}
	
	public synchronized PipeService getPipeService(){
		return(this.pipeService);
	}
	
	


	public synchronized void setPipeAdv(PipeAdvertisement pipeAdvertisement) {
		this.pipeAdv = pipeAdvertisement;
	}
	
	public synchronized PipeAdvertisement getPipeAdv(){
		return(this.pipeAdv);
	}
	
    /*****************************************************/
	
    Timer selfDestruct;
    
	@Override
	public synchronized void onCreateService() {
        Log.i(this.getClass().getCanonicalName(), "onCreateService - mark");

		setStatus(P2PStatus.OFF);
		
		P2p4java.setContext(this.getApplicationContext());

	    //Toast.makeText(this, "P2P Service Created", Toast.LENGTH_SHORT).show(); 
		
		if(manager == null){
			if(lastTask == null){
				setStatus(P2PStatus.STARTING);
		
				lastTask = new CheckCharSet(lastTask,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
				lastTask= new CheckInternet(lastTask,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,(Void) null);
				lastTask = new MakePipeListener(lastTask,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
				lastTask = new CreateOutputPipe(lastTask,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
			}
		}
	}
		

	@Override
	public synchronized void onDestroyService() {
        Log.i(this.getClass().getCanonicalName(), "onDestroyService - mark");
        
        setStatus(P2PStatus.OFF);
		
	    if(manager != null){
	    	manager.stopNetwork();
	    }
	    
		lastTask = null;
		
	    //Toast.makeText(this, "P2P Service Destroyed", Toast.LENGTH_SHORT).show(); 
	}

	@Override
	public void onReceiveMessage(Message msg) {
		 switch (msg.what) {
         case MSG_TO_REGISTER_CLIENT:
        	 send(Message.obtain(null, MSG_FROM_SET_STATUS_VALUE, this.p2pStatus));
             break;
         case MSG_TO_UNREGISTER_CLIENT:
             break;            
         case MSG_TO_SEND_DATA:
        	 try{
        		 // send the message
        		 boolean result = false;
        		 while (!result){
        			 if(outputPipe != null){
        				 System.out.println("Sending message");
        				 result = outputPipe.send((edu.uci.ics.luci.p2p4java.endpoint.Message) msg.obj);
        				 if(result){
        					 System.out.println("message sent");
        				 }
        				 else{
        					 System.out.println("message not sent... retrying");
        					 try {
        						 Thread.sleep(1000);
        					 } catch (InterruptedException e) {
        					 }
        				 }
        			 }
        			 else{
        				 System.out.println("output pipe not open yet ... retrying");
        				 try {
        					 Thread.sleep(1000);
        				 } catch (InterruptedException e) {
        				 }
        			 }
        		 }
        	 } catch (IOException e) {
        		 System.out.println("failed to send message");
        		 e.printStackTrace();
        	 }
        	 break;            
         default:
         }
	}
	
	
	

	/**
	 * by implementing OutputPipeListener we must define this method which
     * is called when the output pipe is created
     *
     * @param event event object from which to get output pipe object
     */
    public void outputPipeEvent(OutputPipeEvent event) {

        System.out.println("Received the output pipe resolution event");
        // get the output pipe object
        outputPipe = event.getOutputPipe();
    }


}
