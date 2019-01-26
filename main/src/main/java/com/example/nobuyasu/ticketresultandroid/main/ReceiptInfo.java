package com.example.nobuyasu.ticketresultandroid.main;

class ReceiptInfo {
    private String phoneNumber;
    private String[] receiptNumbers;

    public ReceiptInfo(String phoneNumber, String[] receiptNumbers) {
        this.phoneNumber = phoneNumber;
        this.receiptNumbers = receiptNumbers;
    }
    public ReceiptInfo(String phoneNumber, String receiptNumberStr) {
        this(phoneNumber, receiptNumberStr.split(","));
    }
    public ReceiptInfo(String phoneNumber) {
        this(phoneNumber, new String[]{});
    }
    public ReceiptInfo() {
        this("09011112222", new String[]{});
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String[] getReceiptNumbers() {
        return this.receiptNumbers;
    }
    public void setReceiptNumbers(String[] receiptNumbers) {
        this.receiptNumbers = receiptNumbers;
    }
    public void addReceiptNumber(String newReceiptNumber) {
        String[] receiptNumbers = new String[this.receiptNumbers.length + 1];
        for (int i=0; i<this.receiptNumbers.length; i++) {
            receiptNumbers[i] = this.receiptNumbers[i];
        }
        receiptNumbers[this.receiptNumbers.length] = newReceiptNumber;
        this.receiptNumbers = receiptNumbers;
    }
    public void resetReceiptNumbers() {
        this.receiptNumbers = new String[]{};
    }
    public String getReceiptNumberStr() {
        String receiptNumberStr = new String();
        for (String receiptNumber : this.receiptNumbers) {
            receiptNumberStr = receiptNumberStr.concat(",").concat(receiptNumber);
        }
        return receiptNumberStr.substring(1);
    }
    public void setReceiptNumberStr(String receiptNumberStr) {
        this.receiptNumbers = receiptNumberStr.split(",");
    }

    public String stringify() {
        return "{phonenumber:"+this.phoneNumber+"}";
    }
}
