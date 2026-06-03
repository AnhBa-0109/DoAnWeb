package khanh.ntu.BF.models;

import java.time.LocalDateTime;

public class ExpenseDTO {
	    private Long id;
	    private String description;
	    private Double amount;
	    private Long payerId;
	    private String payerName;
	    private String sharerIds;
	    private String invoiceImage;
	    private String sharersDisplayText;
	    private LocalDateTime createAt;
	    private String createdByUsername;

	    public ExpenseDTO() {}

		public ExpenseDTO(Long id, String description, Double amount, Long payerId, String payerName, String sharerIds,
				String invoiceImage, String sharersDisplayText, LocalDateTime createAt) {
			super();
			this.id = id;
			this.description = description;
			this.amount = amount;
			this.payerId = payerId;
			this.payerName = payerName;
			this.sharerIds = sharerIds;
			this.invoiceImage = invoiceImage;
			this.sharersDisplayText = sharersDisplayText;
			this.createAt = createAt;
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

		public Long getPayerId() {
			return payerId;
		}

		public void setPayerId(Long payerId) {
			this.payerId = payerId;
		}

		public String getPayerName() {
			return payerName;
		}

		public void setPayerName(String payerName) {
			this.payerName = payerName;
		}

		public String getSharerIds() {
			return sharerIds;
		}

		public void setSharerIds(String sharerIds) {
			this.sharerIds = sharerIds;
		}

		public String getInvoiceImage() {
			return invoiceImage;
		}

		public void setInvoiceImage(String invoiceImage) {
			this.invoiceImage = invoiceImage;
		}

		public String getSharersDisplayText() {
			return sharersDisplayText;
		}

		public void setSharersDisplayText(String sharersDisplayText) {
			this.sharersDisplayText = sharersDisplayText;
		}

		public LocalDateTime getCreateAt() {
			return createAt;
		}

		public void setCreateAt(LocalDateTime createAt) {
			this.createAt = createAt;
		}

		public String getCreatedByUsername() {
			return createdByUsername;
		}

		public void setCreatedByUsername(String createdByUsername) {
			this.createdByUsername = createdByUsername;
		}
}
