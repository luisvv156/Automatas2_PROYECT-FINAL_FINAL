package ast;

public interface ExpressionVisitor {
    // Nodos de declaraciones y bloques
    Object visit(BlockNode node);
    Object visit(FunctionNode node);          // útil si quieres inferir tipos de funciones
    Object visit(VariableDeclNode node);

    // Nodos de expresiones
    Object visit(AssignmentNode node);
    Object visit(BinaryExpression node);
    Object visit(UnaryExpressionNode node);   // <-- agregado
    Object visit(LiteralNode node);
    Object visit(IdentifierNode node);
    Object visit(CallNode node);

    // Nodos de control
    Object visit(IfNode node);
    Object visit(WhileNode node);
    Object visit(ReturnNode node);

    // Nodos de tipo o impresión
    Object visit(TypeNode node);
    Object visit(PrintNode node);

    // Nodos de statements generales
    Object visit(ExpressionStatementNode node);
}
