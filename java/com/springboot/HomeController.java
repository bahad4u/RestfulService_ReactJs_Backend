package com.springboot;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import exceptions.ElasticException;
import exceptions.HttpException;

@RestController
@CrossOrigin(origins="http://localhost:4200")
public class HomeController {
	
	@Autowired
	  private ThreadDemo asyncDemo;

	@GetMapping(path = "/hello-world/path-variable/{name}")
    public HelloWorldBean helloWorldPathVariable (@PathVariable String name) throws ElasticException, HttpException, IOException {
		asyncDemo.invokeThread(name);
		if(name.equalsIgnoreCase("Jenkins")) {
			name="DevOps";
			}else if(name.equalsIgnoreCase("SONAR")) {
				name="Code Quality";
			}else if(name.equalsIgnoreCase("V1_VELOCITY_METRICS")) {
				name ="Agile";
			}
		
        return new HelloWorldBean(String.format(name +"  Job loaded Successfully"));
    }
}
