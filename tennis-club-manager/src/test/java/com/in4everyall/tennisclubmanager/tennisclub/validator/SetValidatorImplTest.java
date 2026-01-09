package com.in4everyall.tennisclubmanager.tennisclub.validator;

import com.in4everyall.tennisclubmanager.tennisclub.exception.SetException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SetValidatorImplTest {

    private final SetValidatorImpl validator = new SetValidatorImpl();

    @Test
    void shouldValidateStraightSetsMatch() {
        assertThatCode(() ->
                validator.validateSets(
                        (short) 6, (short) 3,
                        (short) 6, (short) 4,
                        null, null
                )
        ).doesNotThrowAnyException();
    }

    @Test
    void shouldFailWhenSecondSetProvidedWithoutFirst() {
        assertThatThrownBy(() ->
                validator.validateSets(
                        null, null,
                        (short) 6, (short) 4,
                        null, null
                )
        ).isInstanceOf(SetException.class)
                .hasMessageContaining("set 2");
    }
}

