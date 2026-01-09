package com.in4everyall.tennisclubmanager.tennisclub.validator;

import com.in4everyall.tennisclubmanager.tennisclub.dto.MatchRequest;

public interface WinnerValidator {

    void validateWinner(MatchRequest req);

}
