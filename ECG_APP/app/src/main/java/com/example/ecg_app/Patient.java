package com.example.ecg_app;

public class Patient {
    String patientName, patientAge, mobileNumber, place, result;

    public Patient() {
    }

    public Patient(String patientName, String patientAge, String mobileNumber, String place, String result) {
        this.patientName = patientName;
        this.patientAge = patientAge;
        this.mobileNumber = mobileNumber;
        this.place = place;
        this.result = result;
    }


    public String getPatientName() {
        return patientName;
    }

    public String getPatientAge() {
        return patientAge;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getPlace() {
        return place;
    }

    public String getResult() {
        return result;
    }
}
