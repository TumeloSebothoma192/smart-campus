package com.campus.service;

import com.campus.model.ServiceRequest;
import com.campus.model.User;
import java.util.List;

public interface CampusRequestService {
    ServiceRequest findRequest(long requestId);
    List<ServiceRequest> findAll();
    ServiceRequest storeRequest(User user, ServiceRequest request);
    boolean updateRequest(ServiceRequest request);
    boolean deleteRequest(long requestId);
}
