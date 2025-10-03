package ast;

public class UnaryExpressionNode extends ASTNode {
    private String operator;
    private ASTNode expression;

    public UnaryExpressionNode(int lineNumber, String operator, ASTNode expression) {
        super(lineNumber);
        this.operator = operator;
        this.expression = expression;
    }

    public String getOperator() { 
        return operator; 
    }
    
    public ASTNode getExpression() { 
        return expression; 
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    // Para ExpressionVisitor
    public Object accept(ExpressionVisitor visitor) {
        return visitor.visit(this);
    }
}