package com.leapwise.logicalexpressionevaluator.api.dtos.general;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasicOperationValueResultDTO<T> extends OperationResultDTO {

    private T value;

    public BasicOperationValueResultDTO(Integer statusCode, String resultDescription, T value) {
        super(statusCode, resultDescription);
        this.value = value;
    }

    public BasicOperationValueResultDTO(Integer statusCode, String resultDescription) {
        super(statusCode, resultDescription);
    }
}
