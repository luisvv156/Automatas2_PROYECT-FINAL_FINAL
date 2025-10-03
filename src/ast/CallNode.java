// CallNode.java (usar esta versión)
package ast;

import java.util.ArrayList;
import java.util.List;

public class CallNode extends ASTNode {
    private String functionName;
    private List<ASTNode> arguments;

    public CallNode(int lineNumber, String functionName) {
        super(lineNumber);
        this.functionName = functionName;
        this.arguments = new ArrayList<>();
    }

    public void addArgument(ASTNode argument) {
        arguments.add(argument);
    }

    public String getFunctionName() { return functionName; }
    public List<ASTNode> getArguments() { return arguments; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}