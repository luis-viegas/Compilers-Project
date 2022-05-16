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
        addVisit("BinOp", this::checkOperations);
        addVisit("ArrayAccess", this::checkArrayAccess);
        addVisit("ArrayAccess", this::checkArrayAccessInt);
        addVisit("BinOp", this::checkArrayArithmetic);
        addVisit("FunctionCall", this::checkExtends);
        addVisit("FunctionCall", this::checkMethodParametersCompatibility);
        addVisit("BinOp", this::checkOperations);
        addVisit("ArrayAssignment",this::checkIntegerArrayAccess);
        addVisit("Assignment", this::checkAssignmentCompability);
    }

    private Type getNodeType(JmmNode node) {
        String tipo = node.get("tipo");
        String isArray = node.get("isArray");
        Boolean arrayBool = false;
        if (isArray.equals("true")) arrayBool=true;
        Type returnType = new Type(tipo,arrayBool);
        return returnType;

    }

    private Boolean checkOperations(JmmNode node, MySymbolTable mySymbolTable) {  //TODO POINT 2 OF TYPE VERIFICATION
        var methodName = node.getAncestor("Function").map(jmmNode -> jmmNode.get("functionName")).orElse("Error");
        String op1 = "";
        String op2 = "";
        String type1 = "";
        String type2 = "";
        if(node.getJmmChild(1).getKind().equals("IntLiteral")){type2 =  "int";}
        else{op2 = node.getJmmChild(1).get("id");}
        if(node.getJmmChild(0).getKind().equals("IntLiteral")){type1 =  "int";}
        else{op1 = node.getJmmChild(0).get("id");}
        List<Symbol> localVars = mySymbolTable.getLocalVariables(methodName);
        List<Symbol> params = mySymbolTable.getParameters(methodName);
        List<Symbol> fields = mySymbolTable.getFields();

        for (Symbol symbol : localVars) {
            if(symbol.getName().equals(op1)) {
                type1 = symbol.getType().getName();
                if(symbol.getType().isArray()) {type1 += "A";}
            }
        }
        for (Symbol symbol : params) {
            if(symbol.getName().equals(op1)) {
                type1 = symbol.getType().getName();
                if(symbol.getType().isArray()) {type1 += "A";}
            }
        }
        for (Symbol symbol : fields) {
            if(symbol.getName().equals(op1)) {
                type1 = symbol.getType().getName();
                if(symbol.getType().isArray()) {type1 += "A";}
            }
        }
        for (Symbol symbol : localVars) {
            if(symbol.getName().equals(op2)) {
                type2 = symbol.getType().getName();
                if(symbol.getType().isArray()) {type2 += "A";}
            }
        }
        for (Symbol symbol : params) {
            if(symbol.getName().equals(op2)) {
                type2 = symbol.getType().getName();
                if(symbol.getType().isArray()) {type2 += "A";}
            }
        }
        for (Symbol symbol : fields) {
            if(symbol.getName().equals(op2)) {
                type2 = symbol.getType().getName();
                if(symbol.getType().isArray()) {type2 += "A";}
            }
        }
        if(!type1.equals(type2))
        {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1 , "Operands have different type"));
            return false;
        }
        return true;
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
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1 , "Trying to access an array but var is not an array"));
        }
        return false;
    }
    private Boolean checkArrayAccessInt(JmmNode node, MySymbolTable mySymbolTable)
    {

        String id2 = "";
        if(node.getJmmChild(0).getKind().equals("Identifier")&&node.getJmmChild(1).getKind().equals("Identifier"))
        {
            id2=node.getJmmChild(1).get("id");
        }
        var methodName = node.getAncestor("Function").map(jmmNode -> jmmNode.get("functionName")).orElse("Error");
        List<Symbol> localVars = mySymbolTable.getLocalVariables(methodName);
        List<Symbol> params = mySymbolTable.getParameters(methodName);
        List<Symbol> fields = mySymbolTable.getFields();

        for (Symbol symbol : localVars) {
            if(symbol.getName().equals(id2)) {
                if(symbol.getType().isArray() || (!symbol.getType().getName().equals("int"))) {
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1 , "Trying to access an array but not valid access int"));
                    return false;
                }

            }
        }
        for (Symbol symbol : params) {
            if(symbol.getName().equals(id2)) {

                if(symbol.getType().isArray() || (!symbol.getType().getName().equals("int"))) {
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1 , "Trying to access an array but not valid access int"));
                    return false;
                }
            }
        }
        for (Symbol symbol : fields) {
            if(symbol.getName().equals(id2)) {

                if(symbol.getType().isArray() || (!symbol.getType().getName().equals("int"))) {
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1 , "Trying to access an array but not valid access int"));
                    return false;
                }
            }
        }
        return true;
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
                    System.out.println("entered if");
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
                            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1 , "Arrays cannot be used in arithmetic expressions"));
                            return false;
                        }
                    }
                }
                for (Symbol symbol : params) {
                    if(symbol.getName().equals(id)) {
                        if(symbol.getType().isArray()) {
                            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1 , "Arrays cannot be used in arithmetic expressions"));
                            return false;
                        }
                    }
                }
                for (Symbol symbol : fields) {
                    if (symbol.getName().equals(id)) {
                        if (symbol.getType().isArray()) {
                            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, "Arrays cannot be used in arithmetic expressions"));
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
        var methodName = node.getAncestor("Function").map(jmmNode -> jmmNode.get("functionName")).orElse("Error");
        List<Symbol> localVars = mySymbolTable.getLocalVariables(methodName);
        List<Symbol> params = mySymbolTable.getParameters(methodName);
        List<Symbol> fields = mySymbolTable.getFields();
        String id1 = node.getJmmChild(0).get("id");
        String id2 = "";
        String type1 = "";
        String type2 = "";
        if(node.getJmmChild(1).getKind().equals("Identifier"))
        {
            id2 = node.getJmmChild(1).get("id");
        }
        if(node.getJmmChild(1).getKind().equals("IntLiteral"))
        {
            type2= "int";
        }
        else if(node.getJmmChild(1).getKind().equals("BinOp"))
        {
            id2 = node.getJmmChild(1).getJmmChild(0).get("id");
        }
        for (Symbol symbol : localVars) {
            if(symbol.getName().equals(id1)) {
                type1 = symbol.getType().getName();
                }
            }

        for (Symbol symbol : params) {
            if(symbol.getName().equals(id1)) {
                type1 = symbol.getType().getName();
            }
        }
        for (Symbol symbol : fields) {
            if(symbol.getName().equals(id1)) {
                type1 = symbol.getType().getName();
            }
        }
        for (Symbol symbol : localVars) {
            if(symbol.getName().equals(id2)) {
                type2 = symbol.getType().getName();
            }
        }

        for (Symbol symbol : params) {
            if(symbol.getName().equals(id2)) {
                type2 = symbol.getType().getName();
            }
        }
        for (Symbol symbol : fields) {
            if(symbol.getName().equals(id2)) {
                type2 = symbol.getType().getName();
            }
        }

        if(!type1.equals(type2))
        {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, "Assignement of vars with different types!"));
            return false;
        }
        return true;
    }

    private Boolean checkMethodParametersCompatibility(JmmNode node, MySymbolTable mySymbolTable) {

        var methods = mySymbolTable.getMethods();

        for (String method : methods) {
            if(node.getJmmChild(0).get("id").equals(method)) {
                var parameters = mySymbolTable.getParameters(method);
                if(node.getChildren().size() > 1) {
                    if(node.getJmmChild(1).getChildren().size() != parameters.size()) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1 , "incorrect number of arguments provided for function call"));
                        return false;
                    }
                    int count = 0;
                    for (JmmNode arg : node.getJmmChild(1).getChildren()) {
                        var id = arg.get("id");
                        String tipo = "";
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
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1 , "incorrect number of arguments provided for function call"));
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private Boolean checkIfConditionBoolean(JmmNode node, MySymbolTable mySymbolTable) { //TODO POINT 7 OF TYPE VERIFICATION
        return true;
    }

    private Boolean checkExtends(JmmNode node, MySymbolTable mySymbolTable) {


        var methods = mySymbolTable.getMethods();

        for (String method : methods) {
            if(node.getJmmChild(0).get("id").equals(method)) {
                return true;
            }
        }

        if(mySymbolTable.getSuper().equals("")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1 , "Trying to call non existing method"));
            return false;
        }



        return true;
    }
}
