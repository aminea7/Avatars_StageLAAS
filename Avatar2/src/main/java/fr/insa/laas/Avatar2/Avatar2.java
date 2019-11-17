package fr.insa.laas.Avatar2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import fr.insa.laas.Avatar2.*;
import fr.insa.laas.HTTP.*;
import fr.insa.laas.Managers.*;
import fr.insa.laas.Ontology.*;
import fr.insa.laas.SocialNetwork.*;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

//import org.eclipse.om2m.commons.constants.Constants;
//import org.eclipse.om2m.commons.constants.MimeMediaType;
//import org.eclipse.om2m.commons.resource.ContentInstance;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;



public class Avatar2 {
	/*	------------------------------------------- 		D A T A 		--------------------------------------------------------	*/
	/*																																	*/
	/*	-----------------------------------------------------------------------------------------------------------------------------	*/
	
	//Attributs
	private String name;
	private String msgToSend;
	private String owner;
	//Location
	private double latitude=99;
	private double longitude=99;
	//Interets
	private ArrayList <Interest> interestsList = new ArrayList <Interest> ();	//Used to get a specific data, using an index for exp
	private Map<String,Double> interestsVector = new HashMap<String, Double>();	//Used to calculate the Social Distance using its vector shape
	
	//Social Network
	private SocialNetwork socialNetwork ;
	private MetaAvatar metaAvatar = null ; 		//Contains its meta Data
	private ArrayList <String> InteretsTasksList = new ArrayList <String>();		//Contains the all the kind of interests in its tasks

	//OM2M//"http://localhost:8080/~/mn-cse/mn-name/Repository1"
	private String repoName;    //Repository1
	private ArrayList <String> repoNameList = new ArrayList <String>();
	private final String ORIGINATOR = "admin:admin";
	private ClientInterface client = new Client();
	//private MapperInterface mapper = new Mapper();
	//HTTP Server
	
	//Ontology
	private String data;			//RDF Properties 
	private String rules;			//Semantic Rules
	private Model modelData;		//Model
	
	//Cache
	private ArrayList <Goal> goalsList = new ArrayList <Goal> () ;	//Contains all the goals to reach
	private ArrayList <String> delegateTasks = new ArrayList <String> (); //Contains the id Conversation about the tasks for whom he became the delegate "&&&" nb of requests
	
	//Supervision/Optimisation
	private static int cptTasks=0;
	private static int cptMessages=0;
	private static int cptMessagesHTTP=0;
	private static int cptTasksExec=0;
	private static int nbRequestsA2;

	//Managers
	private ServicesManager servicesManager ;
	private DelegationsManager delegationsManager;
		
    /** CSE Type */
    public final String CSE_TYPE = System.getProperty("org.eclipse.om2m.cseType","IN-CSE");
    /** CseBase id. */
    public final String CSE_ID = System.getProperty("org.eclipse.om2m.cseBaseId","in-cse");
    /** CseBase name. */
    public final String CSE_NAME = System.getProperty("org.eclipse.om2m.cseBaseName", "in-name");

    //Controller
    //private ControllerAvatar controllerAvatar;	//NOT USED ?
	private String URL;
	
	//Constructor 
	public Avatar2(String url, String ontology,ArrayList repos){
		URL=url;
		data = ontology;	
	    //The model containing the data/ontology
		modelData = ModelFactory.createDefaultModel();
        modelData.read(data);
        repoNameList = repos;        //Repositories

        //Actions
        ExtractName();
		System.out.println("		[CREATION OF AVATAR]: "+name+"  "+URL);
		servicesManager = new ServicesManager(name);
		delegationsManager= new DelegationsManager(name);

		ExtractOwner();
		ExtractLocation();
		ExtractInterests();
		ExtractServices();
		
		ExtractGoals();
		//goalsList.get(0).showGoal();
		//ExportMetaData();
		
		//Make his Social Network
		try {TimeUnit.SECONDS.sleep(5);} catch (InterruptedException e) {e.printStackTrace();}
		FriendsResearch();
		servicesManager.UpdateSN(socialNetwork);

		/*
		//Ask his Social network to help him to find Avatars to execute the tasks he can't
		if (!goalsList.isEmpty() ){//&& name.equals("Avatar1")){
			try {
				BrowseTasks(goalsList.get(0).getTasksList());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
	}
	


	/*	--------------------------------------- 		A V A T A R   M E T H O D S 		-------------------------------------------	*/
	/*											Extract all the Avatar Data (its name, location, etc.)									*/
	/*	-----------------------------------------------------------------------------------------------------------------------------	*/
	
	//Get its name
	public void ExtractName(){ 
		String queryString = 
	    		"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"+ 
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+

	    	        "SELECT ?avatar "+
	    	        "WHERE {?avatar rdf:type avataront:Avatar ."+
	    	        "}";
		   
	    	    Query query = QueryFactory.create(queryString);
	    	    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
	    	    ResultSet results =  qe.execSelect();
	    	    //ResultSetFormatter.out(System.out, results);
	    	    while(results.hasNext()){ 
	    	    	QuerySolution binding = results.nextSolution(); 
	    	    	name=binding.get("avatar").toString().split("#")[1];
	    	    }
	}
	
	//Get its owner name
	public void ExtractOwner(){ 
		String queryString = 
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
	    	        "SELECT ?owner "+
	    	        "WHERE { "+   
	    	         "?avatar avataront:hasOwner ?owner ."+ 
	    	        "}";
		 
	    	    Query query = QueryFactory.create(queryString);
	    	    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
	    	    ResultSet results =  qe.execSelect();
	    	    //ResultSetFormatter.out(System.out, results);
	    	    while(results.hasNext()){ 
	    	    	QuerySolution binding = results.nextSolution(); 
	    	    	owner=binding.get("owner").toString();
	    			//System.out.println("[EXTRACTOWNER] "+name+": "+owner) ;		
	    	    }
	}
	
	//Get its Location
	public void ExtractLocation(){ 
		String queryString = 
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
	    	        "SELECT ?location "+
	    	        "WHERE { "+   
	    	         "?avatar avataront:hasLocation ?location ."+ 
	    	        "}";
		   
	    	    Query query = QueryFactory.create(queryString);
	    	    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
	    	    ResultSet results =  qe.execSelect();
	    	    //ResultSetFormatter.out(System.out, results);
	    	    while(results.hasNext()){ 
	    	    	QuerySolution binding = results.nextSolution(); 
	    	    	latitude=Double.parseDouble(binding.get("location").toString().split("/")[0]);
	    	    	longitude=Double.parseDouble(binding.get("location").toString().split("/")[1]);
	    	    	//System.out.println("[EXTRACTLOC] "+name+": "+latitude+"/"+longitude) ;		
	    	    }
	}
	
	//Get all its interests from the semantic data
	public void ExtractInterests(){ 
		String queryString = 
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
	    	        "SELECT ?interest "+
	    	        "WHERE { "+   
	    	         "?avatar avataront:hasInterest ?interest ."+
	    	         "?avatar rdf:type avataront:Avatar ."+
	    	        "}";

			    Query query = QueryFactory.create(queryString);
			    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
			    ResultSet results =  qe.execSelect();
			    //ResultSetFormatter.out(System.out, results);
			    String name2 = null;
			    
			    //For each Interest
			    while(results.hasNext()){ 
			    	QuerySolution binding = results.nextSolution(); 
			    	name2=binding.get("interest").toString();
	    	    	//Name and level Interest split
	    	    	String [] parts = name2.split("/");
	    	    	interestsList.add(new Interest(parts[0],Double.parseDouble(parts[1])));
	    	    	interestsVector.put(parts[0],Double.parseDouble(parts[1]));
	    	    }
				//System.out.println("[EXTRACTINTERETS] "+name+", list size: "+interestsList.size());
	}

	//Get all its services from the semantic data
	public void ExtractServices(){ 
		String queryString = 
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
	    	        "SELECT   ?service ?operation ?omsg ?met ?lab "+
	    	        "WHERE { "+   
	    	         "?avatar avataront:hasService ?service ."+
		    	     "?service avataront:hasLabel ?lab ."+
	    	         "?service <http://iserve.kmi.open.ac.uk/ns/msm#hasOperation>  ?operation ."+
	    	         "?operation <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#hasOutputMessage>  ?omsg ."+
	    	         "?operation <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#hasMethod>  ?met ."+
	    	         //TBD: Search the input msg if it's a PUT Method
	    	         //"?operation <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#hasInputMessage>  ?imsg ."+
	    	         "}";

			    Query query = QueryFactory.create(queryString);
			    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
			    ResultSet results =  qe.execSelect();
			    //ResultSetFormatter.out(System.out, results);
			    
			    //For each Service
			    //TBD: Each Service may have many operations
			    while(results.hasNext()){ 
			    	QuerySolution binding = results.nextSolution(); 
			    	//TBD: Check if a service has many ops
			    	String service=binding.get("service").toString();
			    	String serviceOp=binding.get("operation").toString();
			    	String outputMsg=binding.get("omsg").toString();
			    	//String inputMsg=binding.get("imsg").toString();
			    	String method=binding.get("met").toString();
			    	String label=binding.get("lab").toString();

			    	//Create a service instance and add it to the list
			    	ServiceOperation sOP = new ServiceOperation(serviceOp, method, "inputMSG:TBD", outputMsg);
			    	Service serv = new Service(name, service, label, sOP);
			    	servicesManager.addService(serv);
			    	//System.out.println("			"+name+"   SIZE:"+servicesList.size() +"   adding serv  "+service);
	    	    }
	}

	/*	--------------------------------------- 		G O A L S 	M E T H O D S		--------------------------------------------	*/
	/*																																	*/
	/*	-----------------------------------------------------------------------------------------------------------------------------	*/

	
	//Get all the goals to achieve from the semantic data
	public String ExtractGoals(){ 
		String queryString = 
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
	    	        "SELECT ?goal "+
	    	        "WHERE { "+   
	    	         "?avatar avataront:hasGoal ?goal ."+ 
	    	        "}";
		   
	    	    Query query = QueryFactory.create(queryString);
	    	    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
	    	    ResultSet results =  qe.execSelect();
	    	    //ResultSetFormatter.out(System.out, results);
	    	    String name = "test";
	    	    
	    	    //For each goal
	    	    while(results.hasNext()){ 
	    	    	QuerySolution binding = results.nextSolution(); 
	    	    	name=binding.get("goal").toString();
	    	    	
	    	    	//We create an instance of goal
	    	    	Goal newGoal = new Goal(name);
	    	    	ExtractTasks(newGoal);
	    	    	goalsList.add(newGoal);
	    	    }
		return name;	
	}
		
	//Extract all the tasks contained in a goal
	public void ExtractTasks(Goal goal){ 
		//System.out.println("We extract the tasks of "+goal.getName());
		String queryString = 
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+

	    	        "SELECT ?task "+
	    	        "WHERE {<"+   
	    	         goal.getName()+">" + " avataront:hasChildTask ?task ."+
	    	        "}";
		   
	    	    Query query = QueryFactory.create(queryString);
	    	    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
	    	    ResultSet results =  qe.execSelect();
	    	    //ResultSetFormatter.out(System.out, results);
	    	    String name2 = null;
	    	    
	    	    //For each Task
	    	    while(results.hasNext()){ 
	    	    	QuerySolution binding = results.nextSolution(); 
	    	    	name2=binding.get("task").toString();	
	    	    			   	
	    	    	String interest=ExtractInterestTask(name2);
	    	    	String label=ExtractLabelTask(name2);
	    	    	
	    	    	//Check if it's a composed task
	    	    	if(IsGroupedTask(name2)){
		   				 //System.out.println(name2+" :Composed");		
			    	     Task newTask = new Task(name2,true,IsAbleTask(name2),interest,label);
		   				 ExtractGroupedTask(newTask);
				    	 goal.addTask(newTask);  
		   			 }
		    	    else{
		   				 //System.out.println(name2+" :Not Composed");	    	    	
			    	     Task newTask = new Task(name2,false,IsAbleTask(name2),interest,label);
				    	 goal.addTask(newTask);  
		    	    }    	
	    	    }
	}
	
	
	/*	--------------------------------------- 		T A S K S 	M E T H O D S		---------------------------------------------	*/
	/*																																	*/
	/*	-----------------------------------------------------------------------------------------------------------------------------	*/
	
	//Check if it is an atomic task or a composite task
	public boolean IsGroupedTask(String task){
			//System.out.println("Task to test: "+task);
			String queryString = "PREFIX DEMISA: <http://www.semanticweb.org/kkhadir/ontologies/2019/1/DEMISA#>\n" +
		    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
		    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+				
					" ASK {<"+task+"> rdf:type DEMISA:GroupedTask .}";			
		    Query query = QueryFactory.create(queryString) ;
		    QueryExecution qexec = QueryExecutionFactory.create(query, modelData) ;
		    boolean b = qexec.execAsk();
		    //ResultSetFormatter.out(System.out, b);
		    qexec.close() ;   
			return b;	
		}
	
	//Extract the atomic tasks from a group task
	public void ExtractGroupedTask(Task groupedTask){ 
		String queryString =  
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
	    	        "SELECT ?task "+
	    	        "WHERE {<"+   
	    	        groupedTask.getContent()+">" + " avataront:hasChildTask ?task ."+
	    	        "}";
		   
	    	    Query query = QueryFactory.create(queryString);
	    	    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
	    	    ResultSet results =  qe.execSelect();
	    	    //ResultSetFormatter.out(System.out, results);
 
	    	    String name2 = null;
	    		ArrayList <Task> tasksList = new ArrayList <Task>() ;	//Will contain all the sub tasks
    	    	//System.out.println("[EXTRACTGROUPEDTASK]");
	    	    
	    	    //For each Task
	    	    while(results.hasNext()){ 
	    	    	QuerySolution binding = results.nextSolution(); 
	    	    	name2=binding.get("task").toString();	
	    	        	    	
	    	    	//We create a new Task
	    	    	String interest=ExtractInterestTask(name2);
	    	    	String label=ExtractLabelTask(name2);
		    	    Task newTask = new Task(name2,false,IsAbleTask(name2),interest, label);
	    	    	tasksList.add(newTask);
	    	    }
	    	    groupedTask.majTasksList(tasksList);	
	}
	
	//Extract the interest of a task
	public String ExtractInterestTask(String task){ 
		String name2=null;
		String queryString =  
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
	    	        "SELECT ?interest "+
	    	        "WHERE {<"+ task + "> avataront:hasInterest ?interest ."+
	    	        "}";
	    	    Query query = QueryFactory.create(queryString);
	    	    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
	    	    ResultSet results =  qe.execSelect();
	    	    //ResultSetFormatter.out(System.out, results);
	    	    
	    	    while(results.hasNext()){ 
	    	    	QuerySolution binding = results.nextSolution(); 
	    	    	name2=binding.get("interest").toString();	
	    	    	//System.out.println("[EXTRACTINTERESTTASK: ]"+name+": "+task+" has the interest: "+name2);
	    	    	//We add this interest to the InterestsTasks List if it's not already in 
	    	    	if(!InteretsTasksList.contains(name2)){
	    	    		InteretsTasksList.add(name2);
	    	    	}
	    	    }
	    return name2;
	}
	
	//Extract the label of a task
	public String ExtractLabelTask(String task){ 
		String name2=null;
		String queryString = 
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
	    	        "SELECT ?label "+
	    	        "WHERE {<"+ task + "> avataront:hasLabel ?label ."+
	    	        "}";
	    	    Query query = QueryFactory.create(queryString);
	    	    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
	    	    ResultSet results =  qe.execSelect();
	    	    //ResultSetFormatter.out(System.out, results);
	    	    while(results.hasNext()){ 
	    	    	QuerySolution binding = results.nextSolution(); 
	    	    	name2=binding.get("label").toString();	
	    	    }
    	//System.out.println("[EXTRACTLABELTASK: ]"+name+": "+task+" has the label: "+name2);
	    return name2;
	}
	
	//Check if he can realize this task himself <=> If he has a task and service with the same label
	public boolean IsAbleTask(String task){
		//System.out.println("TASK FROM ISABLE: "+task);
		String queryString = 
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    		"PREFIX iserve: <http://iserve.kmi.open.ac.uk/ns/msm#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
				" ASK {<"+
					task + "> avataront:hasLabel ?label ."+
					"?service avataront:hasLabel ?label ."+
					"?service rdf:type iserve:Service ."+
					"}";

		Query query = QueryFactory.create(queryString) ;
	    QueryExecution qexec = QueryExecutionFactory.create(query, modelData) ;
	    boolean b = qexec.execAsk();
	    //ResultSetFormatter.out(System.out, b);
	    qexec.close() ;
	    return b;	
	}

	//Check if he can realize a task for a friend <=> If he has a service with a similar label than the label asked for
	public boolean IsAbleTaskFriend(String taskLabel){
		String queryString = 
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    		"PREFIX iserve: <http://iserve.kmi.open.ac.uk/ns/msm#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
				" ASK {"+
					"?service avataront:hasLabel \""+taskLabel+"\" ."+
					"?service rdf:type iserve:Service ."+
					"}";

		Query query = QueryFactory.create(queryString) ;
	    QueryExecution qexec = QueryExecutionFactory.create(query, modelData) ;
	    boolean b = qexec.execAsk();
	    //ResultSetFormatter.out(System.out, b);
	    qexec.close() ;
	    return b;	
	}
	
	//Get the service (its name, Label and QoS) with a certain label
	public String ExtractServiceFromLabel(String labelService){ 
		String name2="ExtractServiceERROR"; String name3="ExtractServiceERROR"; String name4="ExtractServiceERROR";
		String queryString =
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
	    		"PREFIX iserve: <http://iserve.kmi.open.ac.uk/ns/msm#>"+
	    	        "SELECT  ?service ?label ?qos "+
	    	        "WHERE {?service avataront:hasLabel \""+labelService+"\" ."+
	    	        "?service avataront:hasLabel ?label ."+
	    	        "?service <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#hasQoS> ?qos ."+
					"?service rdf:type iserve:Service ."+
	    	        "}";
		   
	    	    Query query = QueryFactory.create(queryString);
	    	    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
	    	    ResultSet results =  qe.execSelect();
	    	    //ResultSetFormatter.out(System.out, results);
	    	    
	    	    while(results.hasNext()){ 
	    	    	QuerySolution binding = results.nextSolution(); 
	    	    	name2=binding.get("service").toString();
	    	    	name3=binding.get("label").toString();	
	    	    	name4=binding.get("qos").toString();	
	    	    	//System.out.println("[EXTRACT SERVICE : ]"+name+": "+labelService+"  "+binding.get("qos").toString());	
	    	    }
	    return name2+"&"+name3+"&"+name4;
	}
	
	//Check if he can realize a task for a friend <=> If he has a service with a similiar label than the label asked for
	public boolean IsServiceOK(String taskLabel, String serviceLabel){
		if (taskLabel.equals(serviceLabel))
			return true;	
		else
			return false;
	}
	
	
	/*	----------------------------------- 	 	S O C I A L  M E T H O D S		-----------------------------------------------	*/
	/*																																*/
	/*	---------------------------------------------------------------------------------------------------------------------------	*/

	/*
	//Export its meta Data at the repository as a content instance, in the Data container
	public void ExportMetaData(){
		
		System.out.println("		[EXPORT META DATA OF AVATAR]: "+name);

		
		// Push a description into a content instance
		ContentInstance descriptor = new ContentInstance();
		String agentName=name.split("@")[0];
		descriptor.setName(agentName);
		descriptor.setContent("Content");
		descriptor.setContentInfo("application/obix:0");
		//Labels
		//descriptor.getLabels().add("<TEST>"+"TEST"+"</TEST>");
		descriptor.getLabels().add("<name>"+agentName+"</name>");
		descriptor.getLabels().add("<owner>"+owner+"</owner>");
		descriptor.getLabels().add("<latitude>"+latitude+"</latitude>");
		descriptor.getLabels().add("<longitude>"+longitude+"</longitude>");
		descriptor.getLabels().add("<url>"+URL+"</url>");
		//System.out.println("TEST 			<url>"+URL+"</url>");
		//Interests
		descriptor.getLabels().add("<nb_interest>"+interestsList.size()+"</nb_interest>");
		for (int i=0; i<interestsList.size();i++){
			if (interestsList.get(i).getLevel()!=0.0){
				descriptor.getLabels().add("<interest>"+interestsList.get(i).getName()+"/"+interestsList.get(i).getLevel()+"</interest>");
			}
		}
		try {
			//Browse all the Repos that he's linked to
			for (int b=0; b<repoNameList.size(); b++){
				String adress="http://localhost:8080/~/mn-cse/mn-name/"+repoNameList.get(b)+"/"+repoNameList.get(b)+"_DATA";
				String repo1="http://localhost:8080/~/mn-cse/mn-name/Repository1/Repository1_DATA/";
				client.create(adress, mapper.marshal(descriptor), ORIGINATOR, "4");
				System.out.println("		[EXPORT META DATA OF AVATAR]: "+name+" in repo: "+repoNameList.get(b)+"  "+adress);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("		ERROOOOOOOOOOOOOOOR!!![EXPORT MT OF AVATAR]: "+name);

		}
		
	}*/

	//Check all the avatars availables in the repository
	public void FriendsResearch(){
		
		//Create its metaAvatar to use it to calculate the Social Distance
		metaAvatar = new MetaAvatar(name, owner, latitude, longitude, interestsVector, interestsList, -99.0, URL);	//-99: It is a symolic value, as the Avatar don't have to calculate the SD with itself
		socialNetwork = new SocialNetwork(metaAvatar, InteretsTasksList);

		for (int s=0; s<repoNameList.size();s++){
			try {
				Response resp = client.retrieve("http://localhost:8080/~/mn-cse/mn-name/"+repoNameList.get(s)+"/"+repoNameList.get(s)+"_DATA?rcn=4", ORIGINATOR);
				//System.out.println("RESP: "+resp.getRepresentation());
				socialNetwork.SocialNetworkUpdate(resp.getRepresentation(), metaAvatar, InteretsTasksList);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("FriendsRes ERROR!");
			}
		}
		
		
	}	//TBD: RS Update !!!
	
	//Browse the tasks to deal with the tasks he can't execute
	public void BrowseTasks(ArrayList <Task> tasksList) throws IOException{
		//System.out.println("[BROWSE TASKS]"+name+": "+goalsList.get(0).getName());
		for (int s=0; s<tasksList.size();s++){
			//Able
			if(tasksList.get(s).getIsAble()){
				//System.out.println("["+name+"] "+tasksList.get(s).getContent()+": Able");
				cptTasks++;
				//System.out.println("	[CAN DO TASK ITSELF]"+name+": "+tasksList.get(s).getContent()+", total="+cptTasks);
				tasksList.get(s).setActor(name);

			}
			//Non Able ==> Check if grouped
			else {
				//Grouped ==> Recursion on this tasks list composing this grouped Task
				if(tasksList.get(s).getGrouped()){
					//System.out.println(tasksList.get(s).getContent()+": Not Able and Grouped task");
					//WARNING!!! TBD: Can't he ask someone about the entire Grouped Task, before looking at its atomic tasks components
					BrowseTasks(tasksList.get(s).getTasksList());
				}
				//Non Grouped
				else {
					//ASK
					String interest = tasksList.get(s).getInterest();
					String delegate = socialNetwork.getDelegate(interest);
					//System.out.println("["+name+"] "+tasksList.get(s).getContent()+": Not Able and NOT Grouped task and will ask "+friend+", it's an "+interest);
					//Check if he's its own delegate, if yes, he don't have to send this message
					if (delegate.equals(name)){
						//BroadCast to its SN
						broadcastSN2(tasksList.get(s).getContent()+"&"+tasksList.get(s).getLabel()+"&"+tasksList.get(s).getInterest(),"newConversation",null, this.name,name);					//Content: Task7&Label7&InterestP
					}
					//Send a message to the delegate to ask him to propagate the research of a friend of him who can do this task
					else{
						sendDelegationTask2(delegate,tasksList.get(s).getContent()+"&"+tasksList.get(s).getLabel()+"&"+tasksList.get(s).getInterest(), "newConversation", null);				//Content: Exp: Task7&Label7&InterestY
					}
				
				}

			}
		}
	}
	/*
	public void ExecuteGoal (ArrayList <Task> tasksList){
		
		 for (int s=0; s<tasksList.size(); s++){
			 
			  String task=tasksList.get(s).getContent();
			  String taskLabel=tasksList.get(s).getLabel();
			  String taskInterest=tasksList.get(s).getInterest();
			  String taskData=task+"&"+taskLabel+"&"+taskInterest;
			  String supplier=tasksList.get(s).getActor();
			  
			  //Check if it has the service
			  if (supplier.equals(name)){
				  servicesManager.ExecuteOwnService(task, taskLabel);
				  //cptTasksExec++;
			  }

			  //He can't but someone can, ask the supplier
			  else if (!supplier.equals("noSupplier")){

					  ACLMessage message = new ACLMessage(ACLMessage.QUERY_IF) ;
					  message.addReceiver(new AID(supplier,AID.ISLOCALNAME));
					  message.setContent(taskData) ;
				      Calendar rightNow = Calendar.getInstance();
				      int min = rightNow.get(Calendar.MINUTE);
					  int hour = rightNow.get(Calendar.HOUR_OF_DAY);
					  int day = rightNow.get(Calendar.DAY_OF_MONTH);
					  int month = rightNow.get(Calendar.MONTH);
					  int year = rightNow.get(Calendar.YEAR);
					  //message.setConversationId(name+"&"+task+"&"+hour+"-"+min+"-"+day+"-"+month+"-"+year);
					  message.setConversationId(name+"&"+taskData+"&"+hour+min+day+month+year);
					  
					  Date startTime = rightNow.getTime();
					  message.setReplyByDate(startTime);
					  send(message) ;
					  System.out.println("["+name+":Query Message to "+supplier+"]: "+message.getContent()+", conversation: "+message.getConversationId()) ;

			  }
			  
			  //No one can do this task, but if it's a Grouped Task ==> Recursion on this tasks list composing this grouped Task
			  else if(tasksList.get(s).getGrouped()){
					 ExecuteGoal(tasksList.get(s).getTasksList());
			  }
		  }
	}*/

	
	/*	---------------------------------- 	 	C O M M U N I C A T I O N   M E T H O D S		-----------------------------------	*/
	/*																																*/
	/*	---------------------------------------------------------------------------------------------------------------------------	*/

	//Ask a delegate to ask its SN about a task
		public void sendDelegationTask2(String agentName, String taskData, String conversation, String date ) throws IOException {
			
			String message = "<type>propagate</type>";
			message = addXmlElement(message, "content", taskData) ;
			message = addXmlElement(message,"sender",name);

			//cptMessages++;

			if (conversation.equals("newConversation")){

				//message.setConversationId(name+"&"+task+"&"+hour+"-"+min+"-"+day+"-"+month+"-"+year);
				message = addXmlElement(message, "conversationId",name+"&"+taskData);
			    
				Calendar rightNow = Calendar.getInstance();
			    Date startTime = rightNow.getTime();
			    Instant now = rightNow.toInstant();
				message = addXmlElement(message, "date", now.toString());
				
				//System.out.println("["+name+":Propagate Message to "+agentName+"]: "+message.getContent()+", conversation: "+message.getConversationId()+", nbMessages="+cptMessages) ;

			}
			
			//Delegation of Delegation
			else {
				
				message = addXmlElement(message, "conversationId",conversation);
				message = addXmlElement(message, "date",date);

				//System.out.println("["+name+":Second Propagate Message to "+agentName+"]: "+message.getContent()+", conversation: "+message.getConversationId()+", nbMessages="+cptMessages) ;
			}
			String friendURL= socialNetwork.getFriend(agentName).getURL();
			
			Response response2 = client.request(friendURL+"?type=propagate", ORIGINATOR, message);
			nbRequestsA2++;
			System.out.println("["+name+"] Send a propagate Request to "+agentName+", conv="+conversation+", total requests="+nbRequestsA2);		
			ResponsesHandler(response2);

			
		}

	//BroadCast a msg to its Social Net.
		public int broadcastSN2(String taskData, String conversation, String date, String originalSender, String delegationFrom) throws IOException {
			
			int msgs=0; 	//Nb of Avatars he sent to this msg
			
			String message = "<type>ask</type>";
			message = addXmlElement(message, "content",taskData) ;
			message = addXmlElement(message, "sender", name);
			
			//Test if it's a new conversation or not 
			if (conversation.equals("newConversation")){
				Calendar rightNow = Calendar.getInstance();
				
				message = addXmlElement(message,"conversationId", name+"&"+taskData);
				String instant = rightNow.toInstant().toString();
				message = addXmlElement(message, "date", instant);
			}
			else {
				message = addXmlElement(message,"conversationId", conversation);
    			message = addXmlElement(message, "date", date);
			}  
			 String friendName = null;
			 MetaAvatar metaAvatar = null;
			 //Iterator and the SocialNetwork list 
			 Iterator<MetaAvatar> itrFriend = socialNetwork.getSocialNetwork().iterator();
			 while (itrFriend.hasNext()) {
				 metaAvatar = itrFriend.next();
				 friendName = metaAvatar.getName();

				 //To avoid sending to the originalSender
				 if (!friendName.equals(originalSender) && !friendName.equals(delegationFrom)){
					 
					 //System.out.println("			[TEST INTEREST BC]"+name+": Has the avatar friend "+friendName+" the interest "+taskData.split("&")[2]+" for the task: "+taskData.split("&")[0]);
					 //Test if this friend has the the interest of the task
					 String taskInterest = taskData.split("&")[2];
					 if (metaAvatar.ContainsInterest(taskInterest) != -1){
						 //Add the receiver parameters
						 String friendURL = socialNetwork.getFriend(friendName).getURL();
						 
						 Response response2 = client.request(friendURL+"?type=ask", ORIGINATOR, message);
						 nbRequestsA2++;
						 System.out.println("["+name+"] Send a type Request to "+friendName+", conv="+conversation+", total requests="+nbRequestsA2);		
						 ResponsesHandler(response2);
						 
						 //System.out.println("["+name+":Request Message to "+friendName+"]: "+message.getContent()+", conversation: "+message.getConversationId()) ;
						 //cptMessagesHTTP++;
						 msgs++;

					 }
					}
			 }
			 
			 //We add this conversation to the delegatesConversations								
			 if(conversation.equals("newConversation")){
				 delegationsManager.AddDelegation(new Delegation(name,getXmlElement(message,"conversationId") , msgs));
			 }

			 return msgs;
		}
	

	
	/*	----------------------------------- 	 	R E S S O U R C E S  M E T H O D S		--------------------------------------------------	*/
	/*																																*/
	/*	---------------------------------------------------------------------------------------------------------------------------	*/

	

	/*	----------------------------------- 	 	S H O W  M E T H O D S		--------------------------------------------------	*/
	/*																																*/
	/*	---------------------------------------------------------------------------------------------------------------------------	*/

	
	//Show the complete ontology (its model)
	public void showModel(){
		//System.out.println(modelData);		
		String[] rdfTab = modelData.toString().split(" "); 
		for (int s=0; s<rdfTab.length;s++){
			System.out.println(rdfTab[s]);
		}
	}
	
	//Show Interests List
	public void showInterestsList(){
		System.out.println("Interests List: ");	
		for (int s=0; s<interestsList.size();s++){
			System.out.println("Name: "+interestsList.get(s).getName()+", Level: "+String.valueOf(interestsList.get(s).getLevel()));
		}
	}
	
	//Show the services
	public String ShowServices(){ 
		String name2=null;
		String queryString =  
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
	    		"PREFIX iserve: <http://iserve.kmi.open.ac.uk/ns/msm#>"+

	    	        "SELECT  ?qos "+
	    	        "WHERE {"+
	    	        "?service avataront:hasLabel ?label ."+
					//"?service <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#hasQoS>  ?qos ."+
					"?service rdf:type iserve:Service ."+
	    	        "}";
		   
	    	    Query query = QueryFactory.create(queryString);
	    	    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
	    	    ResultSet results =  qe.execSelect();
	    	    ResultSetFormatter.out(System.out, results);
	    	    
	    	    while(results.hasNext()){ 
	    	    	QuerySolution binding = results.nextSolution(); 
	    	    	name2=binding.get("qos").toString();	
	    	    }
    	//System.out.println("[EXTRACTLABELTASK: ]"+name+": "+task+" has the label: "+name2);
	    return name2;
	}
	
	
/*
	
	public void HTTPGET() throws Exception {
		  StringBuilder result = new StringBuilder();
		  URL url = new URL("http://localhost:9797/~/mn-cse/mn-name/Repository/op=Service3");
		  HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		  conn.setRequestMethod("GET");
		  BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		  String line;
		  while ((line = rd.readLine()) != null) {
		     result.append(line);
		  }
		  rd.close();
		  System.out.println("AVATAR: HTTP REQUEST SENT, resp= "+result.toString());

	} 
	
	// HTTP POST request
		private void sendGET() throws Exception {

			String url = "https://selfsolve.apple.com/wcResults.do";
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			//add reuqest header
			con.setRequestMethod("GET");
			//con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
			
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + urlParameters);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			//print result
			System.out.println("				SEND GET: "+response.toString());

		}*/
		
		//////////////////////////////////////////////////////////////////////////////////////////////
		
		public String GETService(String service){
			return servicesManager.ServiceExecution2(service);
			//return "getservice";
		}
		
		//Ask Request (about a task, outside the case of a process, so we only receive the Task content, and not the date, conv, etc.) 
		public String GETAskRequest(String taskName, String taskLabel){
			String res = "";

			//isAble?
			//TBD: URGENT !! USE THE SERVICES MANAGER AS IT CONTAINS SERVICES OF FRIENDS TOO
			if(IsAbleTaskFriend(taskLabel)){
				//PROPOSE
				res = addXmlElement(res,"type","propose");
				res = addXmlElement(res,"sender",name);
				//TBD: WARNING!!! We have to see data about the Qos and all the info. about the service
				res = addXmlElement(res,"content",ExtractServiceFromLabel(taskLabel)+"&"+name) ;	//ServiceX & LabelX & QosX & name
				//System.out.println("["+name+":Proposal Message to "+sender+"]: "+message.getContent()+", conversation: "+message.getConversationId()+", nbMessages="+cptMessages) ;
			}
			else{
				//Answer that he can't 
				res = addXmlElement(res,"type","failure");
				res = addXmlElement(res,"sender",name);
				res = addXmlElement(res,"content","No Service available for this task") ;
				//cptMessagesHTTP++;
				//System.out.println("["+name+": Failure Message to "+sender+"]: "+message.getContent()+", conversation: "+message.getConversationId()+"   "+message.getPerformative()+", nbMessages="+cptMessages) ;
			}
    		return res;
		}
		
		
		//Ask Request (about a task) 
		public String AskRequest(String request){
        	String res = "";

        	String sender = getXmlElement(request,"sender");
    		String content = getXmlElement(request,"content");
    		String conversationId = getXmlElement(request,"conversationId");
    		
    		String task = content.split("&")[0];
			String taskLabel = content.split("&")[1];
			String date = getXmlElement(request,"date");
    		
			//isAble?
			//TBD: URGENT !! USE THE SERVICES MANAGER AS IT CONTAINS SERVICES OF FRIENDS TOO
			if(IsAbleTaskFriend(taskLabel)){
				//PROPOSE
				res = addXmlElement(res,"type","propose");
				res = addXmlElement(res,"sender",name);
				res = addXmlElement(res,"conversationId",conversationId);
				//TBD: WARNING!!! We have to see data about the Qos and all the info. about the service
				res = addXmlElement(res,"content",ExtractServiceFromLabel(taskLabel)+"&"+name) ;	//ServiceX & LabelX & QosX & name
				res = addXmlElement(res,"date",date);
				//cptMessagesHTTP++;
				//System.out.println("["+name+":Proposal Message to "+sender+"]: "+message.getContent()+", conversation: "+message.getConversationId()+", nbMessages="+cptMessages) ;
			}
			else{
				//Answer that he can't 
				res = addXmlElement(res,"type","failure");
				res = addXmlElement(res,"sender",name);
				res = addXmlElement(res,"content","No Service available for this task") ;

				res = addXmlElement(res,"conversationId",conversationId);
				//TBD: WARNING!!! We have to see data about the Qos and all the info. about the service
				//addXmlElement(res,"content",ExtractServiceFromLabel(taskLabel)+"&"+name) ;	//ServiceX & LabelX & QosX & name
				res = addXmlElement(res,"date",date);
				//cptMessagesHTTP++;
				//System.out.println("["+name+": Failure Message to "+sender+"]: "+message.getContent()+", conversation: "+message.getConversationId()+"   "+message.getPerformative()+", nbMessages="+cptMessages) ;
			}
    		return res;
		}
		
		//Accept Request
		public String AcceptRequest(String request){
    		String res="<type>confirm</type><sender>"+name+"</sender><content>ok</content>";
    		//TBD ADD THE SERVICE FROM AVATARX
    		return res;
		}
		
		//Failure Request (A delegate failed)
		public String FailureRequest(String request) throws IOException{
			
			String sender = getXmlElement(request,"sender");
    		String content = getXmlElement(request,"content");
    		String conversationId = getXmlElement(request,"conversationId");
			String date2 = getXmlElement(request,"date");
			String res="failureDelegation";
			
    		//String date = getXmlElement(request,"date");
    		//System.out.println("["+name+": <-- Failure Reception from "+sender+"]: "+msg.getContent()+", conversation: "+msg.getConversationId()) ;
			
			//Test if he's the delegate of this research
			//if (delegateTasks.contains(msg.getConversationId())){
			String msgToSend = delegationsManager.ManageFailureRequest2(request, socialNetwork, metaAvatar);

			//He was the delegate of this conversation
			if (msgToSend != null){
				//It's a failure msg to the delegationSender because there is no time
				if (msgToSend.contains("<type>failure</type>")){
					String friend = getXmlElement(msgToSend,"receiver");
					String friendURL = socialNetwork.getFriend(friend).getURL();
					Response response2 = client.request(friendURL+"?type=failure", ORIGINATOR, msgToSend);
					nbRequestsA2++;
					System.out.println("["+name+"] Send a Failure Request to "+friend+", conv="+conversationId+", total requests="+nbRequestsA2);		
					//ResponsesHandler(response2);
				}	
				//There is still more time 
				else if(msgToSend.contains("second")){
					String secondDelegate= getXmlElement(msgToSend,"secondDelegate");
					String delegationFrom= getXmlElement(msgToSend,"delegationFrom");

					//Test if there is someone able for a 2nd delegation
					if (!secondDelegate.equals("noOne")){
														
						String taskData=conversationId.split("&")[1]+"&"+conversationId.split("&")[2]+"&"+conversationId.split("&")[3]; //Extract the Task Data From the conversationID, Conv.ID = avatarOriginal & taskData
						sendDelegationTask2(secondDelegate, taskData , conversationId, date2);
						//System.out.println("							NEW DELEG  "+name+":    2nd delegate = "+secondDelegate+" task  "+taskData+"  conv  "+msg.getConversationId());
					}
					//Inform the Avatar that delegate him that it's a failure because he can't 
					else{
						String res2 = "<type>failure</type>";
						res2 = addXmlElement(res2,"conversationId",conversationId);
						//res = addXmlElement(res,"date",date);
						res2 = addXmlElement(res2,"sender",name);
						res2 = addXmlElement(res2,"content","No more time");
						res2 = addXmlElement(res2,"receiver",delegationFrom);
						
						String friendURL = socialNetwork.getFriend(delegationFrom).getURL();
						Response response2 = client.request(friendURL+"?type=failure", ORIGINATOR, res2);
						nbRequestsA2++;
						System.out.println("["+name+"] Send a Failure Request to "+delegationFrom+", conv="+conversationId+", total requests="+nbRequestsA2);		

					}
				}
			}
			return res;
		}
	
		
		public String FailureTimeOutRequest(String request){
    		String conversationId = getXmlElement(request,"conversationId");
			String res="<type>okfailureTO</type><content>okFTO</content><sender>"+name+"</sender><conversationId>"+conversationId+"</conversationId><date>date</date>";
			return res;
		}
		
		public String PropagateRequest(String request) throws IOException{
			String res="";
			
        	String sender = getXmlElement(request,"sender");
    		String content = getXmlElement(request,"content");
    		String conversationId = getXmlElement(request,"conversationId");
			
			String taskP = content.split("&")[0];
			String taskLabelP = content.split("&")[1];
			String taskInterestP = content.split("&")[2];
			String dateP = getXmlElement(request,"date");
			
			//isAble?
			//TBD: Use Services Manager to check the services of our friends
			if(IsAbleTaskFriend(taskLabelP)){
				
				//PROPOSE
				res = addXmlElement(res,"type","propose");
				res = addXmlElement(res,"sender",name);
				res = addXmlElement(res,"conversationId",conversationId);
				//TBD: We have to see data about the Qos and all the info. about the service
				res = addXmlElement(res,"content",ExtractServiceFromLabel(taskLabelP)+"&"+name) ;	//ServiceX & LabelX & QosX & name
				res = addXmlElement(res,"date",dateP);
			}
			else{
				
				//BroadCast to its SN and memorize the nb of people he requested
				int nbRequests=broadcastSN2(taskP+"&"+taskLabelP+"&"+taskInterestP,conversationId,dateP,conversationId.split("&")[0], sender);
				delegationsManager.AddDelegation(new Delegation(sender, conversationId, nbRequests));
				res="<type>okPropagation</type><content>okPropagation</content><sender>"+name+"</sender><conversationId>"+conversationId+"</conversationId><date>"+dateP+"</date>";
				
				//Timeout of 20 sec
				//TBD URGENT IF 2nd PROPAGATION ==> The 2nd delegate don't have 20 sec as the first delegate, it depends
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					  @Override
					  public void run() {
						//See if the task was treated 20sec after the launching of the Timeout
						String msgToSend=delegationsManager.ManageTimeOut2(request);
						if (msgToSend!= null ){
							
							String friend = getXmlElement(msgToSend,"receiver");
							String friendURL = socialNetwork.getFriend(friend).getURL();
							try {
								//System.out.println("		TEST URL TIMEOUT of "+name+"		"+friendURL+"?type=failuretimeout   "+msgToSend);
								Response response2 = client.request(friendURL+"?type=failuretimeout", ORIGINATOR, msgToSend);
								nbRequestsA2++;
								System.out.println("["+name+"] Send a Failure TimeOut Request to "+friend+", conv="+conversationId+", total requests="+nbRequestsA2);		
								ResponsesHandler(response2);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					  }
					}, 20*1000);									
			}
			return res;
		}
		
		
		//HTTP Responses Handler
		public void ResponsesHandler(Response resp){
			if (resp.getStatusCode() != 201 || resp.getRepresentation().isEmpty()) {
				String res="";
				//System.out.println("AVATAR: HTTP RESPONSE to "+name+":"+ resp.getRepresentation());		
				String response=resp.getRepresentation();
				try{
					String type=response.split("<type>")[1].split("</type>")[0];
					//cptMessagesHTTP++;
					System.out.println("[HTTP RESPONSES of "+name+"]: "+ resp.getRepresentation()+"  "+type+", nbHTTPMsg: "+cptMessagesHTTP);		
					
					String content = getXmlElement(response,"content");
					String sender = getXmlElement(response,"sender");
					
					switch (type){
					
					//Propose Message
					case "propose":
						String conversationId = getXmlElement(response,"conversationId");
						String date = getXmlElement(response,"date");
						String serviceLabel=content.split("&")[1];
						String taskLabel=conversationId.split("&")[2];
						
						//System.out.println("["+name+": <-- Proposal Reception from "+sender+"]: "+msg.getContent()+", conversation: "+msg.getConversationId()) ;

						if (IsServiceOK(taskLabel,serviceLabel)){
							//Send an Accept Request 
							res = addXmlElement(res,"type","accept");
							res = addXmlElement(res,"conversationId",conversationId);
							res = addXmlElement(res,"date",date);
							res = addXmlElement(res,"sender",name);
							res = addXmlElement(res,"content","ok");
							
							//Send the request
							String friendURL =  socialNetwork.getFriend(sender).getURL();
							Response resp2 = client.request(friendURL+"?type=accept", ORIGINATOR, res);
							nbRequestsA2++;
							System.out.println("["+name+"] Send an Accept Request to "+sender+", conv="+conversationId+", total requests="+nbRequestsA2);		
							ResponsesHandler(resp2);
							//cptMessagesHTTP++;
							//System.out.println("["+name+":Accept Message to "+sender+"]: "+message.getContent()+", conversation: "+message.getConversationId()+", nbMessages="+cptMessages) ;
							
						}
						break;
						
					//Confirm Message
					case "confirm" : 
						
						break;
					
					//Failure Message
					case "failure":
						
						String conversationIdF = getXmlElement(response,"conversationId");
						String dateF = getXmlElement(response,"date");
						
						//System.out.println("["+name+": <-- Failure Reception from "+sender+"]: "+msg.getContent()+", conversation: "+msg.getConversationId()) ;
						
						//Test if he's the delegate of this research
						//if (delegateTasks.contains(msg.getConversationId())){
						String msgToSend = delegationsManager.ManageFailureRequest2(response, socialNetwork, metaAvatar);

						//He was the delegate of this conversation
						if (msgToSend != null){
							//It's a failure msg to the delegationSender because there is no time
							if (msgToSend.contains("<type>failure</type>")){
								String friend = getXmlElement(msgToSend,"receiver");
								String friendURL = socialNetwork.getFriend(friend).getURL();
								Response response2 = client.request(friendURL+"?type=failure", ORIGINATOR, msgToSend);
								nbRequestsA2++;
								System.out.println("["+name+"] Send a Failure Request to "+friend+", conv="+conversationIdF+", total requests="+nbRequestsA2);		

								//ResponsesHandler(response2);
							}	
							//There is still more time 
							else if(msgToSend.contains("second")){
								String secondDelegate=msgToSend.split("=")[1];
								//Test if there is someone able for a 2nd delegation
								if (!secondDelegate.equals("noOne")){
																	
									String taskData=conversationIdF.split("&")[1]+"&"+conversationIdF.split("&")[2]+"&"+conversationIdF.split("&")[3]; //Extract the Task Data From the conversationID, Conv.ID = avatarOriginal & taskData
									sendDelegationTask2(secondDelegate, taskData , conversationIdF, dateF);
									//System.out.println("							NEW DELEG  "+name+":    2nd delegate = "+secondDelegate+" task  "+taskData+"  conv  "+msg.getConversationId());

								}
								else{
									//TBD URGENT !!!! SEND A FAILURE MSG TO THE DELEGATIONFROM AVATAR
								}
							}
						}
						
						break;
					
					case "failuretimeout":
						String conversationIdTO = getXmlElement(response,"conversationId");
						res="<type>okfailureTO</type><content>okFTO</content><sender>"+name+"</sender><conversationId>"+conversationIdTO+"</conversationId><date>date</date>";
						break;
						
					//Inform Message
					case "inform":
						System.out.println("		INFORM "+response);
						break;
					
					default:
						break;
					}
					
					
					
				
				}catch (Exception e) {e.printStackTrace();}
				
			}
			else{
				System.out.println("AVATAR: HTTP REQUEST ERROR");
			}
		}
		
		//Mapping the params in the URIQuery
		public Map<String, String> queryToMap(String query) {
		    Map<String, String> result = new HashMap<>();
		    for (String param : query.split("&")) {
		        String[] entry = param.split("=");
		        if (entry.length > 1) {
		            result.put(entry[0], entry[1]);
		        }else{
		            result.put(entry[0], "");
		        }
		    }
		    return result;
		}
		//Map<String, String> params = queryToMap(httpExchange.getRequestURI().getQuery()); 
		
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
