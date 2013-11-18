package edu.uci.ics.luci.p2p4java.p2p4android.lib;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;

import android.os.AsyncTask;
import edu.uci.ics.luci.p2p4java.exception.PeerGroupException;
import edu.uci.ics.luci.p2p4java.peergroup.PeerGroup;
import edu.uci.ics.luci.p2p4java.platform.NetworkConfigurator;
import edu.uci.ics.luci.p2p4java.platform.NetworkManager;
import edu.uci.ics.luci.p2p4java.util.luci.P2P4Java;


public class MakePipeListener extends AsyncTask<Void, String, Exception> {

	private AsyncTask<Void,String,Exception> previousTask;
	private P2PService parent;
	
	public MakePipeListener(AsyncTask<Void,String,Exception> previousTask, P2PService parent) {
		super();
		this.previousTask = previousTask;
		this.parent = parent;
	}
	
	@Override
	protected Exception doInBackground(Void... arg0) {
		try{
			if(previousTask != null){
				try {
					while(previousTask.getStatus() != AsyncTask.Status.FINISHED){
						Exception result = previousTask.get();
						if(result != null){
							return result;
						}
					}
				} catch (InterruptedException e) {
					return e;
				} catch (ExecutionException e) {
					return e;
				}
			}
			
			parent.setStatus(P2PStatus.P2P_INITIALIZATION);
			
			NetworkManager localManager = null;
			localManager = parent.getManager();
			if(localManager == null){
			
				File file = P2P4Java.getCacheDirectory();
				File file2 = new File(file, "SinkServer");
				URI uri = file2.toURI();
		
				parent.setStatus(P2PStatus.P2P_INIT_NETWORK_MANAGER);
				localManager = new NetworkManager(NetworkManager.ConfigMode.EDGE, "SinkServer",uri);
				parent.setManager(localManager);
			}
			
			parent.setStatus(P2PStatus.P2P_INIT_NETWORK_CONFIGURATION);
			NetworkConfigurator configurator = null;
			configurator = localManager.getConfigurator();
		
			URI TheSeed = URI.create(Globals.SUPER_URI);
			configurator.addSeedRendezvous(TheSeed);
			configurator.addSeedRelay(TheSeed);
		
			parent.setStatus(P2PStatus.P2P_INIT_NETWORK);
			localManager.startNetwork();
		
			// Get the NetPeerGroup
			parent.setStatus(P2PStatus.P2P_INIT_PEER_GROUP);
			PeerGroup netPeerGroup = localManager.getNetPeerGroup();
			parent.setNetPeerGroup(netPeerGroup);
			
			// get the pipe service, and discovery
			parent.setStatus(P2PStatus.P2P_INIT_PIPE_SERVICE);
			parent.setPipeService(netPeerGroup.getPipeService());
			
			// create the pipe advertisement
			parent.setStatus(P2PStatus.P2P_INIT_PIPE_SERVICE_AD);
			parent.setPipeAdv(SourceServer.getPipeAdvertisement());
			
		} catch (PeerGroupException e) {
			return e;
		} catch (IOException e) {
			return e;
		} catch (Exception e) {
			return e;
		}
		return null;
    }
	
    protected void onPostExecute(Exception exception) {
		super.onPostExecute(exception);

    	if(exception != null){
    		if(parent.getStatus().ordinal() >= P2PStatus.P2P_INITIALIZATION.ordinal()){
    			parent.setStatus(P2PStatus.P2P_FAILURE);
   			}
   			else{
   				//ignore and keep last status
   			}
    	}
    	else{
    		parent.setStatus(P2PStatus.P2P_SUCCESS);
    	}
    }
 }