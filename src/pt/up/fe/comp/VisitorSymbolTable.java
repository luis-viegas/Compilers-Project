package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VisitorSymbolTable extends PreorderJmmVisitor<MySymbolTable, Boolean> {
    List<Report> reports;
    public VisitorSymbolTable() {
        reports = new ArrayList<>();
        addVisit("ImportDeclaration", this::visitImports);
        addVisit("ClassDeclaration", this::visitClass);
        addVisit("VarDeclaration", this::visitFields);
        addVisit("Function",this::visitMethods);
        addVisit("Identifier", this::reportIfNotDeclared);
    }

    private boolean reportIfNotDeclared(JmmNode node, MySymbolTable mySymbolTable) {
        if (!checkIfDeclared(node,mySymbolTable)) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, -1 , "Var is not declared"));
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
                    return true;
                }
            }

        }
        if(node.getAncestor("NewObject").isPresent()) {
            return true;
        }

        return false;
    }

    private Boolean visitClass(JmmNode jmmNode, MySymbolTable mySymbolTable) {
        mySymbolTable.setClassName(jmmNode.get("className"));
        jmmNode.getOptional("classExtends").ifPresent(mySymbolTable::setSuper);
        return true;
    }

    private Boolean visitImports(JmmNode jmmNode, MySymbolTable mySymbolTable) {
        StringBuilder imports = new StringBuilder();
        Boolean first = true;
        for (JmmNode node: jmmNode.getChildren()) {
            if(first) {
                imports.append(node.get("value"));
                first = !first;
            }
            else {
                imports.append(".").append(node.get("value"));
            }
        }

        mySymbolTable.addImport(imports.toString());
        return true;
    }

    private Boolean visitFields(JmmNode jmmNode, MySymbolTable mySymbolTable)
    {
        if(!jmmNode.getAncestor("Function").isPresent()) {
            String fieldName = jmmNode.get("id");
            Type type = getNodeType(jmmNode.getJmmChild(0));
            mySymbolTable.setFields(new Symbol(type,fieldName));
        }
        return true;
    }

    private Boolean visitMethods(JmmNode jmmNode,MySymbolTable mySymbolTable)
    {
        String funcName = jmmNode.get("functionName");
        mySymbolTable.setMethods(funcName);

        Type returnType = getNodeType(jmmNode.getJmmChild(0));
        mySymbolTable.addReturnType(funcName, returnType);

        List<Symbol> params = new ArrayList<>();
        List<Symbol> local_vars = new ArrayList<>();

        for (JmmNode node: jmmNode.getChildren()) {
            if(node.getKind().equals("param")) {
                String fieldName = node.get("id");
                Type type = getNodeType(node.getJmmChild(0));

                Symbol simbolo= new Symbol(type,fieldName);
                params.add(simbolo);

            } else if (node.getKind().equals("VarDeclaration")) {
                String fieldName = node.get("id");
                Type type = getNodeType(node.getJmmChild(0));

                Symbol simbolo= new Symbol(type,fieldName);
                local_vars.add(simbolo);
            }

        }

        mySymbolTable.addParameters(funcName,params);
        mySymbolTable.addLocalVariables(funcName,local_vars);


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






}
