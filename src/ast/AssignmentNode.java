package ast;

public class AssignmentNode extends ASTNode {
    private String variableName;
    private ASTNode value;

    public AssignmentNode(int lineNumber, String variableName, ASTNode value) {
        super(lineNumber);
        this.variableName = variableName;
        this.value = value;
    }

    public String getVariableName() { return variableName; }
    public ASTNode getValue() { return value; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public Object acceptExpression(ExpressionVisitor visitor) {
        return visitor.visit(this);
    }
}
