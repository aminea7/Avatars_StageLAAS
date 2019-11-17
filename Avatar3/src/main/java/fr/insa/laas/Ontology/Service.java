package fr.insa.laas.Ontology;

import java.util.ArrayList;

public class Service {

	//Attributs
	private String name;
	private String label;
	private String supplier;
	private ServiceOperation serviceOp;
	//private ArrayList <ServiceOperation> serviceOpList= new ArrayList <ServiceOperation>();	//TBD
	//ServiceQos serviceQos;	//TBD
	//TBD: List of operations of the service instead of one ServiceOperation + The QoS of the Service
	
	/*
	//Constructor
	public Service (String s, String n, String l){
		supplier=s;
		name=n;
		label=l;
	}*/
	
	//Constructor
	public Service (String s, String n, String l, ServiceOperation  sol){
		supplier=s;
		name=n;
		label=l;
		serviceOp=sol;
	}
	
	//Getters & Setters
	public String getName(){
		return name;
	}
	public String getSupplier(){
		return supplier;
	}
	public String getLabel(){
		return label;
	}
	public ServiceOperation getServiceOp(){
		return serviceOp;
	}
}
