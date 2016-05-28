package com.will.sxlib.bean;

/**
 * Created by Will on 2016/5/23.
 */
public class LoanData {
    private long loanDate;
    private long returnDate;
    private String loanDateStr;
    private String returnDateStr;
    public void setLoanDate(){
        this.loanDate = loanDate;
    }
    public long getLoanDate(){
        return loanDate;
    }
    public void setReturnDate(long returnDate){
        this.returnDate = returnDate;
    }
    public long getReturnDate(){
        return returnDate;
    }
    public void setLoanDateStr(String loanDateStr){
        this.loanDateStr = loanDateStr;
    }
    public String getLoanDateStr(){
        return loanDateStr;
    }
    public void setReturnDateStr(String returnDateStr){
        this.returnDateStr = returnDateStr;
    }
    public String getReturnDateStr(){
        return returnDateStr;
    }
}
