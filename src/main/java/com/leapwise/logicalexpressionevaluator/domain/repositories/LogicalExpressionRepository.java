package com.leapwise.logicalexpressionevaluator.domain.repositories;

import com.leapwise.logicalexpressionevaluator.domain.entites.LogicalExpression;

import java.util.Optional;
import java.util.UUID;

public interface LogicalExpressionRepository {

    LogicalExpression saveNewLogicalExpression(LogicalExpression logicalExpression);

    Optional<LogicalExpression> findLogicalExpressionById(UUID uuid);
}
