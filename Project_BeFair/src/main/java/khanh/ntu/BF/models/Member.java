package khanh.ntu.BF.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "members")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    private boolean active = true;
    
    @Column(name = "join_at")
    private LocalDateTime joinAt = LocalDateTime.now();
    
    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private TravelGroup group;

	public Member() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TravelGroup getGroup() {
		return group;
	}

	public void setGroup(TravelGroup group) {
		this.group = group;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public LocalDateTime getJoinAt() {
		return joinAt;
	}

	public void setJoinAt(LocalDateTime joinAt) {
		this.joinAt = joinAt;
	}

	public LocalDateTime getLeftAt() {
		return leftAt;
	}

	public void setLeftAt(LocalDateTime leftAt) {
		this.leftAt = leftAt;
	}

    
}
