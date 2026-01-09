package com.in4everyall.tennisclubmanager.tennisclub.dto;

import java.util.List;
import java.util.UUID;

public record AddClassesToSubscriptionRequest(
        List<UUID> classTypeIds
) {}



