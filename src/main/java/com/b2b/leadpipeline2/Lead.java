package com.b2b.leadpipeline2;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "leads2")
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int leadId;

    private String companyName;
    private String contactName;
    private String phone;
    private double dealValue;
    private String stage;
    private Boolean isHot;
    private String lastContactedDate;

    // which sales rep this lead is assigned to - null means unassigned
    private Integer repId;

    public Lead() {
    }

    public Lead(String companyName, String contactName, String phone,
                double dealValue, Boolean isHot) {
        this.companyName = companyName;
        this.contactName = contactName;
        this.phone = phone;
        this.dealValue = dealValue;
        this.isHot = isHot;
        this.stage = "NEW";
    }

    public int getLeadId() { return leadId; }
    public void setLeadId(int leadId) { this.leadId = leadId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public double getDealValue() { return dealValue; }
    public void setDealValue(double dealValue) { this.dealValue = dealValue; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public Boolean isHot() { return isHot; }
    public void setHot(Boolean hot) { isHot = hot; }

    public String getLastContactedDate() { return lastContactedDate; }
    public void setLastContactedDate(String lastContactedDate) { this.lastContactedDate = lastContactedDate; }

    public Integer getRepId() { return repId; }
    public void setRepId(Integer repId) { this.repId = repId; }
}
