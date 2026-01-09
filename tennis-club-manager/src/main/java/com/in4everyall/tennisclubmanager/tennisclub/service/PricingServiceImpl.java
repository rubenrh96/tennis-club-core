package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.in4everyall.tennisclubmanager.tennisclub.entity.PaymentEntity;
import com.in4everyall.tennisclubmanager.tennisclub.entity.PlayerSubscriptionEntity;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentStatus;
import com.in4everyall.tennisclubmanager.tennisclub.enums.PaymentType;
import com.in4everyall.tennisclubmanager.tennisclub.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

    private final PaymentRepository paymentRepository;

    @Override
    public BigDecimal calculateQuarterlyPrice(int numberOfClasses) {
        return new BigDecimal("140").multiply(new BigDecimal(numberOfClasses));
    }

    @Override
    public PaymentEntity createPaymentForSubscription(PlayerSubscriptionEntity subscription) {
        int numberOfClasses = subscription.getClassEnrollments() != null ? 
                subscription.getClassEnrollments().size() : 
                (subscription.getDaysPerWeek() != null ? subscription.getDaysPerWeek() : 1);
        
        BigDecimal amount = calculateQuarterlyPrice(numberOfClasses);

        PaymentEntity payment = PaymentEntity.builder()
                .player(subscription.getPlayer())
                .paymentType(PaymentType.QUARTERLY)
                .amount(amount)
                .paymentDate(LocalDate.now())
                .status(PaymentStatus.PENDING)
                .daysPerWeek(numberOfClasses)
                .quarterStartDate(subscription.getCurrentQuarterStart())
                .quarterEndDate(subscription.getCurrentQuarterEnd())
                .subscription(subscription)
                .year(subscription.getCurrentQuarterStart() != null ? 
                        subscription.getCurrentQuarterStart().getYear() : null)
                .quarterNumber(subscription.getCurrentQuarterStart() != null ? 
                        getQuarterNumber(subscription.getCurrentQuarterStart()) : null)
                .notes("Pago trimestral - Generado automáticamente al crear suscripción")
                .build();

        return paymentRepository.save(payment);
    }

    private Integer getQuarterNumber(LocalDate date) {
        int month = date.getMonthValue();
        if (month >= 1 && month <= 3) return 1;
        if (month >= 4 && month <= 6) return 2;
        if (month >= 9 && month <= 12) return 3;
        return 1;
    }
}

