
  package com.springboot.encrypt.password;
  
  import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.google.gson.Gson;
import com.springboot.jpa.h2.User;
  
  public class BCryptPasswordEncoderTest {
  
		public static void main(String[] args) {
												  // TODO Auto-generated method stub 
			BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
												  
												  for(int i=0;i<=10;i++) { String encodedstring=
												  encoder.encode("admin"); System.out.println(encodedstring); }
												 
			/* String jsonString = "{message:user is not found}"; */
			
			String name="hi";
			System.out.println("before"+name);
			
			if(name=="hi") {
				name="hello";
			}
			
			System.out.println("after"+name);
			 
			

			/*
			 * Gson g = new Gson(); Object p = g.fr(jsonString, Object.class);
			 * System.out.println(p);
			 */
  
  }
  
  }
 