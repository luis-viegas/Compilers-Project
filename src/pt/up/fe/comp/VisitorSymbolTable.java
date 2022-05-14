package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;

public class VisitorSymbolTable extends PreorderJmmVisitor<MySymbolTable, Boolean> {
    List<Report> reports;
    public VisitorSymbolTable() {
        reports = new ArrayList<>();
        addVisit("ImportDeclaration", this::visitImports);
        addVisit("ClassDeclaration", this::visitClass);
        addVisit("VarDeclaration", this::visitVars);
        addVisit("Statement",this::visitStatement);
<<<<<<< HEAD
        addVisit("Function",this::visitMethods);
        //addVisit("param", this::visitParam);
=======
        addVisit("MethodDeclaration",this::visitMethods);
>>>>>>> d37a8d251b8d61ee51f00326ae2a368a35f91947
    }

    /*private Boolean visitParam(JmmNode jmmNode, MySymbolTable mySymbolTable)
    {
        String fieldName = jmmNode.get("parameters");
        String tipo = jmmNode.getJmmChild(0).get("tipo");
        String isArray = jmmNode.getJmmChild(0).get("isArray");
        Boolean arrayBool = false;
        if (isArray.equals("true")) arrayBool=true;
        Type type = new Type(tipo,arrayBool);
        List<Symbol> listSimbol = null;
        listSimbol.add(new Symbol(type,fieldName));
        mySymbolTable.setParameters();
        return true;
    }*/
    private Boolean visitStatement(JmmNode jmmNode,MySymbolTable mySymbolTable)
    {

        return true;
    }

    private Boolean visitVars(JmmNode jmmNode, MySymbolTable mySymbolTable)
    {
        String fieldName = jmmNode.get("id");
        String tipo = jmmNode.getJmmChild(0).get("tipo");
        String isArray = jmmNode.getJmmChild(0).get("isArray");
        Boolean arrayBool = false;
        if (isArray.equals("true")) arrayBool=true;
        Type type = new Type(tipo,arrayBool);
        mySymbolTable.setFields(new Symbol(type,fieldName));
        return true;
    }
    private Boolean visitClass(JmmNode jmmNode, MySymbolTable mySymbolTable) {
        mySymbolTable.setClassName(jmmNode.get("className"));
        jmmNode.getOptional("super").ifPresent(mySymbolTable::setSuper);
        return true;
    }

    private Boolean visitImports(JmmNode jmmNode, MySymbolTable mySymbolTable) {
        StringBuilder imports = new StringBuilder();
        int i = 0;
        for (JmmNode node: jmmNode.getChildren()) {
           if(i==0)
           { imports.append(node.get("value"));
           i++;}
           else {
               imports.append(".").append(node.get("value"));
           }
        }

        mySymbolTable.addImport(imports.toString());
        return true;
    }

    private Boolean visitMethods(JmmNode jmmNode,MySymbolTable mySymbolTable)
    {
        StringBuilder methods = new StringBuilder();
        methods.append(jmmNode.get("functionName")).append(";").append(jmmNode.get("Return")).append(";").append(jmmNode.get("isStatic"));
        mySymbolTable.setMethods(String.valueOf(methods));

        /*for (JmmNode node: jmmNode.getChildren()) {
            if(node.getKind().equals("param"))
            {
                String fieldName = jmmNode.get("parameters");
                String tipo = jmmNode.getJmmChild(0).get("tipo");
                String isArray = jmmNode.getJmmChild(0).get("isArray");
                Boolean arrayBool = false;
                if (isArray.equals("true")) arrayBool=true;
                Type type = new Type(tipo,arrayBool);
                Symbol simbolo= new Symbol(type,fieldName);
                mySymbolTable.setParameters(fieldName, simbolo);

            }
            else {visit(node);}
            methods.append(node.getKind()).append(" ");
        } */
        return true;
    }
}
