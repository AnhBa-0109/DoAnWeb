package khanh.ntu.BF.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private Double amount;
    
    @Column(name = "create_at")
    private LocalDateTime createAt = LocalDateTime.now();
    
    private String invoiceImage; 
    
    @ManyToOne
    @JoinColumn(name = "payer_id")
    private Member payer;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private TravelGroup group;
    
    @Column(name = "sharer_ids")
    private String sharerIds = "";
    
	public Expense() {
		super();
	}

	
	public Long getId() {
		return id;
	}



	public void setId(Long id) {
		this.id = id;
	}



	public String getDescription() {
		return description;
	}



	public void setDescription(String description) {
		this.description = description;
	}



	public Double getAmount() {
		return amount;
	}



	public void setAmount(Double amount) {
		this.amount = amount;
	}



	public Member getPayer() {
		return payer;
	}



	public void setPayer(Member payer) {
		this.payer = payer;
	}



	public TravelGroup getGroup() {
		return group;
	}



	public void setGroup(TravelGroup group) {
		this.group = group;
	}



	public String getInvoiceImage() {
		return invoiceImage;
	}

	public void setInvoiceImage(String invoiceImage) {
		this.invoiceImage = invoiceImage;
	}


	public LocalDateTime getCreateAt() {
		return createAt;
	}


	public void setCreateAt(LocalDateTime createAt) {
		this.createAt = createAt;
	}


	public String getSharerIds() {
		return sharerIds;
	}


	public void setSharerIds(String sharerIds) {
		this.sharerIds = sharerIds;
	}
	
	
}
