package ast;

public interface ASTVisitor {
    // Nodos de declaraciones y bloques
    void visit(ProgramNode node);
    void visit(BlockNode node);
    void visit(FunctionNode node);
    void visit(VariableDeclNode node);

    // Nodos de expresiones
    void visit(AssignmentNode node);
    void visit(BinaryExpression node);
    void visit(UnaryExpressionNode node); // <-- agregado
    void visit(LiteralNode node);
    void visit(IdentifierNode node);
    void visit(CallNode node);

    // Nodos de control
    void visit(IfNode node);
    void visit(WhileNode node);
    void visit(ReturnNode node);

    // Nodos de tipo o impresión
    void visit(TypeNode node);
    void visit(PrintNode node);

    // Nodos de statements generales
    void visit(ExpressionStatementNode node);
}
