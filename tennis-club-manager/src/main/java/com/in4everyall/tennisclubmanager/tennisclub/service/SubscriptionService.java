package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.SubscriptionRequest;
import com.in4everyall.tennisclubmanager.tennisclub.dto.SubscriptionResponse;

import java.util.List;
import java.util.UUID;

public interface SubscriptionService {
    
    SubscriptionResponse createSubscription(SubscriptionRequest request);
    
    SubscriptionResponse updateSubscription(UUID subscriptionId, SubscriptionRequest request);
    
    SubscriptionResponse getSubscriptionById(UUID subscriptionId);
    
    SubscriptionResponse getActiveSubscriptionByPlayer(String licenseNumber);
    
    List<SubscriptionResponse> getAllSubscriptions();
    
    List<SubscriptionResponse> getSubscriptionsByPlayer(String licenseNumber);
    
    List<SubscriptionResponse> getActiveSubscriptions();
    
    void deactivateSubscription(UUID subscriptionId);
    
    void deleteSubscription(UUID subscriptionId);
    
    SubscriptionResponse addClassesToSubscription(UUID subscriptionId, List<UUID> classTypeIds);
}




