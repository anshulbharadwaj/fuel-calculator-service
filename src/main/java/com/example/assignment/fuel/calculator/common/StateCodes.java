package com.example.assignment.fuel.calculator.common;


import lombok.Getter;

@Getter
public enum StateCodes {
    Andhra_Pradesh("AP1"),
    Assam("AS"),
    Bihar("BR"),
    Chandigarh("CH"),
    Chhattishgarh("CT"),
    Dadara_and_Nagara_Haveli("DN"),
    Daman_and_Diu("DD"),
    Goa("GA"),
    Gujarat("GJ"),
    Haryana("HR"),
    Himachal_Pradesh("HP"),
    Jammu_and_Kashmir("JK"),
    Jharkhand("JH"),
    Karnataka("KA"),
    Kerala("KL"),
    Madhya_Pradesh("MP"),
    Maharashtra("MH"),
    Manipur("MN"),
    Meghalaya("ML"),
    Mizoram("MZ"),
    Nagaland("NL"),
    New_Delhi("DL"),
    Odisha("OR"),
    Pondicherry("PY"),
    Punjab("PB"),
    Rajasthan("RJ"),
    Sikkim("SK"),
    Tamil_Nadu("TN"),
    Telangana("TG"),
    Tripura("TR"),
    Uttar_Pradesh("UP"),
    Uttarakhand("UT"),
    West_Bengal("WB");

    private String stateCode;

    StateCodes(String stateCode) {
        this.stateCode = stateCode;
    }
}
