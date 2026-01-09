package com.in4everyall.tennisclubmanager.tennisclub.validator;

import com.in4everyall.tennisclubmanager.tennisclub.dto.SignUpRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignUpValidatorImplTest {

    private final SignUpValidatorImpl validator = new SignUpValidatorImpl();

    @Test
    void shouldAcceptWellFormedRequest() {
        assertThatCode(() -> validator.validateSignUpForm(validRequest()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectInvalidDni() {
        var invalid = new SignUpRequest(
                "12345678A",
                "Ana",
                "García",
                "1990-01-01",
                "top",
                "slice",
                "ana@club.com",
                "password1",
                "password1",
                "PLAYER",
                "+34000111222"
        );

        assertThatThrownBy(() -> validator.validateSignUpForm(invalid))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("DNI no es válido");
    }

    private SignUpRequest validRequest() {
        return new SignUpRequest(
                "12345678Z",
                "Ana",
                "García",
                "1990-01-01",
                "top",
                "slice",
                "ana@club.com",
                "password1",
                "password1",
                "PLAYER",
                "+34000111222"
        );
    }
}

