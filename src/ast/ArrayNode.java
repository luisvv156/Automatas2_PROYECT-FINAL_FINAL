package ast;

import java.util.ArrayList;
import java.util.List;

public class ArrayNode extends ASTNode {
    private List<ASTNode> elements;
    private String elementType; // Tipo de los elementos del array

    public ArrayNode(int lineNumber, List<ASTNode> elements, String elementType) {
        super(lineNumber);
        this.elements = elements != null ? elements : new ArrayList<>();
        this.elementType = elementType;
    }

    public List<ASTNode> getElements() {
        return elements;
    }

    public String getElementType() {
        return elementType;
    }

    public void addElement(ASTNode element) {
        elements.add(element);
    }

    public int getSize() {
        return elements.size();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}