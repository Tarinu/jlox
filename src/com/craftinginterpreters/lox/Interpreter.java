package com.craftinginterpreters.lox;

public class Interpreter implements Expr.Visitor<Object> {
    void interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }
    
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        
        return switch (expr.operator.type) {
            case BANG_EQUAL -> !isEqual(left, right);
            case EQUAL_EQUAL -> isEqual(left, right);
            case GREATER -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left >= (double) right;
            }
            case LESS -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left <= (double) right;
            }
            case MINUS -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left - (double) right;
            }
            case PLUS -> {
                if (left instanceof Double && right instanceof Double) {
                    yield (double) left + (double) right;
                }
                
                if (left instanceof String && right instanceof String) {
                    yield (String) left + (String) right;
                }
                
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            }
            case SLASH -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left / (double) right;
            }
            case STAR -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left * (double) right;
            }
            default -> null;
        };
    }
    
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr);
    }
    
    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }
    
    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        
        return switch (expr.operator.type) {
            case BANG -> !isTruthy(right);
            case MINUS -> -(double) right;
            default -> null;
        };
    }
    
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }
    
    private boolean isTruthy(Object object) {
        if (object == null) {
            return false;
        }
        
        if (object instanceof Boolean) {
            return (boolean) object;
        }
        
        return true;
    }
    
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        
        if (a == null) {
            return false;
        }
        
        return a.equals(b);
    }
    
    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) {
            return;
        }
        
        throw new RuntimeError(operator, "Operands must be numbers.");
    }
    
    private String stringify(Object object) {
        if (object == null) {
            return "nil";
        }
        
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        
        return object.toString();
    }
}
