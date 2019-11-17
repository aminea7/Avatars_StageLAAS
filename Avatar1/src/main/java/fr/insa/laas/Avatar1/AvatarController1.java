package fr.insa.laas.Avatar1;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.DefaultedHttpParams;
import org.apache.http.params.HttpParams;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;

import fr.insa.laas.Avatar1.*;



@RestController
public class AvatarController1 implements ErrorController {

	private static final int serverPort = 9701;
	private static final String avatarName="Avatar1";
	private Avatar avatar;
	private static int nbRequestsA1 = 0;
	
	public AvatarController1(){
		//System.out.println("			OK MAIN");		
		String urlAvatar="http://localhost:"+serverPort+"/"+avatarName+"/";
		ArrayList <String> Repo1 = new ArrayList <String> ();
		Repo1.add("REPOSITORY1");
		avatar = new Avatar(urlAvatar,"src/main/resources/OntologyFiles/Avatar1.owl",Repo1);

	}
	
	//Intro (Test)
	@GetMapping(value="/Services/")
	public String TEST(@PathVariable String serviceX){
		//System.out.println("TEST");
		return "Welcome to Services of the avatar "+avatarName;
	}
	
	//GET Services Requests
	@RequestMapping(value="/Services/{serviceX}", method=RequestMethod.GET)
	public String GetService(@PathVariable String serviceX){
		System.out.println("Someone wants the Service "+serviceX);
		String res = avatar.GETService(serviceX);
		return res;
	}
	
	//GET Ask Request (about a certain a task, not in a scenario of a process)
	//Exp: GET http://localhost:9701/Avatar1/?type=ask&task=Task2/Label2
	@RequestMapping(value="", method=RequestMethod.GET)
    public String GETAskRequest (@RequestParam("type") String type, @RequestParam("task") String taskData){
		
		//Split the Task and Data and check the URI format
		String taskName=taskData.split("/")[0];
		String taskLabel=taskData.split("/")[1];
		if (!taskName.contains("http")){	//We add the complete address if it's only "TaskX"
			taskName="http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#"+taskName;
		}
		String res=avatar.GETAskRequest(taskName, taskLabel);
		return res;
	}
		
	//Requests
	@RequestMapping(value="")
    public String TreatHTTPRequests (@RequestBody String request, @RequestParam("type") String type) throws IOException{
	
		String sender = getXmlElement(request, "sender");
		nbRequestsA1++;
		System.out.println("[CONTROLLER of "+avatarName+"] Received a "+type+" Request from "+sender+" total requests="+nbRequestsA1);		
		String res=null;	
		switch (type){
		
		//Ask Request (about a certain Task)
		case "ask":
			//System.out.println("			RECEIVED ASK REQUEST");		
			//res="OK REQ";
			res=avatar.AskRequest(request);
			break;
			
		//Accept Request (about a Service proposition received)
		case "accept":
			res=avatar.AcceptRequest(request);
			break;	
			
		//Failure Request (When a delegate failed)
		case "failure":
			res=avatar.FailureRequest(request);
			break;	
	
		//Failure Request (because of TimeOut)
		case "failuretimeout":
			String conversationIdTO = getXmlElement(request,"conversationId");
			res="<type>okfailureTO</type><content>okFTO</content><sender>"+avatarName+"</sender><conversationId>"+conversationIdTO+"</conversationId><date>date</date>";
			//res=avatar.FailureTimeOutRequest(request);
			break;
			
		//Failure Request (because of TimeOut)
		case "propagate":
			res=avatar.PropagateRequest(request);
			break;
			
		default:
			System.out.println("DEBUG NOTYPE ERROR, type= "+type);		
    		res = addXmlElement(res,"type","NoType");
			res = addXmlElement(res,"conversationId","NoConv");
			res = addXmlElement(res,"date","Nodate");
			res = addXmlElement(res,"sender",avatarName);
			res = addXmlElement(res,"content","noContent");

			break;
		}	

		return res;
	}

	
	
	/*
	public void main(String[] args) {
		ArrayList <String> Repo1 = new ArrayList <String> ();
		Repo1.add("REPOSITORY1");
		System.out.println("			OK MAIN");		

	}*/
	
	/*
	public Controller(String an){
		avatarName=an;
		//System.out.println("			CREATION OF THE CONTROLLER OF  "+an);		
	}*/
	
   // @RequestMapping(value="/Produits", method=RequestMethod.GET)
	/*
    public String listeProduits() {
        return "Un exemple de produit";
    }*/
    
/*
	//"http://localhost:9797/~/mn-cse/mn-name/Repository1/op=setOn&lampid=2"
    //@RequestMapping(value="/~/mn-cse/mn-name/Repository1/{ops}", method=RequestMethod.GET)
    @RequestMapping(value=("/~/mn-cse/mn-name/Avatar1"))

	//public String listeOps (@PathVariable String ops) {	
    public ControllerMessage TreatHTTPRequests (@RequestBody String body, @RequestParam("type") String type, @RequestParam("service") String service) throws ParserConfigurationException{
    
       System.out.println("			CONTROLLER; RECIVED:  "+"   "+body);	
       ControllerMessage message= new ControllerMessage();
       
       if (true){
    	   
    	   	//if ((ops.split("=")[1]).equals("Service3")){
	    	if(type.equals("request")){
	    		message = new ControllerMessage("AvatarX", "contentC", "conversation n1200", "0115742");
	    		System.out.println("						CONTROLLER: REQUEST YES   "+message.toString());	    		
	        	
	    	}
	    	else {
	    		//System.out.println("						CONTROLLER: REQUEST NO");
	        	//return new ControllerMessage();
	    	}
       }
       else{
    	   
       }
       return message;
	    	
    }*/

	
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
	
	@Override
	public String getErrorPath() {
		return "ERROR GETERRORPATH";
	}

}