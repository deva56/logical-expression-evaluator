package com.leapwise.logicalexpressionevaluator.api_rest.controllers;

import com.leapwise.logicalexpressionevaluator.api.dtos.expression.NewLogicalExpressionCreationRequest;
import com.leapwise.logicalexpressionevaluator.api.dtos.expression.NewLogicalExpressionEvaluationRequest;
import com.leapwise.logicalexpressionevaluator.api.dtos.general.BasicOperationValidationResultDTO;
import com.leapwise.logicalexpressionevaluator.api.dtos.general.BasicOperationValueResultDTO;
import com.leapwise.logicalexpressionevaluator.api.dtos.validation.ValidationErrorDTO;
import com.leapwise.logicalexpressionevaluator.api.services.LogicalEvaluatorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/logical-evaluator")
public class LogicalEvaluatorController {

    private final LogicalEvaluatorService logicalEvaluatorService;

    public LogicalEvaluatorController(LogicalEvaluatorService logicalEvaluatorService) {
        this.logicalEvaluatorService = logicalEvaluatorService;
    }

    @PostMapping("/expression")
    public ResponseEntity<BasicOperationValueResultDTO<String>> createNewLogicalExpression(
            @Valid @RequestBody NewLogicalExpressionCreationRequest newLogicalExpressionCreationRequest) {
        BasicOperationValueResultDTO<String> result = logicalEvaluatorService.createNewLogicalExpression(newLogicalExpressionCreationRequest);
        return ResponseEntity.status(result.getStatusCode()).body(result);
    }

    @PostMapping("/evaluate")
    public ResponseEntity<BasicOperationValueResultDTO<String>> evaluateLogicalExpression(
            @Valid @RequestBody NewLogicalExpressionEvaluationRequest newLogicalExpressionEvaluationRequest) {
        BasicOperationValueResultDTO<String> result = logicalEvaluatorService.evaluateLogicalExpression(newLogicalExpressionEvaluationRequest);
        return ResponseEntity.status(result.getStatusCode()).body(result);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    BasicOperationValidationResultDTO onMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        List<ValidationErrorDTO> validationErrorDTOS = new ArrayList<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            validationErrorDTOS.add(new ValidationErrorDTO(fieldError.getField(), fieldError.getDefaultMessage()));
        }
        return new BasicOperationValidationResultDTO(HttpStatus.BAD_REQUEST.value(), "There were some validation errors detected.",
                validationErrorDTOS);
    }
}
