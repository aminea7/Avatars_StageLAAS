package fr.insa.laas.Avatar1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Avatar1Application {
	
	public static void main(String[] args) {
		//System.out.println("Print before");

		SpringApplication.run(Avatar1Application.class, args);		//Launch the Controller too
		//System.out.println("Print after");

	}

}
