package com.equinox.qikbusiness.Models;

import com.equinox.qikbusiness.Enums.EmployeeStatus;

/**
 * Created by mukht on 11/23/2016.
 */

public class Employee extends User {

    private EmployeeStatus employeeStatus;
    private Long lastAssignedTask;
    private String placeId;

    public EmployeeStatus getEmployeeStatus() {
        return employeeStatus;
    }
    public void setEmployeeStatus(EmployeeStatus employeeStatus) {
        this.employeeStatus = employeeStatus;
    }
    public Long getLastAssignedTask() {
        return lastAssignedTask;
    }
    public void setLastAssignedTask(Long lastAssignedTask) {
        this.lastAssignedTask = lastAssignedTask;
    }
    public String getPlaceId() {
        return placeId;
    }
    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
}
