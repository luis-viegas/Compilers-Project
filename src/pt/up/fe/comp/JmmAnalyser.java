package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;


public class JmmAnalyser implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        var mySymbolTable = new MySymbolTable();

        var visitorSymbolTable = new VisitorSymbolTable();
        var typeVerification = new TypeVerification();

        visitorSymbolTable.visit(parserResult.getRootNode(), mySymbolTable);
        typeVerification.visit(parserResult.getRootNode(), mySymbolTable);


        System.out.println(visitorSymbolTable.reports);
        System.out.println(typeVerification.reports);


        return new JmmSemanticsResult(parserResult, mySymbolTable, visitorSymbolTable.reports);
    }
}
