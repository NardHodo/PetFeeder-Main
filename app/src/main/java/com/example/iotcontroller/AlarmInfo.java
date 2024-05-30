package com.example.iotcontroller;

import java.util.List;

public class AlarmInfo {
    private final String assignedTime;
    private final List<String> assignedDays;

    public AlarmInfo(String assignedTime, List<String> assignedDays){
        this.assignedTime = assignedTime;
        this.assignedDays = assignedDays;
    }

    public String getAssignedTime(){
        return assignedTime;
    }

    public List<String> getAssignedDays(){
        return assignedDays;
    }
}
