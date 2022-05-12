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
    }

    private Boolean visitVars(JmmNode jmmNode, MySymbolTable mySymbolTable)
    {
        String fieldName = jmmNode.get("name");
        String tipo = jmmNode.getJmmChild(0).get("tipo");
        Boolean isArray=false;
        if (tipo.equals("array")) isArray=true;
        Type type = new Type(tipo,isArray);
        mySymbolTable.setFields(new Symbol(type,fieldName));
        return true;
    }
    private Boolean visitClass(JmmNode jmmNode, MySymbolTable mySymbolTable) {
        mySymbolTable.setClassName(jmmNode.get("name"));
        jmmNode.getOptional("super").ifPresent(mySymbolTable::setSuper);
        return true;
    }

    private Boolean visitImports(JmmNode jmmNode, MySymbolTable mySymbolTable) {
        StringBuilder imports = new StringBuilder(jmmNode.get("name"));

        for (JmmNode node: jmmNode.getChildren()) {
            imports.append(".").append(node.get("name"));
        }

        mySymbolTable.addImport(imports.toString());
        return true;
    }
    private Boolean visitMethods(JmmNode jmmNode,MySymbolTable mySymbolTable)
    {
        for (JmmNode node: jmmNode.getChildren()) {

        }
        return true;
    }
}
