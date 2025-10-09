package ast;

public class ArrayAccessNode extends ASTNode {
    private String arrayName;
    private ASTNode index;

    public ArrayAccessNode(int lineNumber, String arrayName, ASTNode index) {
        super(lineNumber);
        this.arrayName = arrayName;
        this.index = index;
    }

    public String getArrayName() {
        return arrayName;
    }

    public ASTNode getIndex() {
        return index;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}