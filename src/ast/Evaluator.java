package ast;
public interface Evaluator {
    // Métodos existentes
    Object evaluate(AssignmentNode node);
    Object evaluate(BinaryExpression node);
    Object evaluate(BlockNode node);
    Object evaluate(CallNode node);
    Object evaluate(ExpressionStatementNode node);
    Object evaluate(FunctionNode node);
    Object evaluate(IdentifierNode node);
    Object evaluate(IfNode node);
    Object evaluate(LiteralNode node);
    Object evaluate(PrintNode node);
    Object evaluate(ProgramNode node);
    Object evaluate(ReturnNode node);
    Object evaluate(TypeNode node);
    Object evaluate(VariableDeclNode node);
    Object evaluate(WhileNode node);
    Object evaluate(UnaryExpressionNode node);
    
    // NUEVOS: Métodos para arrays
    Object evaluate(ArrayNode node);
    Object evaluate(ArrayAccessNode node);
}