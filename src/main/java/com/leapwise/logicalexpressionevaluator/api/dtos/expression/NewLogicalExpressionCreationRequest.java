package com.leapwise.logicalexpressionevaluator.api.dtos.expression;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NewLogicalExpressionCreationRequest {

    @NotBlank(message = "Expression name must not be empty.")
    @Size(min = 3, max = 100, message = "Expression name must be between 3 and 100 characters long.")
    private String expressionName;

    @NotBlank(message = "Expression value must not be empty.")
    @Size(min = 4, max = 512, message = "Expression value must be between 4 and 512 characters long.")
    private String expressionValue;
}
