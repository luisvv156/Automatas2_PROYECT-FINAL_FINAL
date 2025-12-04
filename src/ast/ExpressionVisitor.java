package ast;

public interface ExpressionVisitor {
    // Nodos de declaraciones y bloques
    Object visit(BlockNode node);
    Object visit(FunctionNode node);          // útil si quieres inferir tipos de funciones
    Object visit(VariableDeclNode node);

    // NUEVO: Nodos para clases
    Object visit(ClassDeclNode node);
    Object visit(MethodDeclNode node);
    Object visit(ClassInstanceNode node);
    Object visit(FieldAccessNode node);
    Object visit(MethodCallNode node);

    // Nodos de expresiones
    Object visit(AssignmentNode node);
    Object visit(BinaryExpression node);
    Object visit(UnaryExpressionNode node);
    Object visit(LiteralNode node);
    Object visit(IdentifierNode node);
    Object visit(CallNode node);

    // Arrays
    Object visit(ArrayNode node);
    Object visit(ArrayAccessNode node);

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