package com.leapwise.logicalexpressionevaluator.persistence_jpa.repository.implementation;

import com.leapwise.logicalexpressionevaluator.domain.entites.LogicalExpression;
import com.leapwise.logicalexpressionevaluator.domain.repositories.LogicalExpressionRepository;
import com.leapwise.logicalexpressionevaluator.persistence_jpa.repository.springdata.LogicalExpressionRepositorySpringData;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class LogicalExpressionRepositoryImpl implements LogicalExpressionRepository {

    private final LogicalExpressionRepositorySpringData logicalExpressionRepositorySpringData;

    public LogicalExpressionRepositoryImpl(LogicalExpressionRepositorySpringData logicalExpressionRepositorySpringData) {
        this.logicalExpressionRepositorySpringData = logicalExpressionRepositorySpringData;
    }

    @Override
    public LogicalExpression saveNewLogicalExpression(LogicalExpression logicalExpression) {
        return logicalExpressionRepositorySpringData.save(logicalExpression);
    }

    @Override
    public Optional<LogicalExpression> findLogicalExpressionById(UUID uuid) {
        return logicalExpressionRepositorySpringData.findById(uuid);
    }
}
