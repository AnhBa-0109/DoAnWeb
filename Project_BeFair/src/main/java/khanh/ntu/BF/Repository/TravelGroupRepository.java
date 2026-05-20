package khanh.ntu.BF.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import khanh.ntu.BF.models.TravelGroup;

public interface TravelGroupRepository extends JpaRepository<TravelGroup, Long>{
	
	@Query("SELECT DISTINCT tg FROM TravelGroup tg " +
	           "LEFT JOIN tg.members m " +
	           "WHERE tg.owner.id = :userId OR m.user.id = :userId")
	    List<TravelGroup> findByOwnerOrMember(@Param("userId") Long userId);
}
