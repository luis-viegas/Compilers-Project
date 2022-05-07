package pt.up.fe.comp;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class VisitorSymbolTable extends PreorderJmmVisitor<MySymbolTable, Boolean> {
    List<Report> reports;
    public VisitorSymbolTable() {
        reports = new ArrayList<>();
        addVisit("ImportDeclaration", this::visitImports);
    }

    private Boolean visitImports(JmmNode jmmNode, MySymbolTable mySymbolTable) {
        StringBuilder imports = new StringBuilder(jmmNode.get("name"));

        for (JmmNode node: jmmNode.getChildren()) {
            imports.append(".").append(node.get("name"));
        }

        mySymbolTable.addImport(imports.toString());
        return true;
    }

}
