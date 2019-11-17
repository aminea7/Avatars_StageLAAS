package fr.insa.laas.Managers;

import java.io.IOException;
import java.util.ArrayList;

import fr.insa.laas.Avatar2.MetaAvatar;
import fr.insa.laas.HTTP.Client;
import fr.insa.laas.HTTP.ClientInterface;
import fr.insa.laas.HTTP.Response;
import fr.insa.laas.Ontology.Service;
import fr.insa.laas.SocialNetwork.SocialNetwork;


public class ServicesManager {

	//Attributs
	private String avatarOwner;
	private SocialNetwork socialNetwork;
	private ArrayList <MetaAvatar> socialNetworkList = new ArrayList <MetaAvatar>() ; 
	private ArrayList <Service> servicesList = new ArrayList <Service> ();
	//Communication
	private ClientInterface client = new Client();
	private final String ORIGINATOR = "admin:admin";
	private static int cptTasksExecuted=0;	//To test

	//Constructor
	public ServicesManager(String n){
		avatarOwner=n;
		//socialNetwork=sn;
	}
	
	//When we discover a new Friend
	public void UpdateSN(SocialNetwork sn){
		socialNetwork=sn;
		socialNetworkList = sn.getSocialNetwork();
	}
	
	//Treat the execution requests
	public String ServiceExecution2 (String service){
		
		String res =  "<type>inform</type>";
		//TBD: Raccourcir IsableFirnd ac le ExtractServ			
		//System.out.println("			OPERATIONS MANAGER  ["+avatarOwner+"] "+task+", for "+sender+"  tasksExec="+cptTasksExecuted); 

		//Search it
		for (int i=0; i<servicesList.size(); i++){

			//System.out.println("		SM COMPARAISON   "+ service+"  and  "+servicesList.get(i).getName()+"  "+servicesList.size());
			//NB: ExtractServiceFromLabel returns : ServiceX&LabelX&QosX
			if (servicesList.get(i).getName().equals(service) || servicesList.get(i).getName().split("#")[1].equals(service)){
				
				String supplier=servicesList.get(i).getSupplier();
				//Test if he is the supplier of this service or if it's a friend
				if(supplier.equals(avatarOwner)){
					
					String out=servicesList.get(i).getServiceOp().getOutputMessage();
					if (servicesList.get(i).getServiceOp().getMethode().equals("GET")){
						res = addXmlElement(res,"sender",avatarOwner);
						res = addXmlElement(res,"content",out);
					}
					
					//TBD: Treat the PUT messages !!!
					else if (servicesList.get(i).getServiceOp().getMethode().equals("PUT")){
						String in=servicesList.get(i).getServiceOp().getInputMessage();
					}
				}
				
				//Ask this friend (supplier) by a HTTP Request
				else{
				  //System.out.println("		SM ASK FRIEND  "+supplierMeta.getName());
				  String friendURL = socialNetwork.getFriend(supplier).getURL();
				  Response resp;
				  try {
						resp = client.request(friendURL+"?type=query&service="+service, ORIGINATOR, "NoContent");
						if (resp.getStatusCode() != 201 || resp.getRepresentation().isEmpty()) {
							System.out.println("AVATAR: HTTP RESPONSE of "+avatarOwner+":"+ resp.getRepresentation());		
							//ResponsesTreatment(resp.getRepresentation());
							String output=getXmlElement(resp.getRepresentation(),"content");
							//res=addXmlElement(res,"content",output);
							res=resp.getRepresentation();
						}
						else{
							System.out.println("AVATAR: HTTP REQUEST ERROR");
						}
				
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			}
		}
		if (res.equals("<type>inform</type>")){
			res=res+"<content>NoSuchServiceAvailable</content>";
		}

		return res;
	}
	
	public void ExecuteOwnService(String task, String taskLabel){
	
		//Search it
		for (int i=0; i<servicesList.size(); i++){

			// System.out.println("		COMPARAISON   "+ ExtractServiceFromLabel(taskLabel).split("&")[0]+"  and  "+servicesList.get(i).getName()+"  "+servicesList.size());
			//NB: ExtractServiceFromLabel returns : ServiceX&LabelX&QosX
			if (servicesList.get(i).getLabel().equals(taskLabel)){
				
				String supplier=servicesList.get(i).getSupplier();
				//Test if he is the supplier of this service or if it's a friend
				if(supplier.equals(avatarOwner)){
					
					String out=servicesList.get(i).getServiceOp().getOutputMessage();
					if (servicesList.get(i).getServiceOp().getMethode().equals("GET")){						
						//System.out.println("		[EXECUTION TASK ITSELF] ["+avatarOwner+"] "+task+",  tasksExec="+cptTasksExecuted); 

					}
					//TBD: Treat the PUT operations 
					else if (servicesList.get(i).getServiceOp().getMethode().equals("PUT")){
					}
					
				}

			}
		}
	}

	//Getters & Setters:
	public void addService(Service s){
		servicesList.add(s);
	}
	public  ArrayList <Service> getServices (){
		return servicesList;
	}
	
	//Get an element from XML
	public String getXmlElement(String xml, String element){
		String res = "notFound";
		res = xml.split(element)[1].split(element)[0];
		return res;
	}
	
	//Add information to XML 
	public String addXmlElement(String xml, String element, String value){
		return xml+"<"+element+">"+value+"</"+element+">";
	}
	
	//Get the URL of a friend from its name
	public String getURLFriend(String friend){
		String res="friendNoFound";
		for (int i=0;i<socialNetworkList.size() ;i++){
			if (socialNetworkList.get(i).getName().equals(friend)){
				res=socialNetworkList.get(i).getURL();
			}	break;
		}
		return res;
	}
}