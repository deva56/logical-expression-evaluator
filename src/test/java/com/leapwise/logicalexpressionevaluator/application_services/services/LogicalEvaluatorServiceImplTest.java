package com.leapwise.logicalexpressionevaluator.application_services.services;

import com.leapwise.logicalexpressionevaluator.api.dtos.expression.NewLogicalExpressionCreationRequest;
import com.leapwise.logicalexpressionevaluator.api.dtos.expression.NewLogicalExpressionEvaluationRequest;
import com.leapwise.logicalexpressionevaluator.api.dtos.expression.NewLogicalExpressionLiteralValuePlaceholder;
import com.leapwise.logicalexpressionevaluator.api.dtos.general.BasicOperationValueResultDTO;
import com.leapwise.logicalexpressionevaluator.aplication_services.services.LogicalEvaluatorServiceImpl;
import com.leapwise.logicalexpressionevaluator.domain.entites.LogicalExpression;
import com.leapwise.logicalexpressionevaluator.domain.repositories.LogicalExpressionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LogicalEvaluatorServiceImplTest {

    @Mock
    private LogicalExpressionRepository logicalExpressionRepository;
    @InjectMocks
    private LogicalEvaluatorServiceImpl logicalEvaluatorService;

    @Test
    @DisplayName("Should save new logical expression for evaluation")
    void saveNewExpression() {
        NewLogicalExpressionCreationRequest newLogicalExpressionCreationRequest = new NewLogicalExpressionCreationRequest(
                "Test expression name", "True AND False");
        LogicalExpression logicalExpression = new LogicalExpression(UUID.fromString("d2db2264-93ac-4764-8555-4e37c11efb7c"),
                "Test expression name", "True AND False");
        when(logicalExpressionRepository.saveNewLogicalExpression(any())).thenReturn(logicalExpression);
        BasicOperationValueResultDTO<String> result = logicalEvaluatorService.createNewLogicalExpression(newLogicalExpressionCreationRequest);
        assertThat(result.getStatusCode()).isEqualTo(200);
        verify(logicalExpressionRepository, times(1)).saveNewLogicalExpression(any());
    }

    @Test
    @DisplayName("Should not find expression with the given id")
    void evaluateExpressionWithoutExistingId() {
        NewLogicalExpressionEvaluationRequest newLogicalExpressionEvaluationRequest = NewLogicalExpressionEvaluationRequest.builder()
                .expressionId("d2db2264-93ac-4764-8555-4e37c11efb7c").build();
        Optional<LogicalExpression> optionalLogicalExpression = Optional.empty();
        when(logicalExpressionRepository.findLogicalExpressionById(UUID.fromString("d2db2264-93ac-4764-8555-4e37c11efb7c")))
                .thenReturn(optionalLogicalExpression);
        BasicOperationValueResultDTO<String> result = logicalEvaluatorService.evaluateLogicalExpression(newLogicalExpressionEvaluationRequest);
        assertThat(result.getStatusCode()).isEqualTo(404);
        verify(logicalExpressionRepository, times(1)).findLogicalExpressionById(
                UUID.fromString("d2db2264-93ac-4764-8555-4e37c11efb7c"));
    }

    @Test
    @DisplayName("Should evaluate expression with given proper json and literal replacements")
    void evaluateExpressionWithProperJsonAndLiteralReplacement() {
        NewLogicalExpressionEvaluationRequest newLogicalExpressionEvaluationRequest = NewLogicalExpressionEvaluationRequest.builder()
                .expressionId("d2db2264-93ac-4764-8555-4e37c11efb7c")
                .expressionEvaluationJsonBody("{\"customer\":{\"firstName\":\"JOHN\",\"lastName\":\"DOE\",\"address\":{\"city\":\"Chicago\",\"zipCode\":1234,\"street\":\"56th\",\"houseNumber\":2345},\"salary\":99,\"type\":\"BUSINESS\"}}")
                .expressionLiteralValuePlaceholderList(List.of(new NewLogicalExpressionLiteralValuePlaceholder(
                        "x", "True")))
                .build();
        Optional<LogicalExpression> optionalLogicalExpression = Optional.of(new LogicalExpression(UUID.fromString("d2db2264-93ac-4764-8555-4e37c11efb7c"),
                "Test expression name", "((customer.firstName == 'JOHN' OR 99 < customer.salary) AND NOT(true AND false != true OR false)) OR (true AND x)"));
        when(logicalExpressionRepository.findLogicalExpressionById(UUID.fromString("d2db2264-93ac-4764-8555-4e37c11efb7c")))
                .thenReturn(optionalLogicalExpression);
        BasicOperationValueResultDTO<String> result = logicalEvaluatorService.evaluateLogicalExpression(newLogicalExpressionEvaluationRequest);
        assertThat(result.getStatusCode()).isEqualTo(200);
        verify(logicalExpressionRepository, times(1)).findLogicalExpressionById(
                UUID.fromString("d2db2264-93ac-4764-8555-4e37c11efb7c"));
        assertThat(result.getValue()).isEqualTo("Result of expression evaluation is -> TRUE .");
    }

    @Test
    @DisplayName("Should evaluate expression without given json and with literal replacements")
    void evaluateExpressionWithoutJsonAndWithLiteralReplacement() {
        NewLogicalExpressionEvaluationRequest newLogicalExpressionEvaluationRequest = NewLogicalExpressionEvaluationRequest.builder()
                .expressionId("d2db2264-93ac-4764-8555-4e37c11efb7c")
                .expressionLiteralValuePlaceholderList(List.of(new NewLogicalExpressionLiteralValuePlaceholder(
                        "x", "True")))
                .build();
        Optional<LogicalExpression> optionalLogicalExpression = Optional.of(new LogicalExpression(UUID.fromString("d2db2264-93ac-4764-8555-4e37c11efb7c"),
                "Test expression name", "(('JOHN' == 'JOHN' OR 99 < 100) AND NOT(true AND false != true OR false)) OR (true AND x)"));
        when(logicalExpressionRepository.findLogicalExpressionById(UUID.fromString("d2db2264-93ac-4764-8555-4e37c11efb7c")))
                .thenReturn(optionalLogicalExpression);
        BasicOperationValueResultDTO<String> result = logicalEvaluatorService.evaluateLogicalExpression(newLogicalExpressionEvaluationRequest);
        assertThat(result.getStatusCode()).isEqualTo(200);
        verify(logicalExpressionRepository, times(1)).findLogicalExpressionById(
                UUID.fromString("d2db2264-93ac-4764-8555-4e37c11efb7c"));
        assertThat(result.getValue()).isEqualTo("Result of expression evaluation is -> TRUE .");
    }

    @Test
    @DisplayName("Should fail evaluating expression without given proper json and with given literal replacements")
    void evaluateExpressionWithoutProperJsonAndWithLiteralReplacements() {
        NewLogicalExpressionEvaluationRequest newLogicalExpressionEvaluationRequest = NewLogicalExpressionEvaluationRequest.builder()
                .expressionId("d2db2264-93ac-4764-8555-4e37c11efb7c")
                .expressionEvaluationJsonBody("{\"customer\":{\"firstName\":\"JOHN\",\"lastName\":\"DOE\",\"address\":{\"city\":\"Chicago\",\"zipCode\":1234,\"street\":\"56th\",\"houseNumber\":2345},\"salary\":99,\"type\":\"BUSINESS\"}")
                .expressionLiteralValuePlaceholderList(List.of(new NewLogicalExpressionLiteralValuePlaceholder(
                        "x", "True")))
                .build();
        BasicOperationValueResultDTO<String> result = logicalEvaluatorService.evaluateLogicalExpression(newLogicalExpressionEvaluationRequest);
        assertThat(result.getStatusCode()).isEqualTo(400);
        assertThat(result.getResultDescription()).isEqualTo("Given expression evaluation Json data is not a proper Json value.");
    }

    @Test
    @DisplayName("Should fail evaluating expression that is malformed")
    void evaluateMalformedExpressionWithProperJsonAndWrongLiteralReplacements() {
        NewLogicalExpressionEvaluationRequest newLogicalExpressionEvaluationRequest = NewLogicalExpressionEvaluationRequest.builder()
                .expressionId("d2db2264-93ac-4764-8555-4e37c11efb7c")
                .expressionEvaluationJsonBody("{\"customer\":{\"firstName\":\"JOHN\",\"lastName\":\"DOE\",\"address\":{\"city\":\"Chicago\",\"zipCode\":1234,\"street\":\"56th\",\"houseNumber\":2345},\"salary\":99,\"type\":\"BUSINESS\"}}")
                .expressionLiteralValuePlaceholderList(List.of(new NewLogicalExpressionLiteralValuePlaceholder(
                        "x", "21")))
                .build();
        Optional<LogicalExpression> optionalLogicalExpression = Optional.of(new LogicalExpression(UUID.fromString("d2db2264-93ac-4764-8555-4e37c11efb7c"),
                "Test expression name", "((customer.firstName == 'JOHN' OR 99 < customer.salary) AND NOT(true AND false != true OR false)) OR (true AND x)"));
        when(logicalExpressionRepository.findLogicalExpressionById(UUID.fromString("d2db2264-93ac-4764-8555-4e37c11efb7c")))
                .thenReturn(optionalLogicalExpression);
        BasicOperationValueResultDTO<String> result = logicalEvaluatorService.evaluateLogicalExpression(newLogicalExpressionEvaluationRequest);
        assertThat(result.getStatusCode()).isEqualTo(400);
        verify(logicalExpressionRepository, times(1)).findLogicalExpressionById(
                UUID.fromString("d2db2264-93ac-4764-8555-4e37c11efb7c"));
        assertThat(result.getResultDescription()).contains("Expression couldn't be evaluated because it is malformed.");
    }

}
