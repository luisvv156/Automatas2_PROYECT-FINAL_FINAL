package ast;

public interface ASTVisitor {
    // Nodos de declaraciones y bloques
    void visit(ProgramNode node);
    void visit(BlockNode node);
    void visit(FunctionNode node);
    void visit(VariableDeclNode node);

    // NUEVO: Nodos para clases
    void visit(ClassDeclNode node);
    void visit(MethodDeclNode node);
    void visit(ClassInstanceNode node);
    void visit(FieldAccessNode node);
    void visit(MethodCallNode node);

    // Nodos de expresiones
    void visit(AssignmentNode node);
    void visit(BinaryExpression node);
    void visit(UnaryExpressionNode node);
    void visit(LiteralNode node);
    void visit(IdentifierNode node);
    void visit(CallNode node);

    // Nodos de control
    void visit(IfNode node);
    void visit(WhileNode node);
    void visit(ReturnNode node);
    
    // Arrays
    void visit(ArrayNode node);
    void visit(ArrayAccessNode node);

    // Nodos de tipo o impresión
    void visit(TypeNode node);
    void visit(PrintNode node);

    // Nodos de statements generales
    void visit(ExpressionStatementNode node);
}