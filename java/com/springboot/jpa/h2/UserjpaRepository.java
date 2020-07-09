package com.springboot.jpa.h2;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

@Repository
@CrossOrigin(origins="http://localhost:4200")
public interface UserjpaRepository extends JpaRepository<User, Long> {
	List<User> findByUsername(String username);
	
}
