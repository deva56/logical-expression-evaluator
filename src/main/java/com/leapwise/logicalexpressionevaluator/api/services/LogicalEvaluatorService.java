package com.leapwise.logicalexpressionevaluator.api.services;

import com.leapwise.logicalexpressionevaluator.api.dtos.expression.NewLogicalExpressionCreationRequest;
import com.leapwise.logicalexpressionevaluator.api.dtos.expression.NewLogicalExpressionEvaluationRequest;
import com.leapwise.logicalexpressionevaluator.api.dtos.general.BasicOperationValueResultDTO;

public interface LogicalEvaluatorService {

    BasicOperationValueResultDTO<String> createNewLogicalExpression(NewLogicalExpressionCreationRequest newLogicalExpressionCreationRequest);

    BasicOperationValueResultDTO<String> evaluateLogicalExpression(NewLogicalExpressionEvaluationRequest newLogicalExpressionEvaluationRequest);

}
