package com.leapwise.logicalexpressionevaluator.aplication_services.types.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class MalformedJsonException extends RuntimeException {

    private final String errorMessage;

    private Throwable cause;
}
