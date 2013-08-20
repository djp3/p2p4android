package edu.uci.ics.luci.p2p4java.p2p4android.lib;

public enum P2PStatus {
	OFF,
	STARTING,
	CHARSET_CHECK_BEGIN, CHARSET_FAILURE, CHARSET_SUCCESS,
	INTERNET_CHECK_BEGIN, INTERNET_FAILURE, INTERNET_SUCCESS,
	P2P_INITIALIZATION, P2P_INIT_NETWORK_MANAGER, P2P_INIT_NETWORK_CONFIGURATION, P2P_INIT_NETWORK, P2P_INIT_PEER_GROUP, P2P_FAILURE, P2P_SUCCESS, P2P_INIT_PIPE_SERVICE, P2P_INIT_PIPE_SERVICE_AD,
	RUNNING;

	public static String translate(P2PStatus x){
		if(x.equals(P2PStatus.OFF)){
			return "The peer to peer networking service is off";
		}
		else if(x.equals(P2PStatus.STARTING)){
			return "The peer to peer networking service is initializing";
		}
		else if(x.equals(P2PStatus.CHARSET_CHECK_BEGIN)){
			return "The peer to peer networking service is checking that the character set is UTF-8";
		}
		else if(x.equals(P2PStatus.CHARSET_FAILURE)){
			return "The peer to peer networking service failed to start because the character set is not UTF-8";
		}
		else if(x.equals(P2PStatus.CHARSET_SUCCESS)){
			return "The peer to peer networking service confirmed the character set is UTF-8";
		}
		else if(x.equals(P2PStatus.INTERNET_CHECK_BEGIN)){
			return "The peer to peer networking service is checking that the internet is reachable";
		}
		else if(x.equals(P2PStatus.INTERNET_FAILURE)){
			return "The peer to peer networking service failed to start because the internet is not reachable";
		}
		else if(x.equals(P2PStatus.INTERNET_SUCCESS)){
			return "The peer to peer networking service confirmed the internet is reachable";
		}
		else if(x.equals(P2PStatus.P2P_INITIALIZATION)){
			return "The peer to peer networking service is initializing";
		}
		else if(x.equals(P2PStatus.P2P_INIT_NETWORK_MANAGER)){
			return "The peer to peer service network manager is initializing";
		}
		else if(x.equals(P2PStatus.P2P_INIT_NETWORK_CONFIGURATION)){
			return "The peer to peer service is configuring it's network";
		}
		else if(x.equals(P2PStatus.P2P_INIT_NETWORK)){
			return "The peer to peer service is establishing connections";
		}
		else if(x.equals(P2PStatus.P2P_INIT_PEER_GROUP)){
			return "The peer to peer service is retrieving its peer group";
		}
		else if(x.equals(P2PStatus.P2P_INIT_PIPE_SERVICE)){
			return "The peer to peer service is retrieving its pipe service";
		}
		else if(x.equals(P2PStatus.P2P_INIT_PIPE_SERVICE_AD)){
			return "The peer to peer service is retrieving its pipe service advertisement";
		}
		else if(x.equals(P2PStatus.P2P_FAILURE)){
			return "The peer to peer networking service failed to start";
		}
		else if(x.equals(P2PStatus.P2P_SUCCESS)){
			return "The peer to peer networking service successfully started";
		}
		else if(x.equals(P2PStatus.RUNNING)){
			return "The peer to peer networking service is running";
		}
		else{
			throw new IllegalStateException("The peer to peer networking service is in an unknown condition");
		}
	}
	
	public String translate(){
		return translate(this);
	}

}