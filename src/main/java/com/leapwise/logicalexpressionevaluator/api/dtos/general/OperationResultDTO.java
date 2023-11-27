package com.leapwise.logicalexpressionevaluator.api.dtos.general;

import lombok.Getter;

@Getter
public abstract class OperationResultDTO {

    Integer statusCode;
    String resultDescription;

    public OperationResultDTO(Integer statusCode, String resultDescription) {
        this.statusCode = statusCode;
        this.resultDescription = resultDescription;
    }

}
