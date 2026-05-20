package khanh.ntu.BF.models;

public class MemberDebtDto {
	private String expenseDescription;
    private Double amountOwed;
    private String payerName;

    public MemberDebtDto(String expenseDescription, Double amountOwed, String payerName) {
        this.expenseDescription = expenseDescription;
        this.amountOwed = amountOwed;
        this.payerName = payerName;
    }
    public String getExpenseDescription() { 
    	return expenseDescription; 
    }
    public Double getAmountOwed() { 
    	return amountOwed; 
    }
    public String getPayerName() { 
    	return payerName; 
    }
}
