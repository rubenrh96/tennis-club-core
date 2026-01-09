package com.in4everyall.tennisclubmanager.tennisclub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        // Para desarrollo: sin host configurado, solo para que exista el bean.
        return new JavaMailSenderImpl();
    }
}