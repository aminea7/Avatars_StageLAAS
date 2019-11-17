package fr.insa.laas.Ontology;

public class Interest {

	//Attributs
	private String interest;
	private double interestLevel;
	
	//Constructor
	public Interest(String i, double iL){
		interest=i;
		interestLevel=iL;
	}
	
	//Getters & Setters
	public String getName(){
		return interest;
	}
	public double getLevel(){
		return interestLevel;
	}
}
