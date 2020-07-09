package com.springboot.jpa.h2;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class UserService {

	private static List<User> users = new ArrayList<>();
	private static long idCounter = 0;

	static {
		users.add(new User(++idCounter, "admin", "Jenkins", false));
		users.add(new User(++idCounter, "admin1", "Sonar", false));
		users.add(new User(++idCounter, "admin2", "Version1", false));
	}

	public List<User> findAll() {
		return users;
	}
	
	public User save(User User) {
		if(User.getId()==-1 || User.getId()==0) {
			User.setId(++idCounter);
			users.add(User);
		} else {
			deleteById(User.getId());
			users.add(User);
		}
		return User;
	}

	public User deleteById(long id) {
		User User = findById(id);

		if (User == null)
			return null;

		if (users.remove(User)) {
			return User;
		}

		return null;
	}

	public User findById(long id) {
		for (User User : users) {
			if (User.getId() == id) {
				return User;
			}
		}

		return null;
	}
}