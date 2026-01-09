package com.in4everyall.tennisclubmanager.tennisclub.entity.converter;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

class YearMonthAttributeConverterTest {

    private final YearMonthAttributeConverter converter = new YearMonthAttributeConverter();

    @Test
    void shouldConvertToDatabaseColumn() {
        YearMonth march = YearMonth.of(2025, 3);

        assertThat(converter.convertToDatabaseColumn(march)).isEqualTo("2025-03");
    }

    @Test
    void shouldReturnNullWhenYearMonthIsNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void shouldConvertToEntityAttribute() {
        YearMonth result = converter.convertToEntityAttribute("2024-11");

        assertThat(result).isEqualTo(YearMonth.of(2024, 11));
    }
}

