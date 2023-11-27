package com.leapwise.logicalexpressionevaluator.persistence_jpa.repository.springdata;

import com.leapwise.logicalexpressionevaluator.domain.entites.LogicalExpression;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface LogicalExpressionRepositorySpringData extends CrudRepository<LogicalExpression, UUID> {
}
