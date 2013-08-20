package edu.uci.ics.luci.p2p4java.p2p4android.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import android.os.AsyncTask;

public class CheckInternet extends AsyncTask<Void, String, Exception> {

		private AsyncTask<Void,String,Exception> previousTask;
		private P2PService parent;
		
        
		public CheckInternet(AsyncTask<Void,String,Exception> previousTask, P2PService parent) {
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
				
				parent.setStatus(P2PStatus.INTERNET_CHECK_BEGIN);
			
				URL url = null;
				try {
					url = new URL("http://www.google.com");
				} catch (MalformedURLException e) {
					return e;
				}
      		
				BufferedReader reader = null;
				StringBuilder builder = new StringBuilder();
				try {
					reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
					for (String line; (line = reader.readLine()) != null;) {
						builder.append(line.trim());
					}
				} catch (UnsupportedEncodingException e) {
					return e;
				} catch (IOException e) {
					return e;
				} catch (RuntimeException e){
					return e;
				} finally {
					if (reader != null){
						try {
							reader.close();
						} catch (IOException e) {
						}
					}
				}
			}
			catch(Exception e){
				return e;
			}
			return null;
        }
		
		@Override
        protected void onPostExecute(Exception exception) {
			super.onPostExecute(exception);
        	
       		if(exception != null){
       			if(parent.getStatus().equals(P2PStatus.INTERNET_CHECK_BEGIN)){
       				parent.setStatus(P2PStatus.INTERNET_FAILURE);
       			}
       			else{
       				//ignore and keep last status
       			}
        	}
        	else{
       			parent.setStatus(P2PStatus.INTERNET_SUCCESS);
        	}
        }
}
