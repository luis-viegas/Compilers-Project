package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TypeVerification extends PostorderJmmVisitor<MySymbolTable,Boolean> {
    List<Report> reports;
    public TypeVerification() {
        reports = new ArrayList<>();
        addVisit("BinOp", this::BinOp);
        addVisit("ArrayAccess", this::ArrayAccess);
        addVisit("FunctionCall", this::FunctionCall);
        addVisit("ArrayAssignment",this::checkIntegerArrayAccess);
        addVisit("Assignment", this::checkAssignmentCompability);
        addVisit("Condition", this::checkIfCondition);
        addVisit("ReturnExpr", this::checkFunctionReturn);
    }

    private boolean BinOp(JmmNode node, MySymbolTable mySymbolTable) {
        checkOperations(node,mySymbolTable);
        checkArrayArithmetic(node, mySymbolTable);
        return true;
    }

    private Boolean FunctionCall(JmmNode node, MySymbolTable mySymbolTable) {
        checkExtends(node,mySymbolTable);
        checkMethodParametersCompatibility(node,mySymbolTable);
        return true;
    }

    private Boolean ArrayAccess(JmmNode node, MySymbolTable mySymbolTable) {
        checkArrayAccessInt(node,mySymbolTable);
        checkArrayAccess(node,mySymbolTable);
        return true;
    }

    private Type getNodeType(JmmNode node) {
        String tipo = node.get("tipo");
        String isArray = node.get("isArray");
        Boolean arrayBool = false;
        if (isArray.equals("true")) arrayBool=true;
        Type returnType = new Type(tipo,arrayBool);
        return returnType;

    }

    enum OpType {
        andOp,
        lessOp,
        arithmeticOp,
        INITIAL
    }

    private OpType getOpType(JmmNode node ) {
        OpType opType;
        if(node.get("op").equals("initial")){
            opType = OpType.INITIAL;
        } else if(node.get("op").equals("and")) {
            opType = OpType.andOp;
        } else if(node.get("op").equals("less")) {
            opType = OpType.lessOp;
        } else {opType = OpType.arithmeticOp;}
        return opType;
    }

    private OpType getOpType(String op ) {
        OpType opType;
        if(op.equals("initial")){
            opType = OpType.INITIAL;
        } else if(op.equals("and")) {
            opType = OpType.andOp;
        } else if(op.equals("less")) {
            opType = OpType.lessOp;
        } else {opType = OpType.arithmeticOp;}
        return opType;
    }

    private Boolean checkOperations(JmmNode node, MySymbolTable mySymbolTable) {

        if(checkOperationsRecursive(node, "initial", mySymbolTable)) {
            return true;
        } else {
            reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")), "Operands have different type", null));
            return false;
        }
    }

    private Boolean checkOperatorCompability(OpType op , OpType father) {
        switch (father) {
            case INITIAL:
                return true;
            case andOp:
                if (op == OpType.arithmeticOp) return false;
                else return true;
            case lessOp:
                if(op == OpType.lessOp) return false;
                else return true;
            case arithmeticOp:
                if(op == OpType.arithmeticOp) return true;
                else return false;
        }
        return true;
    }

    private Boolean checkOperationsRecursive(JmmNode node, String op, MySymbolTable mySymbolTable) {

        if(!checkOperatorCompability(getOpType(node), getOpType(op))) {
            return false;
        }

        var op1 = node.getJmmChild(0);
        var op2 = node.getJmmChild(1);
        List<JmmNode> ops = new ArrayList<>();
        ops.add(op1);
        ops.add(op2);

        for (JmmNode operator : ops) {
            switch (operator.getKind()) {
                case "IntLiteral":
                    if(getOpType(node) == OpType.andOp) {
                        return false;
                    }
                    break;
                case "BoolLiteral":
                    if(getOpType(node) == OpType.arithmeticOp) {
                        return false;
                    }
                    break;
                case "BinOp":
                    if(!checkOperationsRecursive(operator, node.get("op"), mySymbolTable)) return false;
                    break;
                case "Identifier":
                    if(!checkVarOperation(operator, node.get("op"), mySymbolTable)) return false;
                    break;
                case "FunctionCall":
                    if(!checkFuncOperation(operator,node.get("op"), mySymbolTable)) return false;
                    break;
                case "ArrayAccess":
                    if(!checkVarOperation(operator.getJmmChild(0), node.get("op"), mySymbolTable)) return false;
                    break;
            }
        }

        return true;
    }

    private boolean checkFuncOperation(JmmNode factor, String operator, MySymbolTable mySymbolTable) {
        String varType = mySymbolTable.getReturnType(factor.getJmmChild(1).get("id")).getName();
        switch(varType) {
            case "int":
                if(getOpType(operator) == OpType.andOp) {
                    return false;
                }
                break;
            case "boolean":
                if(getOpType(operator) == OpType.arithmeticOp) {
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    private boolean checkVarOperation(JmmNode factor, String operator, MySymbolTable mySymbolTable) {
        switch(getVarType(factor, mySymbolTable)) {
            case "int":
                if(getOpType(operator) == OpType.andOp) {
                    return false;
                }
                break;
            case "boolean":
                if(getOpType(operator) == OpType.arithmeticOp) {
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    private String getVarType(JmmNode node, MySymbolTable mySymbolTable) {
        var id = node.get("id");
        var methodName = node.getAncestor("Function").map(jmmNode -> jmmNode.get("functionName")).orElse("Error");
        List<Symbol> localVars = mySymbolTable.getLocalVariables(methodName);
        List<Symbol> params = mySymbolTable.getParameters(methodName);
        List<Symbol> fields = mySymbolTable.getFields();
        List<String> imports = mySymbolTable.getImports();
        for (Symbol symbol : localVars) {
            if(symbol.getName().equals(id)) {
                return symbol.getType().getName();
            }
        }
        for (Symbol symbol : params) {
            if(symbol.getName().equals(id)) {
                return symbol.getType().getName();
            }
        }
        for (Symbol symbol : fields) {
            if(symbol.getName().equals(id)) {
                return symbol.getType().getName();
            }
        }
        for (String importName : imports) {
            String[] impNames = importName.split("\\.");
            String lastImport = impNames[impNames.length-1];
            if(lastImport.equals(node.get("id"))) {
                return "imported";
            }
        }
        return "";
    }

    private Boolean checkArrayAccess(JmmNode node, MySymbolTable mySymbolTable) {

        if (node.getJmmChild(0).getKind().equals("ArrayAccess")){
            return true;
        }
        String id = node.getJmmChild(0).get("id");
        if (checkIfDeclared(node.getJmmChild(0),mySymbolTable)) {
            var methodName = node.getAncestor("Function").map(jmmNode -> jmmNode.get("functionName")).orElse("Error");
            List<Symbol> localVars = mySymbolTable.getLocalVariables(methodName);
            List<Symbol> params = mySymbolTable.getParameters(methodName);
            List<Symbol> fields = mySymbolTable.getFields();

            for (Symbol symbol : localVars) {
                if(symbol.getName().equals(id)) {
                    if(symbol.getType().isArray()) {
                        return true;
                    }
                }
            }
            for (Symbol symbol : params) {
                if(symbol.getName().equals(id)) {
                    if(symbol.getType().isArray()) {
                        return true;
                    }
                }
            }
            for (Symbol symbol : fields) {
                if(symbol.getName().equals(id)) {
                    if(symbol.getType().isArray()) {
                        return true;
                    }
                }
            }
            reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")), "You are triyng to access a variable that is not an array", null));
        }
        return false;
    }

    private Boolean checkArrayAccessInt(JmmNode node, MySymbolTable mySymbolTable)
    {
        var expression = node.getJmmChild(1);

        switch (expression.getKind()){
            case "IntLiteral":
                return true;
            case "Identifier":
                if (getVarType(expression,mySymbolTable).equals("int")) {
                    return true;
                }
                break;
            case "FunctionCall":
                if(mySymbolTable.getReturnType(expression.getJmmChild(1).get("id")).getName().equals("int")) {
                    return true;
                }
                break;
            case "BinOp":
                if(getOpType(expression) == OpType.arithmeticOp) return true;
                break;
        }
        reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")), "The expression you are using as index is not an integer", null));
        return false;
    }


    private Boolean checkIfDeclared(JmmNode node, MySymbolTable mySymbolTable) {
        var id = node.get("id");
        var methodName = node.getAncestor("Function").map(jmmNode -> jmmNode.get("functionName")).orElse("Error");
        List<Symbol> localVars = mySymbolTable.getLocalVariables(methodName);
        List<Symbol> params = mySymbolTable.getParameters(methodName);
        List<Symbol> fields = mySymbolTable.getFields();
        List<String> imports = mySymbolTable.getImports();
        for (Symbol symbol : localVars) {
            if(symbol.getName().equals(id)) {
                return true;
            }
        }
        for (Symbol symbol : params) {
            if(symbol.getName().equals(id)) {
                return true;
            }
        }
        for (Symbol symbol : fields) {
            if(symbol.getName().equals(id)) {
                return true;
            }
        }
        if(node.getAncestor("ExprStmt").isPresent()) {
            for (String importName : imports) {
                String[] impNames = importName.split("\\.");
                String lastImport = impNames[impNames.length-1];
                if(lastImport.equals(node.get("id"))) {
                    return true;
                }
            }

        }
        return false;
    }

    private Boolean checkArrayArithmetic(JmmNode node, MySymbolTable mySymbolTable) {
        for(JmmNode factorNode : node.getChildren()) {
            if(!factorNode.getKind().equals("Identifier")) {
                continue;
            }
            String id = factorNode.get("id");
            if (checkIfDeclared(factorNode,mySymbolTable)) {
                var methodName = factorNode.getAncestor("Function").map(jmmNode -> jmmNode.get("functionName")).orElse("Error");
                List<Symbol> localVars = mySymbolTable.getLocalVariables(methodName);
                List<Symbol> params = mySymbolTable.getParameters(methodName);
                List<Symbol> fields = mySymbolTable.getFields();

                for (Symbol symbol : localVars) {
                    if(symbol.getName().equals(id)) {
                        if(symbol.getType().isArray()) {
                            reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")), "Arrays cannot be used in arithmetic expressions", null));
                            return false;
                        }
                    }
                }
                for (Symbol symbol : params) {
                    if(symbol.getName().equals(id)) {
                        if(symbol.getType().isArray()) {
                            reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")), "Arrays cannot be used in arithmetic expressions", null));
                            return false;
                        }
                    }
                }
                for (Symbol symbol : fields) {
                    if (symbol.getName().equals(id)) {
                        if (symbol.getType().isArray()) {
                            reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")), "Arrays cannot be used in arithmetic expressions", null));
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private Boolean checkIntegerArrayAccess(JmmNode node, MySymbolTable mySymbolTable) { //TODO POINT 5 OF TYPE VERIFICATION
        List<JmmNode> nodes = node.getJmmChild(0).getChildren(); //get index
        var methodName = node.getAncestor("Function").map(jmmNode -> jmmNode.get("functionName")).orElse("Error");
        List<Symbol> localVars = mySymbolTable.getLocalVariables(methodName);
        List<Symbol> params = mySymbolTable.getParameters(methodName);
        List<Symbol> fields = mySymbolTable.getFields();

        if(nodes.get(0).getKind().equals("Identifier"))
        {
            String id = nodes.get(0).get("id");
            for (Symbol symbol : localVars) {
                if(symbol.getName().equals(id)) {
                    if(!symbol.getType().getName().equals("int")) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, "Identifier of array isnt int"));
                        return false;
                    }
                }
            }
            for (Symbol symbol : params) {
                if(symbol.getName().equals(id)) {
                    if(!symbol.getType().getName().equals("int")) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, "Identifier of array isnt int"));
                        return false;
                    }
                }
            }
            for (Symbol symbol : fields) {
                if(symbol.getName().equals(id)) {
                    if(!symbol.getType().getName().equals("int")) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, "Identifier of array isnt int"));
                        return false;
                    }
                }
            }

        }
        if(nodes.get(0).getKind().equals("BinOp"))
        {
            String id = nodes.get(0).getJmmChild(0).get("id");
            for (Symbol symbol : localVars) {
                if(symbol.getName().equals(id)) {
                    if(!symbol.getType().getName().equals("int")) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, "Identifier of array isnt int"));
                        return false;
                    }
                }
            }
            for (Symbol symbol : params) {
                if(symbol.getName().equals(id)) {
                    if(!symbol.getType().getName().equals("int")) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, "Identifier of array isnt int"));
                        return false;
                    }
                }
            }
            for (Symbol symbol : fields) {
                if(symbol.getName().equals(id)) {
                    if(!symbol.getType().getName().equals("int")) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, "Identifier of array isnt int"));
                        return false;
                    }
                }
            }

        }

        return true;
    }

    private Boolean checkAssignmentCompability(JmmNode node, MySymbolTable mySymbolTable) { //TODO POINT 6 OF TYPE VERIFICATION
        if (checkAssignmentCompabilityAux(node,mySymbolTable)) {
            return true;
        } else {
            reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")), "Type of the assignee must be compatible with the assigned", null));
            return false;
        }
    }



    private Boolean checkAssignmentCompabilityAux(JmmNode node, MySymbolTable mySymbolTable) {
        var type1 = getVarType(node.getJmmChild(0), mySymbolTable);
        var node2 = node.getJmmChild(1);
        switch (node2.getKind()) {
            case "Identifier":
                if(getVarType(node2,mySymbolTable).equals(type1)) {
                    return true;
                }
                if(getVarType(node2,mySymbolTable).equals("int") || getVarType(node2,mySymbolTable).equals("boolean")) {
                    return false;
                }
                if(isImported(node.getJmmChild(0),mySymbolTable)) {
                    return true;
                }
                if(mySymbolTable.getSuper().equals(getVarType(node2,mySymbolTable)) || mySymbolTable.getSuper().equals(getVarType(node.getJmmChild(0),mySymbolTable))) {
                    return true;
                }
                return false;
            case "IntLiteral":
                return type1.equals("int");
            case "BoolLiteral":
                return type1.equals("boolean");
            case "NewObject":
                return type1.equals(node2.getJmmChild(0).get("id"));
            case "BinOp":
                if(getOpType(node2) == OpType.arithmeticOp) {
                    return type1.equals("int");
                } else return type1.equals("boolean");

            case "inttArray":
                return type1.equals("int") ;
        }
        return false;
    }

    private Boolean isImported(JmmNode node, MySymbolTable mySymbolTable) {
        List<String> imports = mySymbolTable.getImports();
        for (String importName : imports) {
            String[] impNames = importName.split("\\.");
            String lastImport = impNames[impNames.length-1];
            if(lastImport.equals(node.get("id"))) {
                return true;
            }
        }
        return false;
    }

    private Boolean isImported(String type, MySymbolTable mySymbolTable) {
        List<String> imports = mySymbolTable.getImports();
        for (String importName : imports) {
            String[] impNames = importName.split("\\.");
            String lastImport = impNames[impNames.length-1];
            if(lastImport.equals(type)){
                return true;
            }
        }
        return false;
    }

    private Boolean checkMethodParametersCompatibility(JmmNode node, MySymbolTable mySymbolTable) {

        var methods = mySymbolTable.getMethods();

        for (String method : methods) {
            if(node.getJmmChild(1).get("id").equals(method)) {
                var parameters = mySymbolTable.getParameters(method);
                if(node.getJmmChild(2).getChildren().size() > 0) {
                    if(node.getJmmChild(2).getChildren().size() != parameters.size()) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1 , "incorrect number of arguments provided for function call"));
                        return false;
                    }
                    int count = 0;
                    for (JmmNode arg : node.getJmmChild(2).getChildren()) {
                        var id = "";
                        if(arg.getKind().equals("Identifier")) {id = arg.get("id");}
                        String tipo = "";
                        if(arg.getKind().equals("IntLiteral")) {tipo = "int";}
                        if(arg.getKind().equals("BoolLiteral")) {tipo = "bool";}
                        var methodName = node.getAncestor("Function").map(jmmNode -> jmmNode.get("functionName")).orElse("Error");
                        List<Symbol> localVars = mySymbolTable.getLocalVariables(methodName);
                        List<Symbol> params = mySymbolTable.getParameters(methodName);
                        List<Symbol> fields = mySymbolTable.getFields();

                        for (Symbol symbol : localVars) {
                            if(symbol.getName().equals(id)) {
                                tipo = symbol.getType().getName();
                            }
                        }
                        for (Symbol symbol : params) {
                            if(symbol.getName().equals(id)) {
                                tipo = symbol.getType().getName();
                            }
                        }
                        for (Symbol symbol : fields) {
                            if(symbol.getName().equals(id)) {
                                tipo = symbol.getType().getName();
                            }
                        }

                        if(!parameters.get(count).getType().getName().equals(tipo)) {
                            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1 , "Argument type different than expected"));
                            return false;
                        }
                        count++;
                    }

                } else {
                    if(parameters.size() != 0) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1 , "Incorrect number of arguments provided for function call"));
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private Boolean checkFunctionReturn(JmmNode node, MySymbolTable mySymbolTable) {
        if(checkFunctionReturnAux(node,mySymbolTable)){
            return true;
        }
        reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")), "Incorrect return type", null));
        return false;

    }

    private Boolean checkFunctionReturnAux(JmmNode node, MySymbolTable mySymbolTable) {
        var methodName = node.getAncestor("Function").map(jmmNode -> jmmNode.get("functionName")).orElse("Error");
        var expectedReturn = mySymbolTable.getReturnType(methodName).getName();
        var node2 = node.getJmmChild(0);
        switch (node2.getKind()) {
            case "Identifier":
                if(getVarType(node2,mySymbolTable).equals(expectedReturn)) {
                    return true;
                }
                return false;
            case "IntLiteral":
                return expectedReturn.equals("int");
            case "BoolLiteral":
                return expectedReturn.equals("boolean");
            case "NewObject":
                return expectedReturn.equals(node2.getJmmChild(0).get("id"));
            case "BinOp":
                if(getOpType(node2) == OpType.arithmeticOp) {
                    return expectedReturn.equals("int");
                } else return expectedReturn.equals("boolean");
            case "FunctionCall":
                if(isImported(getVarType(node.getJmmChild(0).getJmmChild(0),mySymbolTable),mySymbolTable)) {
                    return true;
                } else {
                    return expectedReturn.equals(mySymbolTable.getReturnType(methodName));
                }
        }
        return false;
    }

    private Boolean checkIfCondition(JmmNode node, MySymbolTable mySymbolTable) {
        if(checkIfConditionBoolean(node,mySymbolTable)) {
            return true;
        } else {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1 , "if condition is not a boolean expression"));
            return false;
        }
    }

    private Boolean checkIfConditionBoolean(JmmNode node, MySymbolTable mySymbolTable) { //TODO POINT 7 OF TYPE VERIFICATION


        if(node.getNumChildren() > 1) {
            if(node.getJmmChild(node.getNumChildren() - 1).getKind().equals("FunctionCall")){
                var funcCal = node.getJmmChild(node.getNumChildren() - 1);
                var funcName = funcCal.getJmmChild(0).get("id");
                if(mySymbolTable.returnType.containsKey(funcName)){
                    return mySymbolTable.getReturnType(funcName).getName().equals("boolean");
                }
                else return true;
            }
        }

        switch (node.getJmmChild(0).getKind()) {
            case "BinOp":
                if (node.getJmmChild(0).get("op").equals("and")) {
                    return true;
                }
                else return false;
            case "BoolLiteral":
                return true;
            case "Identifier":
                if (!checkVarBool(node.getJmmChild(0),mySymbolTable)) {
                    return false;
                }
                else return true;
            default:
                return false;
        }
    }

    private boolean checkVarBool(JmmNode node, MySymbolTable mySymbolTable) {
        var id = node.get("id");
        var methodName = node.getAncestor("Function").map(jmmNode -> jmmNode.get("functionName")).orElse("Error");
        List<Symbol> localVars = mySymbolTable.getLocalVariables(methodName);
        List<Symbol> params = mySymbolTable.getParameters(methodName);
        List<Symbol> fields = mySymbolTable.getFields();
        List<String> imports = mySymbolTable.getImports();
        for (Symbol symbol : localVars) {
            if(symbol.getName().equals(id)) {
                if(symbol.getType().getName().equals("boolean")) {
                    return true;
                }
            }
        }
        for (Symbol symbol : params) {
            if(symbol.getName().equals(id)) {
                if (symbol.getType().getName().equals("boolean")) {
                    return true;
                }
            }
        }
        for (Symbol symbol : fields) {
            if(symbol.getName().equals(id)) {
                if (symbol.getType().getName().equals("boolean")) {
                    return true;

                }
            }
        }
        for (String importName : imports) {
            String[] impNames = importName.split("\\.");
            String lastImport = impNames[impNames.length-1];
            if(lastImport.equals(node.get("id"))) {
                return true;
            }
        }

        return false;
    }

    private Boolean checkExtends(JmmNode node, MySymbolTable mySymbolTable) {


        var methods = mySymbolTable.getMethods();
        for (String method : methods) {
            if(node.getJmmChild(1).get("id").equals(method)) {
                return true;
            }
        }

        if(getVarType(node.getJmmChild(0),mySymbolTable).equals("imported")) {
            return true;
        }

        if(mySymbolTable.getSuper() != "") {
            return true;
        }

        reports.add(Report.newError(Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")), "Trying to call non existing method", null));
        return false;
    }

}

