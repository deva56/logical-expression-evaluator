package com.leapwise.logicalexpressionevaluator.api.dtos.expression;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NewLogicalExpressionLiteralValuePlaceholder {

    @NotBlank(message = "Literal placeholder must not be empty.")
    @Size(min = 1, message = "Literal placeholder must be at least one character long." )
    private String literalPlaceholderName;

    @NotBlank(message = "Literal placeholder value must not be empty.")
    @Size(min = 1, message = "Literal placeholder value must be at least one character long." )
    private String literalPlaceholderValue;
}
