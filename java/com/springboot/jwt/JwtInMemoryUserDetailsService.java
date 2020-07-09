package com.springboot.jwt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JwtInMemoryUserDetailsService implements UserDetailsService {

  static List<JwtUserDetails> inMemoryUserList = new ArrayList<>();

  static {
    inMemoryUserList.add(new JwtUserDetails(1L, "admin",
        "$2a$10$Hy54296U3tWDSqY3gXqUsO7GSHkIYsGp7/fWUtF8MPk.7z1td5tOy", "ROLE_USER_1"));
    
	
	  inMemoryUserList.add(new JwtUserDetails(2L, "admin1",
	  "$2a$10$3UL4IKxgkuZyVdoOrm4D0.nLj16NraDEwyN0DGdXhbGXx4boTiNDO",
	  "ROLE_USER_2"));
	  
	  inMemoryUserList.add(new JwtUserDetails(3L, "admin2",
			  "$2a$10$QGLNg2OynBKW684/yZZei.whOOWmPKeqdh5gYWCqWb5UyttD8G5JS",
			  "ROLE_USER_3"));
	  
	  inMemoryUserList.add(new JwtUserDetails(4L, "admin3",
			  "$2a$10$iXwXM.r/njYTxuYUmrpKSO2sEK9U3GlJAfADZgzPH7xXlOTeALFlu",
			  "ROLE_USER_4"));
	  
	  inMemoryUserList.add(new JwtUserDetails(5L, "admin4",
			  "$2a$10$OCwXMivrgOAbMVhLnEp7POYefPWUyCvWbdG4ffqYBzdDBeLeIOQoy",
			  "ROLE_USER_5"));
	  
	 
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Optional<JwtUserDetails> findFirst = inMemoryUserList.stream()
        .filter(user -> user.getUsername().equals(username)).findFirst();

    if (!findFirst.isPresent()) {
      throw new UsernameNotFoundException(String.format("USER_NOT_FOUND '%s'.", username));
    }

    return findFirst.get();
  }

}
