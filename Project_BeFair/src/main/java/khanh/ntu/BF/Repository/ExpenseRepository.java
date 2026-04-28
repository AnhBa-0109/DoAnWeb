package khanh.ntu.BF.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import khanh.ntu.BF.models.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Integer>{
	List<Expense> findByGroupId(long groupId);
}
