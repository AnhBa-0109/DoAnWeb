package khanh.ntu.BF.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import khanh.ntu.BF.models.TravelGroup;

public interface TravelGroupRepository extends JpaRepository<TravelGroup, Long>{
	List<TravelGroup> findByOwnerUsername(String username);
}
