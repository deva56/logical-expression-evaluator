package com.leapwise.logicalexpressionevaluator.domain.entites;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogicalExpression {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotBlank(message = "Expression name must not be empty.")
    @Size(min = 3, max = 100, message = "Expression name must be between 3 and 100 characters long.")
    @Column(nullable = false, length = 100)
    private String expressionName;

    @NotBlank(message = "Expression value must not be empty.")
    @Size(min = 4, max = 512, message = "Expression value must be between 4 and 512 characters long.")
    @Column(nullable = false, length = 512)
    private String expressionValue;

    public LogicalExpression(String expressionName, String expressionValue) {
        this.expressionName = expressionName;
        this.expressionValue = expressionValue;
    }
}
