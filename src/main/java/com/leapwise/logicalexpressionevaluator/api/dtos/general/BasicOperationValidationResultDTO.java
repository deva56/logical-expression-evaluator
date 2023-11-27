package com.leapwise.logicalexpressionevaluator.api.dtos.general;

import com.leapwise.logicalexpressionevaluator.api.dtos.validation.ValidationErrorDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class BasicOperationValidationResultDTO extends OperationResultDTO {

    private final List<ValidationErrorDTO> validationErrors;

    public BasicOperationValidationResultDTO(Integer statusCode, String resultDescription,
                                             List<ValidationErrorDTO> validationErrors) {
        super(statusCode, resultDescription);
        this.validationErrors = validationErrors;
    }
}
