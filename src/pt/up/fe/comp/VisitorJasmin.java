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
import java.util.Map;

public class VisitorJasmin extends PreorderJmmVisitor<MySymbolTable, Boolean> {
    List<Report> reports;
    Map<String,List<JmmNode>> exprStmts;


    public VisitorJasmin() {
        reports = new ArrayList<>();
        addVisit("Function", this::visitFunction);
        exprStmts = new HashMap<>();

    }

    public Boolean visitFunction(JmmNode node, MySymbolTable mySymbolTable) {
        List<JmmNode> result = new ArrayList<>();
        for(JmmNode child : node.getChildren()) {
            if (child.getKind().equals("ExprStmt") || child.getKind().equals("Assignment") || child.getKind().equals("VarDeclaration")|| child.getKind().equals("IfStatement") || child.getKind().equals("ElseStmt") || child.getKind().equals("WhileStatement")) {
                result.add(child);
            }
        }
        exprStmts.put(node.get("functionName"),result);
        return true;
    }

    public List<JmmNode> getExprs(String nomeMethod) {
        return exprStmts.get(nomeMethod);
    }
}