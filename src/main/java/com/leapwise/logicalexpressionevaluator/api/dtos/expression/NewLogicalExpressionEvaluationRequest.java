package com.leapwise.logicalexpressionevaluator.api.dtos.expression;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewLogicalExpressionEvaluationRequest {

    @NotBlank(message = "Expression id must not be empty.")
    @Size(min = 36, max = 36, message = "Expression id must be exactly 36 characters long.")
    private String expressionId;

    private Object expressionEvaluationJsonBody;

    @Valid
    private List<NewLogicalExpressionLiteralValuePlaceholder> expressionLiteralValuePlaceholderList;

}
