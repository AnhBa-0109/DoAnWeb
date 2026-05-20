package khanh.ntu.BF.models;

public class SettleUpDto {
	private String fromMemberName;
    private String toMemberName;
    private Double amount;

    public SettleUpDto(String fromMemberName, String toMemberName, Double amount) {
        this.fromMemberName = fromMemberName;
        this.toMemberName = toMemberName;
        this.amount = amount;
    }
    public String getFromMemberName() { 
    	return fromMemberName; 
    }
    public String getToMemberName() { 
    	return toMemberName; 
    }
    public Double getAmount() { 
    	return amount; 
    }
}
