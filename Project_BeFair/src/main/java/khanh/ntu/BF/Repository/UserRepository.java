package khanh.ntu.BF.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import khanh.ntu.BF.models.User;

public interface UserRepository extends JpaRepository<User, Long>{
	User findByUsername(String username);
}
