package fr.insa.laas.Ontology;

public class ServiceOperation {

	//Attributs
	private String name;
	private String methode;	//GET or SET
	private String inputMessage;
	private String outputMessage;
	
	//Constructor
	public ServiceOperation(String n, String m, String im, String om){
		name=n;
		methode=m;
		inputMessage=im;
		outputMessage=om;
	}
	
	//Getters & Setters
	public String getMethode(){
		return methode;
	}
	public String getOutputMessage(){
		return outputMessage;
	}
	public String getInputMessage(){
		return inputMessage;
	}
}
