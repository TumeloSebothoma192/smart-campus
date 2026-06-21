package com.campus.rest;

import com.campus.model.ServiceRequest;
import java.util.ArrayList;
import java.util.List;

public class RequestForm {
    private List<ServiceRequest> requests = new ArrayList<>();

    public RequestForm() {
    }

    public RequestForm(List<ServiceRequest> requests) {
        this.requests = requests;
    }

    public List<ServiceRequest> getRequests() {
        return requests;
    }

    public void setRequests(List<ServiceRequest> requests) {
        this.requests = requests;
    }
}
