package ast;

public class VariableDeclNode extends ASTNode {
    private String name;  // SOLO UN campo para el nombre
    private String type;
    private ASTNode initialValue;

    public VariableDeclNode(int lineNumber, String name, String type, ASTNode initialValue) {
        super(lineNumber);
        this.name = name;  // Inicializar el campo 'name'
        this.type = type;
        this.initialValue = initialValue;
    }

    // CORREGIDO: Solo un getter para el nombre
    public String getName() { return name; }
    public String getType() { return type; }
    public ASTNode getInitialValue() { return initialValue; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}