package com.springboot.jpa.h2;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;



@CrossOrigin(origins="http://localhost:4200")
@RestController
public class UserjpaResources {

	@Autowired
	private UserjpaRepository userJpaRepository;

	/*
	 * @Autowired private UserService userService;
	 */
	@CrossOrigin(origins="http://localhost:4200")
	@GetMapping("/jpa/users/{username}/users")
	public List<User> getAllUsers(@PathVariable String username) {
		
		return userJpaRepository.findByUsername(username);
		

	}
	@CrossOrigin(origins="http://localhost:4200")
	@GetMapping("/jpa/users/{username}/users/data")
	public User getUserdata(@PathVariable String username) {
		List<User> userlist=userJpaRepository.findByUsername(username);
		if (userlist != null && !userlist.isEmpty()) {
			return userJpaRepository.findByUsername(username).get(0);
		}else {
			return new User(String.format(username +"  Not found"));
		}
		

	}

	@GetMapping("/jpa/users/{username}/users/{id}")
	public User getUser(@PathVariable String username, @PathVariable long id) {
		return userJpaRepository.findById(id).get();

	}
	// DELETE /users/{username}/users/{id}
	@DeleteMapping("/jpa/users/{username}/users/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable String username, @PathVariable long id) {

		userJpaRepository.deleteById(id);

		return ResponseEntity.noContent().build();
	}

	// Edit/Update a User
	// PUT /users/{user_name}/Users/{todo_id}
	@CrossOrigin(origins = "http://localhost:4200")
	@PutMapping("/jpa/users/{username}/users/{id}")
	public ResponseEntity<User> updateUser(@PathVariable String username, @PathVariable long id,
			@RequestBody User user) {

		user.setUsername(username);

		User userUpdated = userJpaRepository.save(user);
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}
	@CrossOrigin(origins = "http://localhost:4200")
	@PostMapping("/jpa/users/{username}/users")
	public ResponseEntity<Void> createUser(@PathVariable String username, @RequestBody User user) {

		user.setUsername(username);

		User createdUser = userJpaRepository.save(user);

		// Location
		// Get current resource url
		/// {id}
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(createdUser.getId())
				.toUri();

		return ResponseEntity.created(uri).build();
	}
}
