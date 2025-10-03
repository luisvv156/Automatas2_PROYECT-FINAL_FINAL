package ast;

import java.util.ArrayList;
import java.util.List;
import semantic.Tipo;
public class FunctionNode extends ASTNode {
    private String functionName;
    private String returnType;
    private List<VariableDeclNode> parameters; // Parámetros de tipo VariableDeclNode
    private BlockNode body;

    public FunctionNode(int lineNumber, String functionName, String returnType) {
        super(lineNumber);
        this.functionName = functionName;
        this.returnType = returnType;
        this.parameters = new ArrayList<>();
    }

    // Agregar un parámetro a la función
    public void addParameter(VariableDeclNode parameter) {
        parameters.add(parameter);
    }

    // Obtener la lista de parámetros
    public List<VariableDeclNode> getParameters() {
        return new ArrayList<>(parameters);
    }

    // Obtener lista de tipos de parámetros (para SemanticAnalyzer)
    public List<Tipo> getParameterTypes() {
        List<Tipo> types = new ArrayList<>();
        for (VariableDeclNode param : parameters) {
            types.add(Tipo.fromString(param.getType()));
        }
        return types;
    }

    // Establecer el cuerpo de la función
    public void setBody(BlockNode body) {
        this.body = body;
    }

    // Obtener nombre de la función
    public String getFunctionName() { 
        return functionName; 
    }

    // Obtener tipo de retorno
    public String getReturnType() { 
        return returnType; 
    }

    // Obtener cuerpo
    public BlockNode getBody() { 
        return body; 
    }

    // Método para el ASTVisitor
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
