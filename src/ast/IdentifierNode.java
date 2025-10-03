package ast;

public class IdentifierNode extends ASTNode {
    private String name;

    public IdentifierNode(int lineNumber, String name) {
        super(lineNumber);
        this.name = name;
    }

    public String getName() { 
        return name; 
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public Object acceptExpression(ExpressionVisitor visitor) {
        return visitor.visit(this);
    }

    /**
     * Método auxiliar para verificar si el identificador es un literal booleano (true/false).
     */
    public boolean isBooleanLiteral() {
        return "true".equals(name) || "false".equals(name);
    }
}
