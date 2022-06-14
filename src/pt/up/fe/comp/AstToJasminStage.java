package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast2jasmin.AstToJasmin;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AstToJasminStage implements AstToJasmin {
    VisitorJasmin visitor = new VisitorJasmin();
    StringBuilder jasminCode;
    StringBuilder ifCode;
    List<Report> reports;
    List<Symbol> vars;
    int posVar = 0;
    Map<String,TypeR> registos;
    int iflabel = 0;
    int whileLabel = 0;

    public AstToJasminStage(){
        jasminCode = new StringBuilder();
        ifCode = new StringBuilder();
        reports = new ArrayList<>();
        registos  = new HashMap<>();

    }
    @Override
    public JasminResult toJasmin(JmmSemanticsResult semanticsResult) {

        visitor.visit(semanticsResult.getRootNode(), (MySymbolTable) semanticsResult.getSymbolTable());

        if(!semanticsResult.getSymbolTable().getMethods().contains("main")) posVar++;

        //addImportName(semanticsResult);
        //addSuperName(semanticsResult);
        addClassName(semanticsResult);
        addSuperName(semanticsResult);
        addVarName(semanticsResult);
        addHeaderName(semanticsResult);
        addMethodName(semanticsResult);
        addExpressions(semanticsResult);


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

        vars = semanticsResult.getSymbolTable().getFields();
        for(int i =0;i<vars.size();i++)
        {
            jasminCode.append(".field ");
            jasminCode.append(vars.get(i).getName());
            if(vars.get(i).getType().getName().equals("int"))
            {
                if(vars.get(i).getType().isArray())
                {
                    jasminCode.append(" [I").append("\n");
                }
                else {jasminCode.append(" I").append("\n");}
            }
            if(vars.get(i).getType().getName().equals("boolean"))
            {
                if(vars.get(i).getType().isArray())
                {
                    jasminCode.append(" [Z").append("\n");
                }
                else {jasminCode.append(" Z").append("\n");}
            }
            if(vars.get(i).getType().getName().equals("String"))
            {
                if(vars.get(i).getType().isArray())
                {
                    jasminCode.append(" [S").append("\n");
                }
                else {jasminCode.append(" S").append("\n");}
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
        System.out.println("ROOT NODE:");
        System.out.println(semanticsResult.getRootNode().getKind());
        for(int i =0; i<methods.size();i++)
        {
            Type returnType =semanticsResult.getSymbolTable().getReturnType(methods.get(i));
            jasminCode.append(".method ");
            jasminCode.append("public ");
            if(returnType.getName().equals("void")){jasminCode.append("static ");}
            jasminCode.append(methods.get(i));
            jasminCode.append("(");
            List<Symbol> parametros = semanticsResult.getSymbolTable().getParameters(methods.get(i));

            for(int j =0; j<parametros.size();j++)
            {
                if (parametros.get(j).getType().getName().equals("String"))
                {
                    jasminCode.append("[Ljava/lang/String;");
                    registos.put(parametros.get(j).getName(),new TypeR(posVar,parametros
                            .get(j).getType()));
                    posVar++;


                }

                if (parametros.get(j).getType().getName().equals("boolean"))
                {
                    jasminCode.append("Z");
                    registos.put(parametros.get(j).getName(),new TypeR(posVar,parametros
                            .get(j).getType()));
                    posVar++;
                }

                if (parametros.get(j).getType().getName().equals("int"))
                {
                    jasminCode.append("I");
                    registos.put(parametros.get(j).getName(),new TypeR(posVar,parametros
                            .get(j).getType()));
                    posVar++;
                }
            }
            jasminCode.append(")");
            switch (returnType.getName())
            {

                case "string":
                    jasminCode.append("S");
                    break;

                case "boolean":
                    jasminCode.append("Z");
                    break;

                case "int":
                    if(returnType.isArray()) {
                        jasminCode.append("[I");
                    } else{
                        jasminCode.append("I");
                    }
                    break;
                case "void":
                    jasminCode.append("V");
                    break;
                default:
                    jasminCode.append("L").append(returnType.getName()).append(";");
            }
            jasminCode.append("\n");
            jasminCode.append(".limit stack 98 \n");
            jasminCode.append(".limit locals 98 \n");
           List<JmmNode> expr = visitor.getExprs(methods.get(i));
            String  tipo = "";
            String isArray = "";
            for(JmmNode node : expr)
            {
                System.out.println(node.getKind());
                if(node.getKind().equals("VarDeclaration"))
                {
                    tipo=node.getJmmChild(0).get("tipo");
                    isArray  = node.getJmmChild(0).get("isArray");
                    switch (tipo)
                    {

                        case "int":
                           /*if(isArray.equals("false")) {jasminCode.append("istore_").append(posVar).append("\n");;}
                           else {jasminCode.append("iastore_").append(posVar).append("\n");}
                            System.out.println(node.get("id"));
                            */registos.put(node.get("id"),new TypeR(posVar,new Type(tipo,isArray.equals("true"))));
                           posVar++;
                           break;
                        case "boolean":
                           /* if(isArray.equals("false")) {jasminCode.append("bstore_").append(posVar).append("\n");;}
                            else {jasminCode.append("bastore_").append(posVar).append("\n");}
                            */registos.put(node.get("id"),new TypeR(posVar,new Type(tipo,isArray.equals("true"))));
                            posVar++;
                            break;
                        case "String":
                            /*if(isArray.equals("false")) {jasminCode.append("cstore_").append(posVar).append("\n");;}
                            else {jasminCode.append("castore_").append(posVar).append("\n");}
                            */registos.put(node.get("id"),new TypeR(posVar,new Type(tipo,isArray.equals("true"))));
                            posVar++;
                            break;

                    }
                }
                else if(node.getKind().equals("IfStatement"))
                {
                    if(node.getJmmChild(0).getJmmChild(0).getKind().equals("Identifier"))
                    {
                        StringBuilder code = new StringBuilder();
                        jasminCode.append("iload_").append(registos.get(node.getJmmChild(0).getJmmChild(0).get("id")).getPost()).append("\n");
                        jasminCode.append("ifeq Label").append(iflabel).append("\n").append("\n");
                        code.append("Label").append(iflabel).append(":").append("\n");
                        int finalabel = iflabel;
                        if(node.getJmmChild(1).getJmmChild(0).getKind().equals("ScopeStmt")) {
                            visitExpr(node.getJmmChild(1).getJmmChild(0).getChildren(),jasminCode,semanticsResult);
                        } else {
                            visitExpr(node.getJmmChild(1).getChildren(),jasminCode,semanticsResult);
                        }
                        jasminCode.append("goto LabelEndIf").append(finalabel).append("\n");
                        if(node.getJmmChild(1).getJmmChild(0).getKind().equals("ScopeStmt")) {
                            visitExpr(node.getJmmChild(2).getJmmChild(0).getChildren(),code,semanticsResult);
                        } else {
                            visitExpr(node.getJmmChild(2).getChildren(),code,semanticsResult);
                        }
                        jasminCode.append(code);
                        jasminCode.append("LabelEndIf").append(finalabel).append(":").append("\n");
                        iflabel++;

                    }
                    else if(node.getJmmChild(0).getJmmChild(0).getKind().equals("BoolLiteral"))
                    {
                        if(node.getJmmChild(0).getJmmChild(0).get("value").equals("true"))
                        {
                            StringBuilder code = new StringBuilder();
                            jasminCode.append("iconst_1").append("\n");
                            jasminCode.append("ifeq Label").append(iflabel).append("\n");
                            code.append("Label").append(iflabel).append(":").append("\n");
                            int finalabel = iflabel;
                            if(node.getJmmChild(1).getJmmChild(0).getKind().equals("ScopeStmt")) {
                                visitExpr(node.getJmmChild(1).getJmmChild(0).getChildren(),jasminCode,semanticsResult);
                            } else {
                                visitExpr(node.getJmmChild(1).getChildren(),jasminCode,semanticsResult);
                            }
                            jasminCode.append("goto LabelEndIf").append(finalabel).append("\n");
                            if(node.getJmmChild(1).getJmmChild(0).getKind().equals("ScopeStmt")) {
                                visitExpr(node.getJmmChild(2).getJmmChild(0).getChildren(),code,semanticsResult);
                            } else {
                                visitExpr(node.getJmmChild(2).getChildren(),code,semanticsResult);
                            }
                            jasminCode.append(code);
                            jasminCode.append("LabelEndIf").append(finalabel).append(":").append("\n");
                            iflabel++;
                        }
                        else {
                            StringBuilder code = new StringBuilder();
                            jasminCode.append("iconst_0").append("\n");
                            jasminCode.append("ifeq Label").append(iflabel).append("\n").append("\n");
                            code.append("Label").append(iflabel).append(":").append("\n");
                            int finalabel = iflabel;
                            if(node.getJmmChild(1).getJmmChild(0).getKind().equals("ScopeStmt")) {
                                visitExpr(node.getJmmChild(1).getJmmChild(0).getChildren(),jasminCode,semanticsResult);
                            } else {
                                visitExpr(node.getJmmChild(1).getChildren(),jasminCode,semanticsResult);
                            }
                            jasminCode.append("goto LabelEndIf").append(finalabel).append("\n");
                            if(node.getJmmChild(1).getJmmChild(0).getKind().equals("ScopeStmt")) {
                                visitExpr(node.getJmmChild(2).getJmmChild(0).getChildren(),code,semanticsResult);
                            } else {
                                visitExpr(node.getJmmChild(2).getChildren(),code,semanticsResult);
                            }
                            jasminCode.append(code);
                            jasminCode.append("LabelEndIf").append(finalabel).append(":").append("\n");
                            iflabel++;
                        }
                    }
                    else if(node.getJmmChild(0).getJmmChild(0).getKind().equals("BinOp"))
                    {
                        int binresult= BinOp(node.getJmmChild(0).getJmmChild(0),registos,semanticsResult);
                        if(binresult==1)
                        {jasminCode.append("iload_1").append("\n");
                        }
                        else if(binresult==-1)
                        {jasminCode.append("iload_0").append("\n");}
                        else if(binresult==0)
                        {
                            jasminCode.append("if_icmpge Label").append(iflabel).append("\n");
                        }
                        StringBuilder code = new StringBuilder();
                        StringBuilder code1 = new StringBuilder();
                        code.append("Label").append(iflabel).append(":").append("\n");
                        int finalabel = iflabel;
                        if(node.getJmmChild(1).getJmmChild(0).getKind().equals("ScopeStmt")) {
                            visitExpr(node.getJmmChild(1).getJmmChild(0).getChildren(),jasminCode,semanticsResult);
                        } else {
                            visitExpr(node.getJmmChild(1).getChildren(),jasminCode,semanticsResult);
                        }
                        jasminCode.append("goto Label9").append(finalabel).append("\n");
                        if(node.getJmmChild(1).getJmmChild(0).getKind().equals("ScopeStmt")) {
                            visitExpr(node.getJmmChild(2).getJmmChild(0).getChildren(),code,semanticsResult);
                        } else {
                            visitExpr(node.getJmmChild(2).getChildren(),code,semanticsResult);
                        }
                        jasminCode.append(code);
                        jasminCode.append("Label9").append(finalabel).append(":").append("\n");
                        iflabel++;
                    }

                }
                else if ( node.getKind().equals("Assignment"))
                {
                    if(node.getJmmChild(1).getKind().equals("IntLiteral"))
                    {
                        TypeR tipoR = registos.get(node.getJmmChild(0).get("id"));

                        jasminCode.append("ldc ").append(node.getJmmChild(1).get("value")).append("\n");
                        jasminCode.append("istore_").append(tipoR.getPost()).append("\n");
                    }
                    if(node.getJmmChild(1).getKind().equals("BinOp"))
                    {

                       /* if(node.getJmmChild(1).getJmmChild(0).equals("BoolLiteral"))
                        {
                            if(node.getJmmChild(1).getJmmChild(1).equals("BoolLiteral"))
                            {
                                if(node.getJmmChild(1).get("op").equals("and"))
                                {
                                    boolean second;
                                    boolean first;

                                    if(node.getJmmChild(1).getJmmChild(0).get("value").equals("true"))
                                    {
                                        first = true;
                                    }
                                    else
                                    {
                                         first = false;
                                    }

                                    if(node.getJmmChild(1).getJmmChild(1).get("value").equals("true"))
                                    {
                                         second = true;
                                    }
                                    else
                                    {
                                        second = false;
                                    }
                                    if(first && second)
                                    {
                                        jasminCode.append("iconst_1");
                                    }
                                    else
                                    {
                                        jasminCode.append("iconst_0");
                                    }
                                }

                            }
                        }
                        */





                        TypeR tipoR = registos.get(node.getJmmChild(0).get("id"));
                        int bin= BinOp(node.getJmmChild(1),registos,semanticsResult);

                            jasminCode.append("istore_").append(tipoR.getPost()).append("\n");

                    }
                    if(node.getJmmChild(1).getKind().equals("Identifier"))
                    {
                        TypeR tipoR = registos.get(node.getJmmChild(0).get("id"));
                        TypeR tipo1R = registos.get(node.getJmmChild(1).get("id"));
                        if(tipo1R==null)
                        {
                            for(int k=0;k<vars.size();k++)
                            {
                                if(vars.get(k).getName().equals(node.getJmmChild(1).get("id")))
                                {
                                    jasminCode.append("getfield ").append(semanticsResult.getSymbolTable().getClassName()).append("/").append(vars.get(k).getName());
                                    if(vars.get(k).getType().getName().equals("int"))
                                    {jasminCode.append(" I\n");}
                                    if(vars.get(k).getType().getName().equals("String"))
                                    {jasminCode.append(" S\n");}
                                    if(vars.get(k).getType().getName().equals("bool"))
                                    {jasminCode.append(" B\n");}

                                }
                            }
                        }
                        jasminCode.append("istore_").append(tipoR.getPost()).append("\n");

                    }
                }
                else if(node.getKind().equals("WhileStatement")) {
                    int currentWhileLabel = whileLabel;
                    whileLabel++;
                    jasminCode.append("WhileLabel").append(currentWhileLabel).append(":").append("\n");
                    if(node.getJmmChild(0).getJmmChild(0).getKind().equals("Identifier"))
                    {
                        jasminCode.append("iload_").append(registos.get(node.getJmmChild(0).getJmmChild(0).get("id")).getPost()).append("\n");
                        jasminCode.append("ifeq EndWhile").append(currentWhileLabel).append("\n").append("\n");
                    }
                    else if(node.getJmmChild(0).getJmmChild(0).getKind().equals("BoolLiteral")) {
                        if(node.getJmmChild(0).getJmmChild(0).get("value").equals("true"))
                        {
                            jasminCode.append("iconst_1").append("\n");
                            jasminCode.append("ifeq EndWhile").append(currentWhileLabel).append("\n").append("\n");
                        }
                        else {
                            jasminCode.append("iconst_0").append("\n");
                            jasminCode.append("ifeq EndWhile").append(currentWhileLabel).append("\n").append("\n");
                        }
                    }
                    else if(node.getJmmChild(0).getJmmChild(0).getKind().equals("BinOp"))
                    {
                        int binresult= BinOp(node.getJmmChild(0).getJmmChild(0),registos,semanticsResult);
                        if(binresult==1)
                        {jasminCode.append("iload_1").append("\n");
                        }
                        else if(binresult==-1)
                        {jasminCode.append("iload_0").append("\n");}
                        else if(binresult==0)
                        {
                            jasminCode.append("if_icmpge EndWhile").append(currentWhileLabel).append("\n").append("\n");

                        }
                    }
                    visitExpr(node.getJmmChild(1).getChildren(),jasminCode, semanticsResult);
                    jasminCode.append("goto WhileLabel").append(currentWhileLabel).append("\n");
                    jasminCode.append("EndWhile").append(currentWhileLabel).append(":").append("\n");


                }
                else if(node.getKind().equals("ExprStmt"))
                {
                    for(JmmNode iter : node.getJmmChild(0).getJmmChild(2).getChildren())
                    {
                        if(iter.getKind().equals("IntLiteral"))
                        {
                            jasminCode.append("ldc ").append(iter.get("value \n"));
                        }
                        else
                        {
                            TypeR Rtype = registos.get(iter.get("id"));
                            switch (Rtype.getTipo().getName())
                            {
                                case "int":
                                    jasminCode.append("iload_").append(Rtype.getPost()).append("\n");
                                    break;
                                case "bool":
                                    jasminCode.append("zload ").append(Rtype.getPost()).append("\n");
                                    break;
                                case "String":
                                    jasminCode.append("sload ").append(Rtype.getPost()).append("\n");
                                    break;
                                default: break;
                            }
                        }
                    }

                        if(node.getJmmChild(0).getJmmChild(0).getKind().equals("ThisPointer"))
                           {jasminCode.append("invokestatic this.");}
                           else
                           {jasminCode.append("invokestatic ").append(node.getJmmChild(0).getJmmChild(0).get("id")).append(".");
                           }
                           jasminCode.append(node.getJmmChild(0).getJmmChild(1).get("id"));
                           jasminCode.append("(");
                              for(JmmNode iter : node.getJmmChild(0).getJmmChild(2).getChildren())
                               {
                                   if(iter.getKind().equals("IntLiteral"))
                                   {
                                       jasminCode.append("I");

                                   }
                                   else {
                                   TypeR tipoR = registos.get(iter.get("id"));
                                   if(registos.get(iter.get("id"))==null)
                                   {
                                       for(int k =0;k<vars.size();k++)
                                       {
                                           if(vars.get(k).getName().equals(iter.get("id")))
                                           {
                                               if(vars.get(k).getType().getName().equals("int"))
                                               {jasminCode.append("I)");}

                                               if(vars.get(k).getType().getName().equals("bool"))
                                               {jasminCode.append("B)");}

                                               if(vars.get(k).getType().getName().equals("String"))
                                               {jasminCode.append("S)");}

                                           }
                                       }
                                   }
                                   else{
                                   switch (tipoR.getTipo().getName())
                                   {
                                       case "int":
                                           jasminCode.append("I");
                                           break;
                                       case "bool":
                                           jasminCode.append("B");
                                           break;
                                       case "String":
                                           jasminCode.append("Ljava/lang/String");
                                           break;
                                       default: break;
                                   }
                               }}
                           }
                           jasminCode.append(")");
                    boolean encontrou= false;
                    for(String nome : semanticsResult.getSymbolTable().getMethods())
                    {
                        if(nome.equals(node.getJmmChild(0).getJmmChild(1).get("id")))
                        {
                            encontrou = true;
                            switch ( semanticsResult.getSymbolTable().getReturnType(nome).getName())
                            {

                                case "string":
                                    jasminCode.append("S").append("\n");
                                    break;

                                case "bool":
                                    jasminCode.append("B").append("\n");
                                    break;

                                case "int":
                                    jasminCode.append("I").append("\n");
                                    break;
                                case "void":
                                    jasminCode.append("V").append("\n");
                                    break;
                            }
                        }
                    }
                    if(!encontrou)
                    {
                        jasminCode.append("V").append("\n");
                    }




                }
                else if(node.getKind().equals("ReturnExpr")) {
                     if (semanticsResult.getSymbolTable().getReturnType(methods.get(i)).getName().equals("int"))
                    {
                        if(node.getJmmChild(0).getKind().equals("IntLiteral")){
                            jasminCode.append("ldc ").append(node.getJmmChild(0).get("value")).append("\n");
                        } else {
                            jasminCode.append("iload_").append(registos.get(node.getJmmChild(0).get("id")).getPost()).append("\n");
                        }
                    }
                }
            }
            if(semanticsResult.getSymbolTable().getReturnType(methods.get(i)).getName().equals("int"))
            {
                jasminCode.append("ireturn \n");
            }
            else
            {jasminCode.append("return \n");}
            jasminCode.append("\n");
            jasminCode.append(".end method\n");
        }
    }
    public int BinOp(JmmNode node, Map<String, TypeR> registos, JmmSemanticsResult semanticsResult)
    {
        TypeR typeR2=null;
        TypeR typeR1 = null;
        int returned1 = 1;
        int returned2 = 1;
        if(node.getJmmChild(0).getKind().equals("BinOp"))
        {returned1 = BinOp(node.getJmmChild(0), registos,semanticsResult);}
        else {
            if (node.getJmmChild(0).getKind().equals("IntLiteral")) {
                jasminCode.append("ldc ").append(node.getJmmChild(0).get("value")).append("\n");
            }
            else if (node.getJmmChild(0).getKind().equals("Identifier")) {
                typeR1 = registos.get(node.getJmmChild(0).get("id"));
                if(typeR1 == null)
                {
                for(int k = 0;k<vars.size();k++)
                {
                    if(vars.get(k).getName().equals(node.getJmmChild(0).get("id")))
                    {
                        jasminCode.append("getfield ").append(semanticsResult.getSymbolTable().getClassName()).append("/").append(vars.get(k).getName());
                        if(vars.get(k).getType().getName().equals("int"))
                        {jasminCode.append(" I\n");}
                        if(vars.get(k).getType().getName().equals("String"))
                        {jasminCode.append(" S\n");}
                        if(vars.get(k).getType().getName().equals("bool"))
                        {jasminCode.append(" B\n");}
                    }
                }

                }
                else{
                switch (typeR1.getTipo().getName()) {

                    case "int":
                        if (!typeR1.getTipo().isArray()) {
                            jasminCode.append("iload_").append(typeR1.getPost()).append("\n");

                        } else {
                            jasminCode.append("iastore ").append(typeR1.getPost()).append("\n");
                        }
                        break;
                    default:
                        break;
                }
            }}
        }
        if(node.getJmmChild(1).getKind().equals("BinOp"))
        {
            returned2 = BinOp(node.getJmmChild(1), registos,semanticsResult);
        }
        else if(node.getJmmChild(1).getKind().equals("IntLiteral"))
        {
            jasminCode.append("ldc ").append(node.getJmmChild(1).get("value")).append("\n");
        }
        else if(node.getJmmChild(1).getKind().equals("Identifier"))
        {
            typeR2 = registos.get(node.getJmmChild(1).get("id"));
            if(typeR2 == null)
            {
                for(int k = 0;k<vars.size();k++)
                {
                    if(vars.get(k).getName().equals(node.getJmmChild(0).get("id")))
                    {
                        jasminCode.append("getfield ").append(semanticsResult.getSymbolTable().getClassName()).append("/").append(vars.get(k).getName());
                        if(vars.get(k).getType().getName().equals("int"))
                        {jasminCode.append(" I\n");}
                        if(vars.get(k).getType().getName().equals("String"))
                        {jasminCode.append(" S\n");}
                        if(vars.get(k).getType().getName().equals("bool"))
                        {jasminCode.append(" B\n");}
                    }
                }

            }
            else{
                switch (typeR2.getTipo().getName())
                {

                    case "int":
                        if(!typeR2.getTipo().isArray()) {jasminCode.append("iload_").append(typeR2.getPost()).append("\n");;}
                        else {jasminCode.append("iastore_").append(typeR2.getPost()).append("\n");}
                        break;
                    default: break;
                }
            }
        }
        String op = node.get("op");
        switch (op)
        {
            case "add":
                jasminCode.append("iadd \n");
                break;


            case "subtract":
                jasminCode.append("isub \n");
                break;

            case "multiplication":
                jasminCode.append("imul \n");
                break;

            case "division":
                jasminCode.append("idiv \n");
                break;
            case "less":
               if(node.getJmmChild(0).getKind().equals("IntLiteral") && node.getJmmChild(1).getKind().equals("IntLiteral")){
                   if(Integer.parseInt(node.getJmmChild(0).get("value"))<Integer.parseInt(node.getJmmChild(1).get("value")))
                {
                    return 1;
                }
                else {
                    return -1;
                }}
               else if(node.getJmmChild(0).getKind().equals("Identifier") && node.getJmmChild(1).getKind().equals("Identifier"))
               {
                   if(typeR1.getTipo().getName().equals("int")&&typeR2.getTipo().getName().equals("int"))
                   {
                       return 0;
                   }
               }
               else if(node.getJmmChild(0).getKind().equals("Identifier") && node.getJmmChild(1).getKind().equals("IntLiteral"))
               {
                   return 0;
               }
               else if(node.getJmmChild(0).getKind().equals("IntLiteral") && node.getJmmChild(1).getKind().equals("Identifier"))
               {
                   return 0;
               }
               break;
            case "and":
                boolean second=true;
                boolean first=true;
                if(!node.getJmmChild(0).getKind().equals("BinOp")) {
                    if (node.getJmmChild(0).get("value").equals("true")) {
                        first = true;
                    } else {
                        first = false;
                    }
                }

                if(node.getJmmChild(1).get("value").equals("true"))
                {
                    second = true;
                }
                else
                {
                    second = false;
                }
                if(first && second && returned1>-1 && returned2> -1)
                {
                    jasminCode.append("iconst_1 \n");
                    return 1;
                }
                else
                {
                    jasminCode.append("iconst_0 \n");
                    return -1;
                }




            default: break;
        }
    return 1;
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

    public void addExpressions(JmmSemanticsResult semanticsResult) {
        var methods = semanticsResult.getSymbolTable().getMethods();

        for (String nomeMethod : methods) {
            System.out.println(nomeMethod);
            System.out.println(visitor.getExprs(nomeMethod));
        }

    }

    public void visitExpr(List<JmmNode> list, StringBuilder jasminCode, JmmSemanticsResult semanticsResult)
    {

        String tipo;
        String isArray;
        for(JmmNode node : list)
        {
            if(node.getKind().equals("VarDeclaration"))
            {
                tipo = node.getJmmChild(0).get("tipo");
                isArray = node.getJmmChild(0).get("isArray");
                switch (tipo)
                {

                    case "int":
                           /*if(isArray.equals("false")) {jasminCode.append("istore_").append(posVar).append("\n");;}
                           else {jasminCode.append("iastore_").append(posVar).append("\n");}
                            System.out.println(node.get("id"));
                            */registos.put(node.get("id"),new TypeR(posVar,new Type(tipo,isArray.equals("true"))));
                        posVar++;
                        break;
                    case "boolean":
                           /* if(isArray.equals("false")) {jasminCode.append("bstore_").append(posVar).append("\n");;}
                            else {jasminCode.append("bastore_").append(posVar).append("\n");}
                            */registos.put(node.get("id"),new TypeR(posVar,new Type(tipo,isArray.equals("true"))));
                        posVar++;
                        break;
                    case "String":
                            /*if(isArray.equals("false")) {jasminCode.append("cstore_").append(posVar).append("\n");;}
                            else {jasminCode.append("castore_").append(posVar).append("\n");}
                            */registos.put(node.get("id"),new TypeR(posVar,new Type(tipo,isArray.equals("true"))));
                        posVar++;
                        break;
                    default:
                        registos.put(node.get("id"),new TypeR(posVar,new Type(tipo,isArray.equals("true"))));
                        posVar++;
                        break;
                }
            }
            else if(node.getKind().equals("IfStatement"))
            {
                if(node.getJmmChild(0).getJmmChild(0).getKind().equals("Identifier"))
                {
                    StringBuilder code = new StringBuilder();
                    jasminCode.append("iload_").append(registos.get(node.getJmmChild(0).getJmmChild(0).get("id")).getPost()).append("\n");
                    jasminCode.append("ifeq Label").append(iflabel).append("\n").append("\n");
                    code.append("Label").append(iflabel).append(":").append("\n");
                    int finalabel = iflabel;
                    if(node.getJmmChild(1).getJmmChild(0).getKind().equals("ScopeStmt")) {
                        visitExpr(node.getJmmChild(1).getJmmChild(0).getChildren(),jasminCode,semanticsResult);
                    } else {
                        visitExpr(node.getJmmChild(1).getChildren(),jasminCode,semanticsResult);
                    }
                    jasminCode.append("goto LabelEndIf").append(finalabel).append("\n");
                    if(node.getJmmChild(1).getJmmChild(0).getKind().equals("ScopeStmt")) {
                        visitExpr(node.getJmmChild(2).getJmmChild(0).getChildren(),code,semanticsResult);
                    } else {
                        visitExpr(node.getJmmChild(2).getChildren(),code,semanticsResult);
                    }
                    jasminCode.append(code);
                    jasminCode.append("LabelEndIf").append(finalabel).append(":").append("\n");
                    iflabel++;

                }
                else if(node.getJmmChild(0).getJmmChild(0).getKind().equals("BoolLiteral"))
                {
                    if(node.getJmmChild(0).getJmmChild(0).get("value").equals("true"))
                    {
                        StringBuilder code = new StringBuilder();
                        jasminCode.append("iconst_1").append("\n");
                        jasminCode.append("ifeq Label").append(iflabel).append("\n");
                        code.append("Label").append(iflabel).append(":").append("\n");
                        int finalabel = iflabel;
                        if(node.getJmmChild(1).getJmmChild(0).getKind().equals("ScopeStmt")) {
                            visitExpr(node.getJmmChild(1).getJmmChild(0).getChildren(),jasminCode,semanticsResult);
                        } else {
                            visitExpr(node.getJmmChild(1).getChildren(),jasminCode,semanticsResult);
                        }
                        jasminCode.append("goto LabelEndIf").append(finalabel).append("\n");
                        if(node.getJmmChild(1).getJmmChild(0).getKind().equals("ScopeStmt")) {
                            visitExpr(node.getJmmChild(2).getJmmChild(0).getChildren(),code,semanticsResult);
                        } else {
                            visitExpr(node.getJmmChild(2).getChildren(),code,semanticsResult);
                        }
                        jasminCode.append(code);
                        jasminCode.append("LabelEndIf").append(finalabel).append(":").append("\n");
                        iflabel++;
                    }
                    else {
                        StringBuilder code = new StringBuilder();
                        jasminCode.append("iconst_0").append("\n");
                        jasminCode.append("ifeq Label").append(iflabel).append("\n").append("\n");
                        code.append("Label").append(iflabel).append(":").append("\n");
                        int finalabel = iflabel;
                        if(node.getJmmChild(1).getJmmChild(0).getKind().equals("ScopeStmt")) {
                            visitExpr(node.getJmmChild(1).getJmmChild(0).getChildren(),jasminCode,semanticsResult);
                        } else {
                            visitExpr(node.getJmmChild(1).getChildren(),jasminCode,semanticsResult);
                        }
                        jasminCode.append("goto LabelEndIf").append(finalabel).append("\n");
                        if(node.getJmmChild(1).getJmmChild(0).getKind().equals("ScopeStmt")) {
                            visitExpr(node.getJmmChild(2).getJmmChild(0).getChildren(),code,semanticsResult);
                        } else {
                            visitExpr(node.getJmmChild(2).getChildren(),code,semanticsResult);
                        }
                        jasminCode.append(code);
                        jasminCode.append("LabelEndIf").append(finalabel).append(":").append("\n");
                        iflabel++;
                    }
                }
                else if(node.getJmmChild(0).getJmmChild(0).getKind().equals("BinOp"))
                {
                    int binresult= BinOp(node.getJmmChild(0).getJmmChild(0),registos,semanticsResult);
                    if(binresult==1)
                    {jasminCode.append("iload_1").append("\n");
                    }
                    else if(binresult==-1)
                    {jasminCode.append("iload_0").append("\n");}
                    else if(binresult==0)
                    {
                        jasminCode.append("if_icmpge Label").append(iflabel).append("\n");

                    }
                    StringBuilder code = new StringBuilder();
                    StringBuilder code1 = new StringBuilder();
                   code.append("Label").append(iflabel).append(":").append("\n");
                    int finalabel = iflabel;
                    if(node.getJmmChild(1).getJmmChild(0).getKind().equals("ScopeStmt")) {
                        visitExpr(node.getJmmChild(1).getJmmChild(0).getChildren(),jasminCode,semanticsResult);
                    } else {
                        visitExpr(node.getJmmChild(1).getChildren(),jasminCode,semanticsResult);
                    }
                    jasminCode.append("goto LabelEndIf").append(finalabel).append("\n");
                    if(node.getJmmChild(1).getJmmChild(0).getKind().equals("ScopeStmt")) {
                        visitExpr(node.getJmmChild(2).getJmmChild(0).getChildren(),code,semanticsResult);
                    } else {
                        visitExpr(node.getJmmChild(2).getChildren(),code,semanticsResult);
                    }
                    jasminCode.append(code);
                    jasminCode.append("LabelEndIf").append(finalabel).append(":").append("\n");
                    iflabel++;
                }
            }
            else if ( node.getKind().equals("Assignment"))
            {
                if(node.getJmmChild(1).getKind().equals("IntLiteral"))
                {
                    TypeR tipoR = registos.get(node.getJmmChild(0).get("id"));

                    jasminCode.append("ldc ").append(node.getJmmChild(1).get("value")).append("\n");
                    jasminCode.append("istore_").append(tipoR.getPost()).append("\n");
                }
                if(node.getJmmChild(1).getKind().equals("NewObject"))
                {
                    TypeR tipoR = registos.get(node.getJmmChild(0).get("id"));

                    jasminCode.append("ldc ").append(node.getJmmChild(1).get("value")).append("\n");
                    jasminCode.append("astore_").append(tipoR.getPost()).append("\n");
                }
                if(node.getJmmChild(1).getKind().equals("BinOp"))
                {

                       /* if(node.getJmmChild(1).getJmmChild(0).equals("BoolLiteral"))
                        {
                            if(node.getJmmChild(1).getJmmChild(1).equals("BoolLiteral"))
                            {
                                if(node.getJmmChild(1).get("op").equals("and"))
                                {
                                    boolean second;
                                    boolean first;

                                    if(node.getJmmChild(1).getJmmChild(0).get("value").equals("true"))
                                    {
                                        first = true;
                                    }
                                    else
                                    {
                                         first = false;
                                    }

                                    if(node.getJmmChild(1).getJmmChild(1).get("value").equals("true"))
                                    {
                                         second = true;
                                    }
                                    else
                                    {
                                        second = false;
                                    }
                                    if(first && second)
                                    {
                                        jasminCode.append("iconst_1");
                                    }
                                    else
                                    {
                                        jasminCode.append("iconst_0");
                                    }
                                }

                            }
                        }
                        */





                    TypeR tipoR = registos.get(node.getJmmChild(0).get("id"));
                    int bin= BinOp(node.getJmmChild(1),registos,semanticsResult);

                    jasminCode.append("istore_").append(tipoR.getPost()).append("\n");

                }
                if(node.getJmmChild(1).getKind().equals("Identifier"))
                {
                    TypeR tipoR = registos.get(node.getJmmChild(0).get("id"));
                    TypeR tipo1R = registos.get(node.getJmmChild(1).get("id"));
                    if(tipo1R==null)
                    {
                        for(int k=0;k<vars.size();k++)
                        {
                            if(vars.get(k).getName().equals(node.getJmmChild(1).get("id")))
                            {
                                jasminCode.append("getfield ").append(semanticsResult.getSymbolTable().getClassName()).append("/").append(vars.get(k).getName());
                                if(vars.get(k).getType().getName().equals("int"))
                                {jasminCode.append(" I\n");}
                                if(vars.get(k).getType().getName().equals("String"))
                                {jasminCode.append(" S\n");}
                                if(vars.get(k).getType().getName().equals("bool"))
                                {jasminCode.append(" B\n");}

                            }
                        }
                    }
                    jasminCode.append("istore_").append(tipoR.getPost()).append("\n");

                }
            }
            else if(node.getKind().equals("ExprStmt"))
            {
                for(JmmNode iter : node.getJmmChild(0).getJmmChild(2).getChildren())
                {
                    if(iter.getKind().equals("IntLiteral"))
                    {
                        jasminCode.append("ldc ").append(iter.get("value")).append("\n");
                    }
                    else
                    {
                        TypeR Rtype = registos.get(iter.get("id"));
                        switch (Rtype.getTipo().getName())
                        {
                            case "int":
                                jasminCode.append("iload_").append(Rtype.getPost()).append("\n");
                                break;
                            case "bool":
                                jasminCode.append("zload ").append(Rtype.getPost()).append("\n");
                                break;
                            case "String":
                                jasminCode.append("sload ").append(Rtype.getPost()).append("\n");
                                break;
                            default: break;
                        }
                    }
                }

                if(node.getJmmChild(0).getJmmChild(0).getKind().equals("ThisPointer"))
                {jasminCode.append("invokestatic this.");}
                else
                {jasminCode.append("invokestatic ").append(node.getJmmChild(0).getJmmChild(0).get("id")).append(".");
                }
                jasminCode.append(node.getJmmChild(0).getJmmChild(1).get("id"));
                jasminCode.append("(");
                for(JmmNode iter : node.getJmmChild(0).getJmmChild(2).getChildren())
                {
                    if(iter.getKind().equals("IntLiteral"))
                    {
                        jasminCode.append("I");

                    }
                    else {
                        TypeR tipoR = registos.get(iter.get("id"));
                        if(registos.get(iter.get("id"))==null)
                        {
                            for(int k =0;k<vars.size();k++)
                            {
                                if(vars.get(k).getName().equals(iter.get("id")))
                                {
                                    if(vars.get(k).getType().getName().equals("int"))
                                    {jasminCode.append("I)");}

                                    if(vars.get(k).getType().getName().equals("bool"))
                                    {jasminCode.append("B)");}

                                    if(vars.get(k).getType().getName().equals("String"))
                                    {jasminCode.append("S)");}

                                }
                            }
                        }
                        else{
                            switch (tipoR.getTipo().getName())
                            {
                                case "int":
                                    jasminCode.append("I");
                                    break;
                                case "bool":
                                    jasminCode.append("B");
                                    break;
                                case "String":
                                    jasminCode.append("Ljava/lang/String");
                                    break;
                                default: break;
                            }
                        }}
                }
                jasminCode.append(")");
                boolean encontrou= false;
                for(String nome : semanticsResult.getSymbolTable().getMethods())
                {
                    if(nome.equals(node.getJmmChild(0).getJmmChild(1).get("id")))
                    {
                        encontrou = true;
                        switch ( semanticsResult.getSymbolTable().getReturnType(nome).getName())
                        {

                            case "string":
                                jasminCode.append("S").append("\n");
                                break;

                            case "bool":
                                jasminCode.append("B").append("\n");
                                break;

                            case "int":
                                jasminCode.append("I").append("\n");
                                break;
                            case "void":
                                jasminCode.append("V").append("\n");
                                break;
                        }
                    }
                }
                if(!encontrou)
                {
                    jasminCode.append("V").append("\n");
                }



            }
            else if(node.getKind().equals("WhileStatement")) {
                int currentWhileLabel = whileLabel;
                whileLabel++;
                jasminCode.append("WhileLabel").append(currentWhileLabel).append(":").append("\n");
                if(node.getJmmChild(0).getJmmChild(0).getKind().equals("Identifier"))
                {
                    jasminCode.append("iload_").append(registos.get(node.getJmmChild(0).getJmmChild(0).get("id")).getPost()).append("\n");
                    jasminCode.append("ifeq EndWhile").append(currentWhileLabel).append("\n").append("\n");
                }
                else if(node.getJmmChild(0).getJmmChild(0).getKind().equals("BoolLiteral")) {
                    if(node.getJmmChild(0).getJmmChild(0).get("value").equals("true"))
                    {
                        jasminCode.append("iconst_1").append("\n");
                        jasminCode.append("ifeq EndWhile").append(currentWhileLabel).append("\n").append("\n");
                    }
                    else {
                        jasminCode.append("iconst_0").append("\n");
                        jasminCode.append("ifeq EndWhile").append(currentWhileLabel).append("\n").append("\n");
                    }
                }
                else if(node.getJmmChild(0).getJmmChild(0).getKind().equals("BinOp"))
                {
                    int binresult= BinOp(node.getJmmChild(0).getJmmChild(0),registos,semanticsResult);
                    if(binresult==1)
                    {jasminCode.append("iload_1").append("\n");
                    }
                    else if(binresult==-1)
                    {jasminCode.append("iload_0").append("\n");}
                    else if(binresult==0)
                    {
                        jasminCode.append("if_icmpge EndWhile").append(currentWhileLabel).append("\n").append("\n");

                    }
                }
                visitExpr(node.getJmmChild(1).getChildren(),jasminCode, semanticsResult);
                jasminCode.append("goto WhileLabel").append(currentWhileLabel).append("\n");
                jasminCode.append("EndWhile").append(currentWhileLabel).append(":").append("\n");
            }
        }
    }
}
