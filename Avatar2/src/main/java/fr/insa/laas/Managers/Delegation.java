package fr.insa.laas.Managers;

public class Delegation {

	//Attributs
	private String delegationFrom;	//Avatar who sent him the delegation
	private String conversationId;
	private int nbRequests; 		//Nb of Avatars he request about this delegation
	
	//Constructor
	public Delegation(String dF, String cI, int nbR){
		delegationFrom=dF;
		conversationId=cI;
		nbRequests=nbR;
	}

	//Getters & Setters 
	public String getDelegationFrom(){
		return delegationFrom;
	}
	public String getConversationId(){
		return conversationId;
	}
	public int getNbRequests(){
		return nbRequests;
	}
	public void DecrementeNbReq(){
		nbRequests--;
	}
}
