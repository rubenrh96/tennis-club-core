package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.util.List;

public record BulkSubscriptionResponse(
        Integer createdCount,
        List<SubscriptionResponse> subscriptions,
        List<PaymentResponse> payments,
        List<String> errors
) {}

