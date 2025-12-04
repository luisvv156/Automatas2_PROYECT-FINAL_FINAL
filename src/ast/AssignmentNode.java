package ast;

public class AssignmentNode extends ASTNode {
    private String variableName;  // Para asignaciones simples: x = 5
    private ASTNode target;        // Para asignaciones a campos: p.nombre = "Juan"
    private ASTNode value;

    // Constructor para asignaciones simples
    public AssignmentNode(int lineNumber, String variableName, ASTNode value) {
        super(lineNumber);
        this.variableName = variableName;
        this.target = null;
        this.value = value;
    }

    // Constructor para asignaciones a campos/expresiones
    public AssignmentNode(int lineNumber, ASTNode target, ASTNode value) {
        super(lineNumber);
        this.variableName = null;
        this.target = target;
        this.value = value;
    }

    public String getVariableName() { return variableName; }
    public ASTNode getTarget() { return target; }
    public ASTNode getValue() { return value; }
    
    // Método helper para saber si es asignación simple o a campo
    public boolean isSimpleAssignment() {
        return variableName != null;
    }
    
    public boolean isFieldAssignment() {
        return target != null;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public Object acceptExpression(ExpressionVisitor visitor) {
        return visitor.visit(this);
    }
}