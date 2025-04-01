package it.gov.pagopa.emd.payment.stub.model;

public class PaymentInfo {
    private String amount;
    private String dueDate;
    private String paymentNote;

    public PaymentInfo(String amount, String dueDate, String paymentNote) {
        this.amount = amount;
        this.dueDate = dueDate;
        this.paymentNote = paymentNote;
    }

    public String getAmount() { return amount; }
    public String getDueDate() { return dueDate; }
    public String getPaymentNote() { return paymentNote; }
}