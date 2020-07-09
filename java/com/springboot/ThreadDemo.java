package com.springboot;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.StringTokenizer;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import exceptions.ElasticException;
import exceptions.HttpException;
import exceptions.LoaderException;
import loader.KeysLoader;
import loader.Loader;
import loader.LoaderFactory;
import util.PropertyConfiguration;

@Component
public class ThreadDemo {

	
	public void invokeThread(String name) throws ElasticException, HttpException, IOException {
		//Thread.sleep(3000); // Let me sleep for 3 sec
		LocalDateTime startTime = LocalDateTime.now();
		//log.info("Data Collection Start Time: " + startTime);

		Properties prop = PropertyConfiguration.getProperties();
		prop.setProperty("ADMDJOBS", name);
		System.out.println(prop.getProperty("ADMDJOBS"));
		StringTokenizer st = new StringTokenizer(prop.getProperty("ADMDJOBS"), ",");
		System.out.println(st);
		String job = null;
		Loader loader = null;
		new KeysLoader();
		
		
		try {
			while (st.hasMoreTokens()) {
				job = st.nextToken().trim();
				
				loader = LoaderFactory.getLoader(job);
				if(loader!=null) {
			//		log.info("Data Collection Started for job: " + job);
					loader.load();
				//	log.info("Data Collection Ended for job: " + job);
				}else {
					//log.info("Loader not configured for job: " + job);
				}
			}
		}catch(LoaderException ex) {
		//	log.error("Loader exception occured for the job"+job, ex);
		}

		LocalDateTime endTime = LocalDateTime.now();
		//log.info("Data Collection End Time: " + endTime);

		System.out.println("My Name"+"" +name+"" + Thread.currentThread().getName());
// Let me sleep for 3 sec

		//System.out.println("My Name"+"" +name+"" + Thread.currentThread().getName());
	}

}
