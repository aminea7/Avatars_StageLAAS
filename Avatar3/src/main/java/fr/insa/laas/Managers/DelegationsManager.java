package fr.insa.laas.Managers;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import fr.insa.laas.Ontology.*;
import fr.insa.laas.SocialNetwork.*;
import fr.insa.laas.Avatar3.*;

import org.omg.CORBA.portable.Delegate;

public class DelegationsManager {

	//Attributs
	private ArrayList <Delegation> delegationsList = new ArrayList <Delegation>();
	private String avatarOwner;
	
	//Constructor
	public DelegationsManager(String a){
		avatarOwner=a;
	}
	
	//Browse in the delegations List a certain Delegation from ConvID, If yes ==> Return the delegation instance, if false ==> Return null
	public Delegation ContainsDelegation(String conversation){
		Delegation delegation= null;
		for (int i=0; i<delegationsList.size(); i++){
			if (delegationsList.get(i).getConversationId().equals(conversation)){
				delegation= delegationsList.get(i);
				break;
			}
		}
		return delegation;
	}
	
	//Add a delegation to the list
	public void AddDelegation(Delegation d){
		delegationsList.add(d);
		System.out.println("	[ADD DELEGATION]"+avatarOwner+" add the conv: "+d.getConversationId()+" from "+d.getDelegationFrom());
	}
	
	//Return -1 if not successed (7 is successed)
	public int RemoveDelegation(Delegation del){
		int res=-1;
		for (int i=0;i<delegationsList.size(); i++){
			if (delegationsList.get(i).getConversationId().equals(del.getConversationId())){
				res=7;
				//System.out.println("			[DELETE DELEGATION]"+avatarOwner+" delete the conv: "+delegationsList.get(i).getConversationId()+" from "+delegationsList.get(i).getDelegationFrom());
				delegationsList.remove(i);
			}
		}
		return res;
	}
	
	
	//A TimeOut activation 20sec after receiving a delegation
	public String ManageTimeOut2(String msgDelegation){
		String msgToSend = null;
		String conversationId = getXmlElement(msgDelegation, "conversationId");
		String date = getXmlElement(msgDelegation,"date");
		Delegation delegation = ContainsDelegation(conversationId);
		
		//The Delegated task is still in the list (He didn't receive any positive response during that time) ==> inform the original sender about the failure
		if (delegation != null){
			msgToSend = "<type>failureTimeOut</type>";
			msgToSend = addXmlElement(msgToSend,"receiver",delegation.getDelegationFrom());
			msgToSend = addXmlElement(msgToSend,"conversationId",conversationId);
			msgToSend = addXmlElement(msgToSend,"date",date);
			msgToSend = addXmlElement(msgToSend,"sender",avatarOwner);
			msgToSend = addXmlElement(msgToSend,"content","failuretimeout");
		}
		//He received a positive response and this task was treated
		else{
			//System.out.println("YYYYYYYYYYYOOOOSSS for the task: "+msg.getConversationId().split("&")[1]);
		}
		return msgToSend;
	}
	
	//After receiving a failure message
	public String ManageFailureRequest2(String msg, SocialNetwork socialNetwork, MetaAvatar metaAvatar){
		String content = getXmlElement(msg,"content");
		String conversationId = getXmlElement(msg,"conversationId");
		String date = getXmlElement(msg,"date");
		String sender = getXmlElement(msg,"sender");
		
		String res = null;
		Delegation delegation = ContainsDelegation(conversationId);
		
		//He's the delegate of this conv.
		if (delegation != null){
			
			Instant firstInstant = Instant.parse(date);
			Duration duration = Duration.between(firstInstant, Instant.now());
			String delegationFrom = delegation.getDelegationFrom();

			//Test if the Timeout is over (more than 20 sec)
			if ((duration.toMillis()/1000)>20){ 	//It's dead
				
				//Send Failure + Remove from DelegateTasks
				//System.out.println("			[CONTROL FAILURE]   NO MORE TIME  "+avatarOwner+"   "+msg.getContent());
				RemoveDelegation(delegation);
				//TBD: What if he's its own delegate ? He don't have this request to itself
				res = "<type>failure</type>";
				res = addXmlElement(res,"conversationId",conversationId);
				res = addXmlElement(res,"date",date);
				res = addXmlElement(res,"sender",avatarOwner);
				res = addXmlElement(res,"content","No more time");
				res = addXmlElement(res,"receiver",delegationFrom);
			}
			//There is still time
			else {	
				delegation.DecrementeNbReq();
				int requests = delegation.getNbRequests();	//The nb of people that still have to answer him
				
				//All the avatars requested answered failures
				if (requests==0){	
					//Determine a new Delegate
					String secondDelegate=socialNetwork.DetermineAnotherDelegate(metaAvatar,  conversationId.split("&")[3], conversationId.split("&")[0], delegation.getDelegationFrom());
					res="<secondDelegate>"+secondDelegate+"</secondDelegate><delegationFrom>"+delegationFrom+"</delegationFrom>";
					//System.out.println("			[CONTROL FAILURE] THERE IS STILL MORE TIME  BUT NO MORE REQ "+secondDelegate+"  "+avatarOwner+"  "+msg.getContent()+"   "+message.getContent());
				}
				//There's still Avatars that had not answered
				else {
					//System.out.println("			[CONTROL FAILURE] THERE IS STILL MORE TIME  AND THERE IS STILL REQUESTS    "+avatarOwner+"   "+msg.getContent());
				}
			}
		}
		//He's not the delegate of this conversation
		else{
			//TBD: Answer: wrong destinator
		}
		return res;
	}
	
	/*
	public ACLMessage ManageConfirmRequest(ACLMessage msg, SocialNetwork socialNetwork, MetaAvatar metaAvatar, ServicesManager servicesManager, ArrayList <Goal> goalsList){
		ACLMessage msgToSend = null;
		Delegation delegation = ContainsDelegation(msg.getConversationId());
		
		//He's the delegate of this conv.
		if (delegation != null){
			
			//Update his servicesList so that he knows who can execute this task
			//TBD: Ne careful if the sender is the real supplier or just a propagate avatar
			servicesManager.addService(new Service(msg.getSender().getLocalName(),msg.getContent().split("&")[0],msg.getContent().split("&")[1] )); 	//New Service(avatarX, serviceY, serviceLabelY)
			

			
			if (!msg.getConversationId().split("&")[0].equals(avatarOwner)){

				//He's the delegate of this conversation, so he has to inform the original sender 
				//TBD: URGENT !!!! Verify if he's the delegate, and if he's the delegate of another delegate and not of the original sender USE DELEGATE LIST

				String dest= delegation.getDelegationFrom();
				//Ack Message: 
				ACLMessage message = new ACLMessage(ACLMessage.CONFIRM) ;
				message.addReceiver(new AID(dest,AID.ISLOCALNAME));
				message.setInReplyTo(msg.getConversationId());
				message.setContent(msg.getContent()); 	//msg= Service77&Label77&Qos77 &NameOfSupplier (Exp)
				message.setConversationId(msg.getConversationId());
				message.setReplyByDate(msg.getReplyByDate());
				msgToSend=message;
				
				//cptMessages++;
				//System.out.println("["+name+":Confirm Message to "+dest+"]: "+message.getContent()+", conversation: "+message.getConversationId()+", nbMessages="+cptMessages+"    "+message.getPerformative()) ;
				
			}
			RemoveDelegation(delegation);

		}
		//He's not the delegate of this conversation, and he's not supposed to receive that
		else{
			//System.out.println("YYYYYYYYYYYOOOOSSS for the task: "+msg.getConversationId().split("&")[1]);
		}
		
		//Check if he is the original sender 
		if (msg.getConversationId().split("&")[0].equals(avatarOwner)){
			//Update its strct data of Tasks, first test if the supplier is in SocialNetwork to add save its name, otherwise, save the propagate (or just sender) avatar name
			String taskName=msg.getConversationId().split("&")[1];
			String supplier = msg.getContent().split("&")[3];
			
			if(socialNetwork.ContainsFriend(supplier)){
				//Add him as a supplier(actor) for this task
				goalsList.get(0).setActorTask(supplier, taskName, goalsList.get(0).getTasksList());
			}
			else{
				//Add the the sender 
				goalsList.get(0).setActorTask(msg.getSender().getLocalName(), taskName, goalsList.get(0).getTasksList());
			} 
			
			
			//goalsList.get(0).setActorTask(sender, taskName, goalsList.get(0).getTasksList());
			//TBD: Maybe its a friend of the sender
			//System.out.println("	[AVATAR FRIEND CAN DO TASK]"+name+": 	"+msg.getConversationId().split("&")[1]+",		by: "+sender 	+",total="+cptTasks);
		}
		return msgToSend;
	} */
	
	//Get an element from XML
	public String getXmlElement(String xml, String element){
		String res = "notFound";
		res = xml.split("<"+element+">")[1].split("</"+element+">")[0];
		return res;
	}
	
	//Add information to XML 
	public String addXmlElement(String xml, String element, String value){
		xml = xml+"<"+element+">"+value+"</"+element+">";
		return xml;
	}
}
