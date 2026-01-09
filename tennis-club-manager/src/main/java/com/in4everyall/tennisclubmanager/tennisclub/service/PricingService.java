package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.entity.PaymentEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerSubscriptionEntity;

import java.math.BigDecimal;

public interface PricingService {
    
    BigDecimal calculateQuarterlyPrice(int numberOfClasses);
    
    PaymentEntity createPaymentForSubscription(PlayerSubscriptionEntity subscription);
}

