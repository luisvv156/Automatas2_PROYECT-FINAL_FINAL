// PrintNode.java (usar esta versión)
package ast;

public class PrintNode extends ASTNode {
    private ASTNode value;

    public PrintNode(int lineNumber, ASTNode value) {
        super(lineNumber);
        this.value = value;
    }

    public ASTNode getValue() {
        return value;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}