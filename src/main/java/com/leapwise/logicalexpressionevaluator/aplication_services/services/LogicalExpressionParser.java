package com.leapwise.logicalexpressionevaluator.aplication_services.services;

import com.leapwise.logicalexpressionevaluator.api.dtos.expression.NewLogicalExpressionLiteralValuePlaceholder;
import com.leapwise.logicalexpressionevaluator.aplication_services.types.exceptions.MalformedExpressionException;

import java.util.*;

public class LogicalExpressionParser {

    public static boolean parseExpression(String expression, Map<String, Object> jsonNodeMap,
                                          List<NewLogicalExpressionLiteralValuePlaceholder>
                                                  literalValuePlaceholderList) {
        String formattedExpression = performExpressionFormatting(expression);
        List<String> expressionTokens = tokenize(formattedExpression);
        List<String> literalPlaceholderValuesFormattedTokens = expressionTokens;
        if (Objects.nonNull(literalValuePlaceholderList)) {
            literalPlaceholderValuesFormattedTokens = replaceLiteralPlaceholdersWithValues(
                    literalValuePlaceholderList,
                    expressionTokens
            );
        }
        List<String> jsonFormattedTokens = literalPlaceholderValuesFormattedTokens;
        if (Objects.nonNull(jsonNodeMap)) {
            jsonFormattedTokens = replaceLiteralsWithJsonValues(literalPlaceholderValuesFormattedTokens, jsonNodeMap);
        }
        return evaluateTokens(jsonFormattedTokens);
    }

    private static List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean isParsingMapObject = false;
        StringBuilder currentMapObject = new StringBuilder();

        for (int i = 0; i < expression.toCharArray().length; i++) {
            if (!isParsingMapObject) {
                if (Character.isWhitespace(expression.toCharArray()[i])) {
                    if (!currentToken.isEmpty()) {
                        tokens.add(currentToken.toString());
                        currentToken.setLength(0);
                    }
                } else if (expression.toCharArray()[i] == '(' || expression.toCharArray()[i] == ')') {
                    if (!currentToken.isEmpty()) {
                        tokens.add(currentToken.toString());
                        currentToken.setLength(0);
                    }
                    tokens.add(String.valueOf(expression.toCharArray()[i]));
                } else if (expression.toCharArray()[i] == '{') {
                    currentMapObject.append(expression.toCharArray()[i]);
                    isParsingMapObject = true;
                } else if (expression.toCharArray()[i] == 'T') {
                    if (currentToken.toString().equals("NO")) {
                        currentToken.append(expression.toCharArray()[i]);
                        tokens.add(currentToken.toString());
                        currentToken.setLength(0);
                    } else {
                        currentToken.append(expression.toCharArray()[i]);
                    }
                } else if (i == expression.toCharArray().length - 1) {
                    if (!currentToken.isEmpty()) {
                        currentToken.append(expression.toCharArray()[i]);
                        tokens.add(currentToken.toString());
                        currentToken.setLength(0);
                    }
                } else {
                    currentToken.append(expression.toCharArray()[i]);
                }
            } else {
                if ((expression.toCharArray()[i] == '}')) {
                    if (i == expression.toCharArray().length - 1) {
                        currentMapObject.append(expression.toCharArray()[i]);
                        tokens.add(currentMapObject.toString());
                        currentMapObject.setLength(0);
                        isParsingMapObject = false;
                    } else {
                        if (expression.toCharArray()[i + 1] != ',') {
                            currentMapObject.append(expression.toCharArray()[i]);
                            tokens.add(currentMapObject.toString());
                            currentMapObject.setLength(0);
                            isParsingMapObject = false;
                        } else {
                            currentMapObject.append(expression.toCharArray()[i]);
                        }
                    }
                } else {
                    currentMapObject.append(expression.toCharArray()[i]);
                }
            }
        }

        return tokens;
    }

    private static boolean evaluateTokens(List<String> tokens) {
        try {
            Stack<Object> operandStack = new Stack<>();
            Stack<String> operatorStack = new Stack<>();

            for (String token : tokens) {
                if (isBooleanLiteral(token) || isNumericLiteral(token)) {
                    operandStack.push(convertToLiteral(token));
                } else if ("(".equals(token)) {
                    operatorStack.push(token);
                } else if (")".equals(token)) {
                    while (!operatorStack.isEmpty() && !"(".equals(operatorStack.peek())) {
                        performOperation(operatorStack.pop(), operandStack);
                    }
                    operatorStack.pop(); // Pop the '('
                } else if (isOperator(token)) {
                    while (!operatorStack.isEmpty() && operandStack.size() > 1 && hasPrecedence(token, operatorStack.peek())) {
                        performOperation(operatorStack.pop(), operandStack);
                    }
                    operatorStack.push(token);
                } else {
                    operandStack.push((token));
                }
            }

            while (!operatorStack.isEmpty()) {
                performOperation(operatorStack.pop(), operandStack);
            }

            return (boolean) operandStack.pop();
        } catch (EmptyStackException e) {
            throw new MalformedExpressionException("Expression couldn't be evaluated because it was malformed. Make sure that there are proper " +
                    "number of operators and operands required by your expression.");
        }
    }

    private static void performOperation(String operator, Stack<Object> operandStack) {
        if ("NOT".equals(operator)) {
            operandStack.push(!((boolean) operandStack.pop()));
        } else if (isLogicalOperator(operator)) {
            performLogicalOperation(operator, operandStack);
        } else if (isRelationalOperator(operator)) {
            performRelationalOperation(operator, operandStack);
        } else {
            throw new MalformedExpressionException(String.format("Unsupported operator %s detected while performing logical or relational operation.",
                    operator));
        }
    }

    private static void performLogicalOperation(String operator, Stack<Object> operandStack) {
        Object objectOperand2 = operandStack.pop();
        Object objectOperand1 = operandStack.pop();

        try {
            boolean operand2 = (boolean) objectOperand2;
            boolean operand1 = (boolean) objectOperand1;

            switch (operator) {
                case "AND":
                    operandStack.push(operand1 && operand2);
                    break;
                case "OR":
                    operandStack.push(operand1 || operand2);
                    break;
                default:
                    throw new MalformedExpressionException(String.format("Unsupported logical operator %s detected.", operator));
            }
        } catch (ClassCastException e) {
            throw new MalformedExpressionException(String.format("Operands %s and %s are not both logical TRUE or FALSE values therefore the expression couldn't " +
                    "be evaluated.", objectOperand1, objectOperand2));
        }
    }

    private static void performRelationalOperation(String operator, Stack<Object> operandStack) {
        Object operand2 = operandStack.pop();
        Object operand1 = operandStack.pop();

        if (operand1 instanceof Comparable && operand2 instanceof Comparable) {
            int comparisonResult;
            try {
                comparisonResult = ((Comparable) operand1).compareTo(operand2);
            } catch (ClassCastException e) {
                throw new MalformedExpressionException(String.format("Operands %s and %s cant be compared together since they are of different types.",
                        operand1, operand2));
            }
            switch (operator) {
                case ">":
                    operandStack.push(comparisonResult > 0);
                    break;
                case "<":
                    operandStack.push(comparisonResult < 0);
                    break;
                case ">=":
                    operandStack.push(comparisonResult >= 0);
                    break;
                case "<=":
                    operandStack.push(comparisonResult <= 0);
                    break;
                case "==":
                    operandStack.push(comparisonResult == 0);
                    break;
                case "!=":
                    operandStack.push(comparisonResult != 0);
                    break;
                default:
                    throw new MalformedExpressionException(String.format("Unsupported relational operator %s detected.", operator));
            }
        } else {
            throw new MalformedExpressionException(String.format("Unsupported operand types for comparison. " +
                    "Operand types: operand1 %s ; operand2 %s", operand1.getClass().getName(), operand2.getClass().getName()));
        }
    }

    private static boolean hasPrecedence(String op1, String op2) {
        boolean isOperator1Logical = isLogicalOperator(op1);
        boolean isOperator2Logical = isLogicalOperator(op2);

        if (isOperator1Logical && isOperator2Logical) {
            return hasLogicalOperatorPrecedence(op1, op2);
        } else {
            int precedence1 = getPrecedence(op1);
            int precedence2 = getPrecedence(op2);

            return precedence1 >= precedence2;
        }
    }

    private static boolean hasLogicalOperatorPrecedence(String op1, String op2) {
        int operator1Precedence = getLogicalOperatorPrecedence(op1);
        int operator2Precedence = getLogicalOperatorPrecedence(op2);

        if (operator1Precedence == operator2Precedence && isRightAssociative(op1)) {
            return false;
        }

        return operator1Precedence <= operator2Precedence;
    }

    private static int getPrecedence(String operator) {
        if (operator.equals("(")) {
            return 3;
        } else if (isLogicalOperator(operator)) {
            return 2;
        } else if (isRelationalOperator(operator)) {
            return 1;
        } else {
            throw new MalformedExpressionException(String.format("Unknown operator %s provided for precedence calculation.", operator));
        }
    }

    private static int getLogicalOperatorPrecedence(String operator) {
        if ("NOT".equalsIgnoreCase(operator)) {
            return 3;
        } else if ("AND".equalsIgnoreCase(operator)) {
            return 2;
        } else if ("OR".equalsIgnoreCase(operator)) {
            return 1;
        } else {
            throw new MalformedExpressionException(String.format("Unknown logical operator %s provided.", operator));
        }
    }

    private static boolean isRightAssociative(String operator) {
        return "NOT".equals(operator);
    }

    private static boolean isOperator(String operator) {
        return isLogicalOperator(operator) || isRelationalOperator(operator);
    }

    private static boolean isLogicalOperator(String operator) {
        return "AND".equalsIgnoreCase(operator) || "OR".equalsIgnoreCase(operator) || "NOT".equalsIgnoreCase(operator);
    }

    private static boolean isRelationalOperator(String operator) {
        return ">".equals(operator) || "<".equals(operator) || ">=".equals(operator)
                || "<=".equals(operator) || "==".equals(operator) || "!=".equals(operator);
    }

    private static boolean isBooleanLiteral(String token) {
        return "TRUE".equalsIgnoreCase(token) || "FALSE".equalsIgnoreCase(token);
    }

    private static boolean isNumericLiteral(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static Object convertToLiteral(String token) {
        if (isBooleanLiteral(token)) {
            return Boolean.parseBoolean(token);
        } else if (isNumericLiteral(token)) {
            return Double.parseDouble(token);
        } else {
            throw new MalformedExpressionException(String.format("Unsupported literal type %s detected.", token));
        }
    }

    private static List<String> replaceLiteralPlaceholdersWithValues(List<NewLogicalExpressionLiteralValuePlaceholder>
                                                                             literalValuePlaceholderList,
                                                                     List<String> expressionTokens) {
        if (!literalValuePlaceholderList.isEmpty()) {
            List<String> literalPlaceholderValuesFormattedTokens = new ArrayList<>();
            for (String expressionToken : expressionTokens) {
                boolean hasExpressionTokenBeenChanged = false;
                for (NewLogicalExpressionLiteralValuePlaceholder literalPlaceholderValue : literalValuePlaceholderList) {
                    if (expressionToken.equals(literalPlaceholderValue.getLiteralPlaceholderName())) {
                        literalPlaceholderValuesFormattedTokens.add(literalPlaceholderValue.getLiteralPlaceholderValue());
                        hasExpressionTokenBeenChanged = true;
                    }
                }
                if (!hasExpressionTokenBeenChanged) literalPlaceholderValuesFormattedTokens.add(expressionToken);
            }
            return literalPlaceholderValuesFormattedTokens;
        } else {
            return expressionTokens;
        }
    }

    private static List<String> replaceLiteralsWithJsonValues(List<String> nonJsonFormattedTokens, Map<String, Object> nodeMap) {
        List<String> jsonReformattedTokens = new ArrayList<>();
        for (String nonJsonFormattedToken : nonJsonFormattedTokens) {
            if (nonJsonFormattedToken.contains(".")) {
                String[] jsonValueTokens = nonJsonFormattedToken.split("\\.");
                String rightMostToken = jsonValueTokens[jsonValueTokens.length - 1];
                String leftMostToken = jsonValueTokens[0];
                if (nodeMap.containsKey(leftMostToken)) {
                    int counter = 0;
                    Map<String, Object> currentWorkingMap = nodeMap;
                    while (true) {
                        Object currentWorkingValue = currentWorkingMap.get(jsonValueTokens[counter]);
                        counter++;
                        if (currentWorkingValue instanceof Map<?, ?>) {
                            if (((Map<?, ?>) currentWorkingValue).containsKey(rightMostToken)) {
                                jsonReformattedTokens.add(((Map<?, ?>) currentWorkingValue).get(rightMostToken).toString());
                                break;
                            } else {
                                currentWorkingMap = (Map<String, Object>) currentWorkingValue;
                            }
                        } else if (Objects.isNull(currentWorkingValue)) {
                            jsonReformattedTokens.add("NULL");
                            break;
                        } else {
                            throw new MalformedExpressionException(String.format("Unhandled json map value detected. Value type: %s .",
                                    currentWorkingValue.getClass().getName()));
                        }
                    }
                } else {
                    throw new MalformedExpressionException(String.format("Malformed json structure provided in expression. " +
                            "Key %s doesn't exist in provided json data.", leftMostToken));
                }
            } else {
                if (nodeMap.containsKey(nonJsonFormattedToken)) {
                    jsonReformattedTokens.add(nodeMap.get(nonJsonFormattedToken).toString());
                } else {
                    jsonReformattedTokens.add(nonJsonFormattedToken);
                }
            }
        }
        return jsonReformattedTokens;
    }

    private static String performExpressionFormatting(String expression) {
        return expression.replaceAll("&&", "AND")
                .replaceAll("(?i)AND", "AND")
                .replaceAll("\\|\\|", "OR")
                .replaceAll("(?i)OR", "OR")
                .replaceAll("\\!(?!=)", "NOT")
                .replaceAll("(?i)NOT", "NOT")
                .replaceAll("(?i)NULL", "NULL")
                .replaceAll("(?i)TRUE", "TRUE")
                .replaceAll("(?i)FALSE", "FALSE")
                .replaceAll("\"", "")
                .replaceAll("'", "");
    }
}
