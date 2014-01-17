package edu.uci.ics.luci.p2p4java.p2p4android.lib;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import android.os.AsyncTask;


public class CreateOutputPipe extends AsyncTask<Void, String, Exception> {

	private AsyncTask<Void,String,Exception> previousTask;
	private P2PService parent;
	
	public CreateOutputPipe(AsyncTask<Void,String,Exception> previousTask, P2PService parent) {
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
			// create the output pipe
			parent.setStatus(P2PStatus.P2P_INIT_OUTPUT_PIPE);
			parent.getPipeService().createOutputPipe(parent.getPipeAdv(),parent);
			
		
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