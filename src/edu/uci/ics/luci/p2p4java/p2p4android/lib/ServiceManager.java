package edu.uci.ics.luci.p2p4java.p2p4android.lib;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class ServiceManager {
	
	private Class<? extends AbstractService> serviceClass;
	private Context context;
    private boolean serviceIsBound;
    private Messenger serviceMessenger = null;
    private Messenger incomingMessenger = null;
    
    private static class IncomingHandler extends Handler {
    	
    	private Handler incomingHandler = null;
    	
		IncomingHandler(Handler incomingHandler){
			this.incomingHandler = incomingHandler;
		}
		
        @Override
        public void handleMessage(Message msg) {
        	if (incomingHandler != null) {
        		Log.i(this.getClass().getCanonicalName(), "Incoming message. Passing to handler: "+msg);
        		incomingHandler.handleMessage(msg);
        	}
        }
    }
    
    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            serviceMessenger = new Messenger(service);
            Log.i("ServiceHandler", "Attached.");
            try {
                Message msg = Message.obtain(null, AbstractService.MSG_TO_REGISTER_CLIENT);
                msg.replyTo = incomingMessenger;
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            Log.i(this.getClass().getCanonicalName(), "Disconnected");
            serviceMessenger = null;
        }
    };
    
    public ServiceManager(Context context, Class<? extends AbstractService> serviceClass, Handler incomingHandler) {
    	this.context = context;
    	this.serviceClass = serviceClass;
    	this.incomingMessenger = new Messenger(new IncomingHandler(incomingHandler));
    }

    public void start() {
    	doStartService();
    }
    
    public void bind() {
    	doBindService();
    }
    
    public void rebind() {
    	doBindService();
    }
    
    public void stop() {
    	doStopService();    	
    }
    
    public void unbind() {
    	doUnbindService();
    }
    
    public boolean isRunning() {
    	ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    
	    return false;
    }
    
    public boolean isBound(){
    	return this.serviceIsBound;
    }
    
    public void send(Message msg) throws RemoteException {
    	if (serviceIsBound) {
            if (serviceMessenger != null) {
            	serviceMessenger.send(msg);
            }
    	}
    }
    
    private void doStartService() {
    	context.startService(new Intent(context, serviceClass));    	
    }
    
    private void doStopService() {
    	context.stopService(new Intent(context, serviceClass));
    }
    
    private void doBindService() {
   		context.bindService(new Intent(context, serviceClass), serviceConnection, Context.BIND_ABOVE_CLIENT);
   		serviceIsBound = true;
    }
    
    private void doUnbindService() {
        if (serviceIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (serviceMessenger != null) {
                try {
                    Message msg = Message.obtain(null, AbstractService.MSG_TO_UNREGISTER_CLIENT);
                    msg.replyTo = incomingMessenger;
                    serviceMessenger.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            
            // Detach our existing connection.
            context.unbindService(serviceConnection);
            serviceIsBound = false;
            //textStatus.setText("Unbinding.");
            Log.i("ServiceHandler", "Unbinding.");
        }
    }
}
