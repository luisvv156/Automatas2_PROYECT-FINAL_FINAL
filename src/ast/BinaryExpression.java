// BinaryExpression.java (esta está bien)
package ast;

public class BinaryExpression extends ASTNode {
    private ASTNode left;
    private String operator;
    private ASTNode right;

    public BinaryExpression(int lineNumber, ASTNode left, String operator, ASTNode right) {
        super(lineNumber);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public ASTNode getLeft() { return left; }
    public String getOperator() { return operator; }
    public ASTNode getRight() { return right; }

    // ... resto del código igual

    public void setLeft(ASTNode left) { this.left = left; }
    public void setRight(ASTNode right) { this.right = right; }
    public void setOperator(String operator) { this.operator = operator; }

    // --- Visitor para análisis general ---
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    // --- Visitor para expresiones, método con nombre distinto ---
    public Object acceptExpression(ExpressionVisitor visitor) {
        return visitor.visit(this);
    }

    /**
     * Método auxiliar para verificar si el operador es lógico.
     */
    public boolean isLogicalOperator() {
        return "&&".equals(operator) || "||".equals(operator);
    }

    /**
     * Método auxiliar para verificar si el operador es comparativo.
     */
    public boolean isComparisonOperator() {
        return "==".equals(operator) || "!=".equals(operator) ||
               "<".equals(operator) || ">".equals(operator) ||
               "<=".equals(operator) || ">=".equals(operator);
    }

    /**
     * Método auxiliar para verificar si el operador es aritmético.
     */
    public boolean isArithmeticOperator() {
        return "+".equals(operator) || "-".equals(operator) ||
               "*".equals(operator) || "/".equals(operator);
    }
}
