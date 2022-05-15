package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast2jasmin.AstToJasmin;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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
        addVarName(semanticsResult);
        addHeaderName(semanticsResult);
        addMethodName(semanticsResult);

        return new JasminResult(semanticsResult.getSymbolTable().getClassName(), jasminCode.toString(), reports);
    }
    public void addHeaderName(JmmSemanticsResult semanticsResult)
    {
        jasminCode.append(".method public <init>()V\n" +
                "         aload_0\n" +
                "         invokespecial java/lang/Object/<init>()V\n" +
                "         return\n" +
                ".end method \n");
    }
    public void addVarName(JmmSemanticsResult semanticsResult)
    {
        List<Symbol> vars = semanticsResult.getSymbolTable().getFields();
        for(int i =0;i<vars.size();i++)
        {
            jasminCode.append(".field ");
            jasminCode.append(vars.get(i).getName());
            if(vars.get(i).getType().getName().equals("int"))
            {
                if(vars.get(i).getType().isArray())
                {
                    jasminCode.append(" [I \n");
                }
                else {jasminCode.append(" I\n");}
            }
            if(vars.get(i).getType().getName().equals("bool"))
            {
                if(vars.get(i).getType().isArray())
                {
                    jasminCode.append(" [B");
                }
                else {jasminCode.append(" B");}
            }
            if(vars.get(i).getType().getName().equals("String"))
            {
                if(vars.get(i).getType().isArray())
                {
                    jasminCode.append(" [S");
                }
                else {jasminCode.append(" S");}
            }
        }


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
        List<String> methods = semanticsResult.getSymbolTable().getMethods();
        // List<Method> methodsAux = semanticsResult.getSymbolTable().getMethodsAux();

        for(int i =0; i<methods.size();i++)
        {
            Type returnType =semanticsResult.getSymbolTable().getReturnType(methods.get(i));
            jasminCode.append(".method ");
            if(returnType.getName().equals("void")){jasminCode.append("static ");}
            jasminCode.append("public ");
            jasminCode.append(methods.get(i));
            jasminCode.append("( ");
            List<Symbol> parametros = semanticsResult.getSymbolTable().getParameters(methods.get(i));

            for(int j =0; j<parametros.size();j++)
            {
                if (parametros.get(j).getType().getName().equals("String"))
                {
                    jasminCode.append("[Ljava/lang/String; ");
                }

                if (parametros.get(j).getType().getName().equals("boolean"))
                {
                    jasminCode.append("B; ");
                }

                if (parametros.get(j).getType().getName().equals("int"))
                {
                    jasminCode.append("I; ");
                }
            }
            jasminCode.append(")");
            switch (returnType.getName())
            {

                case "string":
                    jasminCode.append("S");
                    break;

                case "bool":
                    jasminCode.append("B");
                    break;

                case "int":
                    jasminCode.append("I");
                    break;
                case "void":
                    jasminCode.append("V");
                    break;
            }


            jasminCode.append("\n");
            jasminCode.append(".end method\n");
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
