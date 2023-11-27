package com.leapwise.logicalexpressionevaluator.aplication_services.services;

import com.leapwise.logicalexpressionevaluator.api.dtos.expression.NewLogicalExpressionCreationRequest;
import com.leapwise.logicalexpressionevaluator.api.dtos.expression.NewLogicalExpressionEvaluationRequest;
import com.leapwise.logicalexpressionevaluator.api.dtos.general.BasicOperationValueResultDTO;
import com.leapwise.logicalexpressionevaluator.api.services.LogicalEvaluatorService;
import com.leapwise.logicalexpressionevaluator.aplication_services.types.exceptions.MalformedExpressionException;
import com.leapwise.logicalexpressionevaluator.aplication_services.types.exceptions.MalformedJsonException;
import com.leapwise.logicalexpressionevaluator.aplication_services.utils.JsonUtil;
import com.leapwise.logicalexpressionevaluator.domain.entites.LogicalExpression;
import com.leapwise.logicalexpressionevaluator.domain.repositories.LogicalExpressionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class LogicalEvaluatorServiceImpl implements LogicalEvaluatorService {

    private final LogicalExpressionRepository logicalExpressionRepository;
    private final Logger logger = LoggerFactory.getLogger(LogicalEvaluatorServiceImpl.class);

    public LogicalEvaluatorServiceImpl(LogicalExpressionRepository logicalExpressionRepository) {
        this.logicalExpressionRepository = logicalExpressionRepository;
    }

    @Override
    public BasicOperationValueResultDTO<String> createNewLogicalExpression(NewLogicalExpressionCreationRequest newLogicalExpressionCreationRequest) {
        try {
            LogicalExpression logicalExpression = logicalExpressionRepository.saveNewLogicalExpression(
                    new LogicalExpression(newLogicalExpressionCreationRequest.getExpressionName(),
                            newLogicalExpressionCreationRequest.getExpressionValue()));
            return new BasicOperationValueResultDTO<>(HttpStatus.OK.value(),
                    String.format("Logical expression with name %s has been saved. " +
                                    "Logical expression id: %s .", newLogicalExpressionCreationRequest.getExpressionName(),
                            logicalExpression.getId().toString()));
        } catch (Exception e) {
            logger.error(String.format("Unknown error occurred while saving logical expression with name %s .",
                    newLogicalExpressionCreationRequest.getExpressionName()), e);
            return new BasicOperationValueResultDTO<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    String.format("Unknown error occurred while saving logical expression with name %s .",
                            newLogicalExpressionCreationRequest.getExpressionName()));
        }
    }

    @Override
    public BasicOperationValueResultDTO<String> evaluateLogicalExpression(NewLogicalExpressionEvaluationRequest newLogicalExpressionEvaluationRequest) {
        try {
            if (Objects.nonNull(newLogicalExpressionEvaluationRequest.getExpressionEvaluationJsonBody())) {
                boolean isValueJson = JsonUtil.isValidJson(newLogicalExpressionEvaluationRequest.getExpressionEvaluationJsonBody());
                if (isValueJson) {
                    Optional<LogicalExpression> optionalLogicalExpression = getLogicalExpression(newLogicalExpressionEvaluationRequest);
                    if (optionalLogicalExpression.isPresent()) {
                        Map<String, Object> jsonNodeMap = JsonUtil.convertJsonDataToMap(newLogicalExpressionEvaluationRequest.getExpressionEvaluationJsonBody());
                        boolean result = LogicalExpressionParser.parseExpression(optionalLogicalExpression.get().getExpressionValue(),
                                jsonNodeMap, newLogicalExpressionEvaluationRequest.getExpressionLiteralValuePlaceholderList());
                        return new BasicOperationValueResultDTO<>(HttpStatus.OK.value(), String.format(
                                "Logical expression with name %s was evaluated.", optionalLogicalExpression.get().getExpressionName()),
                                String.format("Result of expression evaluation is -> %s .", String.valueOf(result).toUpperCase()));
                    } else {
                        return new BasicOperationValueResultDTO<>(HttpStatus.NOT_FOUND.value(), String.format(
                                "Logical expression with id %s was not found.", newLogicalExpressionEvaluationRequest.getExpressionId()));
                    }
                } else {
                    return new BasicOperationValueResultDTO<>(HttpStatus.BAD_REQUEST.value(), "Given expression evaluation Json data is not a proper Json value.");
                }
            } else {
                Optional<LogicalExpression> optionalLogicalExpression = getLogicalExpression(newLogicalExpressionEvaluationRequest);
                if (optionalLogicalExpression.isPresent()) {
                    boolean result = LogicalExpressionParser.parseExpression(optionalLogicalExpression.get().getExpressionValue(),
                            null, newLogicalExpressionEvaluationRequest.getExpressionLiteralValuePlaceholderList());
                    return new BasicOperationValueResultDTO<>(HttpStatus.OK.value(), String.format(
                            "Logical expression with name %s was evaluated.", optionalLogicalExpression.get().getExpressionName()),
                            String.format("Result of expression evaluation is -> %s .", String.valueOf(result).toUpperCase()));
                } else {
                    return new BasicOperationValueResultDTO<>(HttpStatus.NOT_FOUND.value(), String.format(
                            "Logical expression with id %s was not found.", newLogicalExpressionEvaluationRequest.getExpressionId()));
                }
            }
        } catch (MalformedExpressionException e) {
            return new BasicOperationValueResultDTO<>(HttpStatus.BAD_REQUEST.value(),
                    "Expression couldn't be evaluated because it is malformed. Make sure the expression is properly written " +
                            "following the given rules and that any necessary data for literals replacement is inputted. " +
                            "Error detail message: " + e.getErrorMessage());
        } catch (MalformedJsonException e) {
            return new BasicOperationValueResultDTO<>(HttpStatus.BAD_REQUEST.value(), "Provided json couldn't be processed. " +
                    "Make sure the value and formatting are valid.");
        } catch (IllegalArgumentException e) {
            return new BasicOperationValueResultDTO<>(HttpStatus.BAD_REQUEST.value(), String.format(
                    "Given expressionId %s is not a valid unique ID.", newLogicalExpressionEvaluationRequest.getExpressionId()));
        } catch (Exception e) {
            logger.error(String.format("An unknown error occurred while evaluating logical expression with id %s.",
                    newLogicalExpressionEvaluationRequest.getExpressionId()), e);
            return new BasicOperationValueResultDTO<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    String.format("An unknown error occurred while evaluating logical expression with id %s. " +
                                    "Double check that the expression is properly written following the given rules and " +
                                    "that any necessary data for literals replacement is inputted.",
                            newLogicalExpressionEvaluationRequest.getExpressionId()));
        }
    }

    private Optional<LogicalExpression> getLogicalExpression(NewLogicalExpressionEvaluationRequest newLogicalExpressionEvaluationRequest) {
        UUID logicalExpressionUuid = UUID.fromString(newLogicalExpressionEvaluationRequest.getExpressionId());
        return logicalExpressionRepository.findLogicalExpressionById(logicalExpressionUuid);
    }
}
