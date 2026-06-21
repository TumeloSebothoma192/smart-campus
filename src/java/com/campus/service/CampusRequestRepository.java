package com.campus.service;

import com.campus.model.ServiceRequest;
import com.campus.model.User;
import com.campus.store.AppStore;
import java.util.ArrayList;
import java.util.List;

public class CampusRequestRepository implements CampusRequestService {
    @Override
    public ServiceRequest findRequest(long requestId) {
        return AppStore.request(requestId);
    }

    @Override
    public List<ServiceRequest> findAll() {
        return new ArrayList<>(AppStore.requests);
    }

    @Override
    public ServiceRequest storeRequest(User user, ServiceRequest request) {
        return AppStore.addRequest(
                user,
                request.getTitle(),
                request.getDescription(),
                request.getCategoryName(),
                request.getDepartmentName());
    }

    @Override
    public boolean updateRequest(ServiceRequest request) {
        ServiceRequest existing = AppStore.request(request.getRequestId());
        if (existing == null) {
            return false;
        }
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            existing.setTitle(request.getTitle());
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            existing.setDescription(request.getDescription());
        }
        if (request.getCategoryName() != null && !request.getCategoryName().isBlank()) {
            existing.setCategory(request.getCategoryName());
        }
        if (request.getDepartmentName() != null && !request.getDepartmentName().isBlank()) {
            existing.setDepartment(request.getDepartmentName());
        }
        if (request.getPriority() != null && !request.getPriority().isBlank()) {
            existing.setPriority(request.getPriority());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            existing.setStatus(request.getStatus());
        }
        AppStore.persistRequest(existing);
        return true;
    }

    @Override
    public boolean deleteRequest(long requestId) {
        ServiceRequest existing = AppStore.request(requestId);
        if (existing == null) {
            return false;
        }
        boolean removed = AppStore.requests.remove(existing);
        if (removed) {
            AppStore.deleteRequest(requestId);
        }
        return removed;
    }
}
