package com.in4everyall.tennisclubmanager.tennisclub.validator;

import com.in4everyall.tennisclubmanager.tennisclub.dto.SignUpRequest;

public interface SignUpValidator {

    void validateSignUpForm(SignUpRequest signUpRequest);
}
