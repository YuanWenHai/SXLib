package com.will.sxlib.bean;

/**
 * Created by Will on 2016/5/31.
 */
public class MyBook {
    private String barCode;
    private String title;
    private String callNo;
    private String local;
    private String type;
    private String volumeInfo;
    private String loanDate;
    private String returnDate;
    private String renewCount;
    public void setBarCode(String barCode){
        this.barCode = barCode;
    }
    public String getBarCode(){
        return barCode;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public String getTitle(){
        return title;
    }
    public void setCallNo(String callNo){
        this.callNo = callNo;
    }
    public String getCallNo(){
        return callNo;
    }
    public void setLocal(String local){
        this.local = local;
    }
    public String getLocal(){
        return local;
    }
    public void setType(String type){
        this.type = type;
    }
    public String getType(){
        return type;
    }
    public void setVolumeInfo(String volumeInfo){
        this.volumeInfo = volumeInfo;
    }
    public String  getVolumeInfo(){
        return volumeInfo;
    }
    public void setLoanDate(String loanDate){
        this.loanDate = loanDate;
    }
    public String getLoanDate(){
        return loanDate;
    }
    public void setReturnDate(String returnDate){
        this.returnDate = returnDate;
    }
    public String getReturnDate(){
        return returnDate;
    }
    public void setRenewCount(String renewCount){
        this.renewCount = renewCount;
    }
    public String getRenewCount(){
        return renewCount;
    }
}
