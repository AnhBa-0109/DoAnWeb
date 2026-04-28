package khanh.ntu.BF.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import khanh.ntu.BF.models.Member;


public interface MemberRepository extends JpaRepository<Member, Integer>{
	List<Member> findByGroupId(long groupId);
}
