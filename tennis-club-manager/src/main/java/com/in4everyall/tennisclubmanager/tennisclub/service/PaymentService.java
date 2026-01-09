package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.dto.*;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentStatus;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentType;

import java.util.List;
import java.util.UUID;

public interface PaymentService {
    
    PaymentResponse createPayment(PaymentRequest request);
    
    PaymentResponse updatePayment(UUID paymentId, PaymentRequest request);
    
    PaymentResponse updatePaymentStatus(UUID paymentId, PaymentStatus status);
    
    PaymentResponse getPaymentById(UUID paymentId);
    
    List<PaymentResponse> getAllPayments();
    
    List<PaymentResponse> getPaymentsByPlayer(String licenseNumber);
    
    List<PaymentResponse> getPendingPayments();
    
    List<PaymentResponse> getPendingPaymentsByPlayer(String licenseNumber);
    
    PaymentSummaryResponse getPaymentSummary();
    
    PaymentSummaryResponse getPaymentSummaryByPlayer(String licenseNumber);
    
    List<PaymentResponse> getPaymentsByType(PaymentType paymentType);
    
    void deletePayment(UUID paymentId);
}




