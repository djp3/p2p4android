package edu.uci.ics.luci.p2p4java.p2pservice;

import java.util.concurrent.ExecutionException;

import android.os.AsyncTask;

public class CheckCharSet extends AsyncTask<Void, String, Exception> {

		private AsyncTask<Void,String,Exception> previousTask;
		private P2PService parent;
        
		public CheckCharSet(AsyncTask<Void,String,Exception> previousTask, P2PService parent) {
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
				parent.setStatus(P2PStatus.CHARSET_CHECK_BEGIN);
				
				String c = java.nio.charset.Charset.defaultCharset().name();
				if(!c.equals("UTF-8")){
					return new IllegalArgumentException("The character set is not UTF-8:"+c);
				}
			} catch (Exception e) {
				return e;
			}
			return null;
        }
		
		@Override
        protected void onPostExecute(Exception exception) {
			super.onPostExecute(exception);
			
       		if(exception != null){
       			if(parent.getStatus().equals(P2PStatus.CHARSET_CHECK_BEGIN)){
       				parent.setStatus(P2PStatus.CHARSET_FAILURE);
       			}
       			else{
       				//ignore and keep last status
       			}
        	}
        	else{
       			parent.setStatus(P2PStatus.CHARSET_SUCCESS);
        	}
        }
}
