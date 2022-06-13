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

    public AstToJasminStage(){
        jasminCode = new StringBuilder();
        ifCode = new StringBuilder();
        reports = new ArrayList<>();
        registos  = new HashMap<>();

    }
    @Override
    public JasminResult toJasmin(JmmSemanticsResult semanticsResult) {

        visitor.visit(semanticsResult.getRootNode(), (MySymbolTable) semanticsResult.getSymbolTable());

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
                    jasminCode.append("B; ");
                    registos.put(parametros.get(j).getName(),new TypeR(posVar,parametros
                            .get(j).getType()));
                    posVar++;
                }

                if (parametros.get(j).getType().getName().equals("int"))
                {
                    jasminCode.append("I; ");
                    registos.put(parametros.get(j).getName(),new TypeR(posVar,parametros
                            .get(j).getType()));
                    posVar++;
                }
            }
            jasminCode.append(")");
            switch (returnType.getName())
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
            jasminCode.append(".limit stack 99 \n");
            jasminCode.append(".limit locals 99 \n");
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
                        visitExpr(node.getJmmChild(1).getJmmChild(0).getChildren(),jasminCode, semanticsResult);
                        jasminCode.append("goto LabelEndIf").append(finalabel).append("\n");
                        visitExpr(node.getJmmChild(2).getJmmChild(0).getChildren(),code, semanticsResult);
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

                        jasminCode.append("bipush ").append(node.getJmmChild(1).get("value")).append("\n");
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
                        boolean bin = BinOp(node.getJmmChild(1),registos,semanticsResult);

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
                            jasminCode.append("bipush ").append(iter.get("value \n"));
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
            }
            if(semanticsResult.getSymbolTable().getReturnType(methods.get(i)).getName().equals("int"))
            {jasminCode.append("ireturn \n");}
            else
            {jasminCode.append("return \n");}
            jasminCode.append("\n");
            jasminCode.append(".end method\n");
        }
    }
    public boolean BinOp(JmmNode node, Map<String, TypeR> registos, JmmSemanticsResult semanticsResult)
    {
        boolean returned = true;
        if(node.getJmmChild(0).getKind().equals("BinOp"))
        {returned = BinOp(node.getJmmChild(0), registos,semanticsResult);}
        else {
            if (node.getJmmChild(0).getKind().equals("IntLiteral")) {
                jasminCode.append("bipush ").append(node.getJmmChild(0).get("value")).append("\n");
            }
            else if (node.getJmmChild(0).getKind().equals("Identifier")) {
                TypeR typeR = registos.get(node.getJmmChild(0).get("id"));
                if(typeR == null)
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
                switch (typeR.getTipo().getName()) {

                    case "int":
                        if (!typeR.getTipo().isArray()) {
                            jasminCode.append("iload_").append(typeR.getPost()).append("\n");

                        } else {
                            jasminCode.append("iastore ").append(typeR.getPost()).append("\n");
                        }
                        break;
                    default:
                        break;
                }
            }}
        }

            if(node.getJmmChild(1).getKind().equals("IntLiteral"))
            {
                jasminCode.append("bipush ").append(node.getJmmChild(1).get("value")).append("\n");
            }
            else if(node.getJmmChild(1).getKind().equals("Identifier"))
            {
                TypeR typeR = registos.get(node.getJmmChild(1).get("id"));
                if(typeR == null)
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
                switch (typeR.getTipo().getName())
                {

                    case "int":
                        if(!typeR.getTipo().isArray()) {jasminCode.append("iload_").append(typeR.getPost()).append("\n");;}
                        else {jasminCode.append("iastore_").append(typeR.getPost()).append("\n");}
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
                if(Integer.parseInt(node.getJmmChild(0).get("value"))<Integer.parseInt(node.getJmmChild(1).get("value")))
                {
                    return true;
                }
                else {
                    return false;
                }
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
                if(first && second && returned)
                {
                    jasminCode.append("iconst_1 \n");
                    return true;
                }
                else
                {
                    jasminCode.append("iconst_0 \n");
                    return false;
                }




            default: break;
        }
    return true;
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

                }
            }
            else if(node.getKind().equals("IfStatement"))
            {
                if(node.getJmmChild(0).getJmmChild(0).getKind().equals("Identifier"))
                {
                    StringBuilder code = new StringBuilder();
                    StringBuilder code1 = new StringBuilder();
                    jasminCode.append("iload_").append(registos.get(node.getJmmChild(0).getJmmChild(0).get("id")).getPost()).append("\n");
                    jasminCode.append("ifeq Label").append(iflabel).append("\n");
                    code.append("Label").append(iflabel).append(":").append("\n");
                    int finalabel = iflabel;
                    visitExpr(node.getJmmChild(1).getJmmChild(0).getChildren(),jasminCode,semanticsResult);
                    jasminCode.append("goto Label9").append(finalabel).append(":").append("\n");
                    visitExpr(node.getJmmChild(2).getJmmChild(0).getChildren(),code,semanticsResult);
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

                    jasminCode.append("bipush ").append(node.getJmmChild(1).get("value")).append("\n");
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
                    boolean bin = BinOp(node.getJmmChild(1),registos,semanticsResult);

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
                        jasminCode.append("bipush ").append(iter.get("value")).append("\n");
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
        }
    }
}
