package ast;
public class ReturnNode extends ASTNode {
    private ASTNode value;

    public ReturnNode(int lineNumber, ASTNode value) {
        super(lineNumber);
        this.value = value;
    }

    public ASTNode getValue() { return value; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public Object acceptExpression(ExpressionVisitor visitor) {
        return visitor.visit(this);
    }
}
