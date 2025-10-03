package ast;

// SOLO ESTO DEBE QUEDAR EN ASTNode.java
public abstract class ASTNode {
    private int lineNumber;

    public ASTNode(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public abstract void accept(ASTVisitor visitor);
}