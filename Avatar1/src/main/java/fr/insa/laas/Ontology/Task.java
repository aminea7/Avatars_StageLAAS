package fr.insa.laas.Ontology;


import java.util.ArrayList;

public class Task {

	//Attributs
	private String content; 			//Contains the triplet of the task
	private boolean achieved = false;
	private boolean isAble;				//To do it himself
	private String label;
	private String actor = "noSupplier" ;
	private boolean grouped;	//Grouped Task
	private String interest;
	private ArrayList <Task> tasksList = new ArrayList <Task>() ;	//If it's a groupedTask
	
	public Task(String content, boolean gr, boolean isAble, String i, String lab){
		this.content=content;
		this.grouped=gr;
		this.isAble=isAble;
		interest=i;
		this.label=lab;
	}
	
	public void setActor(String avatar){
		actor=avatar;
	}
	public void setAchivied(){
		achieved=true;
	}
	
	public void majTasksList(ArrayList <Task> tasksList){
		this.tasksList=tasksList;
	}
	
	public ArrayList <Task> getTasksList(){
		return tasksList;
	}
	
	
	public String getContent(){
		return content;
	}
	public String getInterest(){
		return interest;
	}
	
	public String getLabel(){
		return label;
	}
	public String getActor(){
		return actor;
	}
	
	public boolean getAchieved(){
		return achieved;
	}
	public boolean getGrouped(){
		return grouped;
	}
	public boolean getIsAble(){
		return isAble;
	}

}
