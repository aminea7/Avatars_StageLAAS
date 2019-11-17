package fr.insa.laas.SocialNetwork;


import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import fr.insa.laas.Ontology.*;
import fr.insa.laas.Avatar1.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


public class SocialNetwork {

	//Attributs
	private String avatarName;
	private MetaAvatar metaAvatar;
	//Social Network
	private ArrayList <MetaAvatar> socialNetwork = new ArrayList <MetaAvatar>() ; 
	private ArrayList <String> InteretsTasksList = new ArrayList <String>();			 //The interets of the tasks to do, we need a delegate for each one
	private ArrayList <Interest> interestsList = new ArrayList <Interest> () ;			 //Used to iterate and to get the level interest easily for each task
	private ArrayList <String> interestsDelegates =  new ArrayList <String>(); 			 //Contains triples "InterestX/AvatarX/SocialDist"
	//Social Distance Calculs
	private SocialNetworkCalculs socialDistance = new SocialNetworkCalculs();
	
	//Constructor
	public SocialNetwork(MetaAvatar mAvatar, ArrayList <String> iTL){
		avatarName=mAvatar.getName();
		metaAvatar=mAvatar;
		InteretsTasksList=iTL;
		interestsList=metaAvatar.getInterestsList();
	}
	
	//Update of the Social Network from an Xml response from a request to a certain Repository
	public void SocialNetworkUpdate(String xml, MetaAvatar metaAvatar, ArrayList <String> iTL){

		try {
		    InputSource is = new InputSource(new StringReader(xml));
		    //System.out.println(xml);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(is);
			NodeList nList = doc.getElementsByTagName("m2m:cin");		

			//Iterate the avatars contained in the repository
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);		
				//System.out.println("\nCurrent Element :" + nNode.getNodeName());		
				//Check if it's an Avatar and if it's not himself or an old friend
				if ((nNode.getNodeType() == Node.ELEMENT_NODE) && !(((Element) nNode).getAttribute("rn").equals(metaAvatar.getName())) && !(ContainsFriend(((Element) nNode).getAttribute("rn")))) {

					Element eElement = (Element) nNode;
					//System.out.println("ELEMENT : " + eElement.getTextContent());
					Node n = eElement.getElementsByTagName("lbl").item(0);
					Element e = (Element) nNode;
					String data = e.getTextContent();
					String name = eElement.getAttribute("rn");
					String owner = data.split("<owner>")[1].split("</owner>")[0];
					String longitude  = data.split("<longitude>")[1].split("</longitude>")[0];
					String latitude = data.split("<latitude>")[1].split("</latitude>")[0];
					String nbInterest = data.split("<nb_interest>")[1].split("</nb_interest>")[0];
					String url = data.split("<url>")[1].split("</url>")[0];

					//Iterates the interests of the friend Avatar to complete its interets list and vector 
					Map<String,Double> interestsV = new HashMap<String, Double>();	
					ArrayList <Interest> interestsL = new ArrayList <Interest> () ;			//Used to iterate

					for (int i=0; i<Integer.parseInt(nbInterest); i++){
						String interest=data.split("<interest>")[i+1].split("</interest>")[0];
						interestsV.put(interest.split("/")[0], Double.parseDouble(interest.split("/")[1]));
						interestsL.add(new Interest(interest.split("/")[0], Double.parseDouble(interest.split("/")[1])));
						//System.out.println("Interest n°"+(i+1)+": name= "+interest.split("/")[0]+", value= "+Double.parseDouble(interest.split("/")[1]));
					}

					//Social Distance calcul
					double socialDistanceRes = socialDistance.SocialDistance(metaAvatar, interestsV,Double.parseDouble(latitude), Double.parseDouble(longitude), owner);
					//System.out.println("[SD from "+avatarName+"]: "+name+"= "+socialDistance);
					
					//Creation of a friend Meta Avatar
					MetaAvatar friend = new MetaAvatar(name, owner, Double.parseDouble(latitude), Double.parseDouble(longitude), interestsV, interestsL, socialDistanceRes, url);
					addAvatar(friend);
					System.out.println("[ADDING A FRIEND]: "+avatarName+" add "+name);
					}
			}
			//Update the delegations friends for each interest as we have a new friend that can become a delegate in the future
			DetermineDelegate();
		    } catch (Exception e) {
			e.printStackTrace();
		    }
	}
	
	//For each interest, determine the delegate friend who will take care of each task having this interest 
	public void DetermineDelegate(){
		interestsDelegates.clear();		//Clear the delegations list as we fill it 
	
		//Iterate its interest
		for (int i=0; i<InteretsTasksList.size(); i++){
			String interest=InteretsTasksList.get(i);
			Double minSD = 0.2 + 1.5*0.4 + GetInterestLevel(interest)*0.4;		//Its Own Social Distance
			//System.out.println("[DETERMINE DELEGATE]"+avatarName+":Own SD= "+minSD+" for "+interest);
			String delegate=avatarName;
			
			//Iterate all its friends
			for (int f=0; f<socialNetwork.size(); f++){

				if (socialNetwork.get(f).ContainsInterest(interest)!=-1.0){		//Get the interest level for this friend
					Double interestLevel= socialNetwork.get(f).ContainsInterest(interest);
					Double socialDistInt = socialDistance.SocialDistanceInterest(metaAvatar, socialNetwork.get(f),interestLevel);
					//System.out.println("[DETERMINE DELEGATE]"+avatarName+": Yes the friend "+socialNetwork.get(f).getName()+" has the interest "+interest+" with a lvl: "+interestLevel+" and a SD: "+socialDistInt);
					
					//Check if its friend has a better SD than its
					if (socialDistInt>minSD){
						minSD=socialDistInt;
						delegate=socialNetwork.get(f).getName();
					}		
				}
			}
			interestsDelegates.add(interest+"/"+delegate+"/"+minSD);
			//System.out.println("[ADD INTERESTS DELEGATES]"+avatarName+" add: "+delegate+" as delegate for "+interest);
		}
		//System.out.println("[SHOW INTERESTS DELEGATES]"+avatarName+": "+interestsDelegates.toString());
	}
	
	//When a delegate Avatar cannot satisfy a task and choose another Delegate from its friends
	public String DetermineAnotherDelegate(MetaAvatar metaAvatar, String interest, String originalSender, String delegationFrom){
		
		Double minSD = -999999.0;		//Its Own SD
		String delegate="noOne";
		//System.out.println("[DETERMINE DELEGATE]"+avatarName+":Own SD= "+minSD+" for "+interest);
		
		//Iterate all its friends
		for (int f=0; f<socialNetwork.size(); f++){
			//System.out.println("[DETERMINE ANOTHER DELEGATE] TEST WITH "+socialNetwork.get(f).getName());

			if (socialNetwork.get(f).ContainsInterest(interest)!=-1.0 && !socialNetwork.get(f).getName().equals(originalSender) && !socialNetwork.get(f).getName().equals(avatarName) && !socialNetwork.get(f).getName().equals(delegationFrom)){
				Double interestLevel= socialNetwork.get(f).ContainsInterest(interest);
				Double socialDistInt = socialDistance.SocialDistanceInterest(metaAvatar, socialNetwork.get(f),interestLevel);
				//System.out.println("[DETERMINE DELEGATE]"+avatarName+": Yes the friend "+socialNetwork.get(f).getName()+" has the interest "+interest+" with a lvl: "+interestLevel+" and a SD: "+socialDistInt);
				
				//Check if its friend has a better SD (Social Distance) than its
				if (socialDistInt>minSD){
					minSD=socialDistInt;
					delegate=socialNetwork.get(f).getName();
				}		
			}
		}
		//System.out.println("[DETERMINE DELEGATE]"+avatarName+": delegate="+delegate   +"  for "+interest);
		return delegate;
	}
	
						/****				Getters & Setters				******/
	
	public void addAvatar (MetaAvatar avatar){
		socialNetwork.add(avatar) ;
	}
	public  ArrayList <MetaAvatar> getSocialNetwork (){
		return socialNetwork ;
	}
	
	//Check if the avatar has a certain interest, if YES: Return the level, if FALSE: Return 99.0
	public Double GetInterestLevel(String interest){
		Double res=-99.0;
		for (int i=0; i<interestsList.size(); i++){
			if(interestsList.get(i).getName().equals(interest)){
				res=interestsList.get(i).getLevel();
			}
		}
		return res;
	}
	
	//Return the delegatesList
	public ArrayList <String> getDelegatesList(){
		return interestsDelegates;
	}
	
	//Get the delegate of a certain Interest
	public String getDelegate(String interest){
		String delegate=null;
		for (int i=0; i<interestsDelegates.size(); i++){
			if (interestsDelegates.get(i).contains(interest)){
				delegate = interestsDelegates.get(i).split("/")[1];
				//System.out.println("[GET DELEGATE]"+avatarName+": "+"the delegate of "+interest+" is "+delegate);
			}
		}
		return delegate;
	}
	
	//Show its friends names
	public void showSocialNetwork(){
		System.out.println("[SHOW SOC. NET.]" +avatarName+": ");
		for (int i=0; i<socialNetwork.size(); i++){
			System.out.println("Friend n°"+i+": "+socialNetwork.get(i).getName());
		}
	}
	
	//Returns true if he has a friend with this name
	public boolean ContainsFriend(String name){
		boolean res=false;
		for (int i=0; i<socialNetwork.size(); i++){
			if (socialNetwork.get(i).getName().equals(name)){
				res=true;
				break;
			}
		}
		return res;
	}
	
	//Return the meta data of an avatar friend from its name
	public MetaAvatar getFriend(String name){
		MetaAvatar res=null;
		for (int i=0; i<socialNetwork.size(); i++){
			if (socialNetwork.get(i).getName().equals(name)){
				res=socialNetwork.get(i);
				break;
			}
		}
		return res;
	}
	
	
}
