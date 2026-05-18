package main.java.com.fintech.dbilleteras_virtuales.dataStructure;

public class GraphEdge {
    
    public String sourceUserId;
    public String targetUserId;
    public double amount;
    public String transactionId;

    public GraphEdge(String sourceUserId, String targetUserId, double amount, String transactionId) {
        this.sourceUserId = sourceUserId;
        this.targetUserId = targetUserId;
        this.amount = amount;
        this.transactionId = transactionId;
    }
}