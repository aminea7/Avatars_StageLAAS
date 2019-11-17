package fr.insa.laas.Ontology;

import java.util.ArrayList;

public class Goal {

	//Attributs
	private String name;
	private boolean achieved = false;	
	private ArrayList <Task> tasksList = new ArrayList <Task>();		

	//Constructor
	public Goal(String name){
		this.name=name;
	}

	public void addTask(Task newTask) {
		tasksList.add(newTask);
	}
	
	//Getters & Setters
	public String getName(){
		return name;
	}
	
	public ArrayList <Task> getTasksList(){
		return tasksList;
	}
	
	public boolean setActorTask(String avatar, String task,ArrayList <Task> ListTasks ){
		boolean res=false;
		//Browse in the tasks List
		for (int s=0; s<ListTasks.size();s++){

			if (ListTasks.get(s).getContent().equals(task)){
				ListTasks.get(s).setActor(avatar);
				res=true;
			}
			//If it's a grouped task
			else if (ListTasks.get(s).getGrouped() == true){
				//Browse in its sub tasks
				setActorTask(avatar, task, ListTasks.get(s).getTasksList());
			}
		}
		return res;
	}
	
	//Test if every task or subTask of the Goal has an actor
	public boolean ReadyForExecution(){
		boolean res=true;
		for (int f=0; f<tasksList.size();f++){
			if (tasksList.get(f).getActor().equals("noSupplier")){
				
				if (tasksList.get(f).getGrouped()){
					for (int t=0; t<tasksList.get(f).getTasksList().size(); t++){
						if (tasksList.get(f).getTasksList().get(t).getActor().equals("noSupplier")){
							res=false;
							break;
						}
					}
				}
				else {
					res=false;
					break;
				}
			}
		}
		return res;
	}
	
	//Show the goal
	public void showGoal(){
		System.out.println("Goal name: "+name);
		for (int s=0; s<tasksList.size();s++){
			//Atomic Task
			if(tasksList.get(s).getGrouped() == false){
				System.out.println(tasksList.get(s).getContent()+", isAble: "+tasksList.get(s).getIsAble()+", can be done by: "+tasksList.get(s).getActor());
			}
			//Grouped Task
			else {
				System.out.println("GroupedTask "+tasksList.get(s).getContent()+", isAble: "+tasksList.get(s).getIsAble()+", can be done by: "+tasksList.get(s).getActor()+" : ");
				for (int t=0; t<tasksList.get(s).getTasksList().size();t++){
					System.out.println(tasksList.get(s).getTasksList().get(t).getContent()+", isAble: "+tasksList.get(s).getTasksList().get(t).getIsAble()+", can be done by: "+tasksList.get(s).getTasksList().get(t).getActor());
				}
			}
		}
	}

}
