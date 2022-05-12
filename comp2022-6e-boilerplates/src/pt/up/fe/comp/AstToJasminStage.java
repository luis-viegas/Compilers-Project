package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast2jasmin.AstToJasmin;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;


public class AstToJasminStage implements AstToJasmin {
    StringBuilder jasminCode;
    List<Report> reports;
    public AstToJasminStage(){
        jasminCode = new StringBuilder();
        reports = new ArrayList<>();
    }
    @Override
    public JasminResult toJasmin(JmmSemanticsResult semanticsResult) {

        addImportName(semanticsResult);
        addSuperName(semanticsResult);
        addClassName(semanticsResult);
        addSuperName(semanticsResult);
        // addHeaderName(semanticsResult);
        //addVarName(semanticsResult);
        addMethodName(semanticsResult);

        return new JasminResult(semanticsResult.getSymbolTable().getClassName(), jasminCode.toString(), reports);
    }
    public void addHeaderName(JmmSemanticsResult semanticsResult)
    {
        jasminCode.append(".method public <init>()V\n" +
                "         aload_0\n" +
                "         invokespecial java/lang/Object/<init>()V\n" +
                "         return\n" +
                ".end method");
    }
    public void addVarName(JmmSemanticsResult semanticsResult)
    {
       /* List<Symbol> vars = semanticsResult.getSymbolTable().getFields();
        for(int i =0;i<vars.size();i++)
        {
        if(jasminCode.append(vars.get(i).getType().equals("int"))
        {
        jasminCode.append("aload"?);
        }
        }

    */

    }
    public void addImportName(JmmSemanticsResult semanticsResult){
        List<String> imports = semanticsResult.getSymbolTable().getImports();
        imports.add("");
        int i =0;
        while(!imports.get(i).equals("")) {
            jasminCode.append(".import ");
            jasminCode.append(imports.get(i));
            jasminCode.append("\n");
            i++;
        }
    }
    public void addClassName(JmmSemanticsResult semanticsResult){
        jasminCode.append(".class ");
        jasminCode.append(semanticsResult.getSymbolTable().getClassName());
        jasminCode.append("\n");
    }
    public void addMethodName(JmmSemanticsResult semanticsResult)
    {
        List<String> methods;
        methods = semanticsResult.getSymbolTable().getMethods();
        for(int i =0; i<methods.size();i++)
        {
        jasminCode.append(".method");
        jasminCode.append(methods.get(i));
        jasminCode.append("\n");
        }
    }
    public void addSuperName(JmmSemanticsResult semanticsResult){
        jasminCode.append(".super ");
        if (semanticsResult.getSymbolTable().getSuper().equals("")){
            jasminCode.append("java/lang/Object");
            jasminCode.append("\n");

        } else {
            jasminCode.append(semanticsResult.getSymbolTable().getSuper());
            jasminCode.append("\n");
        }
    }
}
