package com.will.sxlib.bean;

/**
 * Created by Will on 2016/5/23.
 */
public class BookState {
    private String callno;
    private String barcode;
    private String state;
    private String curlib;
    private String curlocal;
    private String totalLoanNumber;
    private String totalRenewNumber;
    private LoanData loanData;
    public void setTotalloanNumber(String totalLoanNumber){
        this.totalLoanNumber = totalLoanNumber;
    }
    public String getTotalLoanNumber(){
        return totalLoanNumber;
    }
    public void setTotalRenewNumber(String totalRenewNumber){
        this.totalRenewNumber = totalRenewNumber;
    }
    public String getTotalRenewNumber(){
        return totalRenewNumber;
    }
    public void setLoanData(LoanData loanData){
        this.loanData = loanData;
    }
    public LoanData getLoanData(){
        return loanData;
    }
    public void setCallno(String callno){
        this.callno = callno;
    }
    public String getCallno(){
        return callno;
    }
    public void setBarcode(String barcode){
        this.barcode = barcode;
    }
    public String getBarcode(){
        return barcode;
    }
    public void setState(String state){
        this.state = state;
    }
    public String getState(){
        return state;
    }
    public void setCurlib(String curlib){
        this.curlib = curlib;
    }
    public String getCurlib(){
        return curlib;
    }
    public void setCurlocal(String curlocal){
        this.curlocal = curlocal;
    }
    public String getCurlocal(){
        return curlocal;
    }
}
