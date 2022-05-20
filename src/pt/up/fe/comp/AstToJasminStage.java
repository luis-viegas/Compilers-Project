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
    List<Report> reports;


    public AstToJasminStage(){
        jasminCode = new StringBuilder();
        reports = new ArrayList<>();
    }
    @Override
    public JasminResult toJasmin(JmmSemanticsResult semanticsResult) {

        visitor.visit(semanticsResult.getRootNode(), (MySymbolTable) semanticsResult.getSymbolTable());

        addImportName(semanticsResult);
        addSuperName(semanticsResult);
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
        System.out.println("ROOT NODE:");

        System.out.println(semanticsResult.getRootNode().getKind());
        for(int i =0; i<methods.size();i++)
        {
            int posVar = 0;
            Map<String,TypeR> registos = new HashMap<>();
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

           List<JmmNode> expr = visitor.getExprs(methods.get(i));
            String  tipo = "";
            String isArray = "";
            for(JmmNode node : expr)
            {
                if(node.getKind().equals("VarDeclaration"))
                {
                    tipo=node.getJmmChild(0).get("tipo");
                    isArray  = node.getJmmChild(0).get("isArray");
                    switch (tipo)
                    {

                        case "int":
                           if(isArray.equals("false")) {jasminCode.append("istore_").append(posVar).append("\n");;}
                           else {jasminCode.append("iastore_").append(posVar).append("\n");}
                            registos.put(node.get("id"),new TypeR(posVar,new Type(tipo,isArray.equals("true"))));
                           posVar++;
                           break;
                        case "boolean":
                            if(isArray.equals("false")) {jasminCode.append("bstore_").append(posVar).append("\n");;}
                            else {jasminCode.append("bastore_").append(posVar).append("\n");}
                            registos.put(node.get("id"),new TypeR(posVar,new Type(tipo,isArray.equals("true"))));
                            posVar++;
                            break;
                        case "String":
                            if(isArray.equals("false")) {jasminCode.append("cstore_").append(posVar).append("\n");;}
                            else {jasminCode.append("castore_").append(posVar).append("\n");}
                            registos.put(node.get("id"),new TypeR(posVar,new Type(tipo,isArray.equals("true"))));
                            posVar++;
                            break;

                    }
                }
                else if ( node.getKind().equals("Assignment"))
                {
                    if(node.getJmmChild(1).getKind().equals("IntLiteral"))
                    {
                        TypeR tipoR = registos.get(node.getJmmChild(0).get("id"));

                        jasminCode.append("iconst_").append(node.getJmmChild(1).get("value")).append("\n");
                        jasminCode.append("istore ").append(tipoR.getPost()).append("\n");
                    }
                    if(node.getJmmChild(1).getKind().equals("BinOp"))
                    {
                        TypeR tipoR = registos.get(node.getJmmChild(0).get("id"));
                        BinOp(node.getJmmChild(1),registos);
                        jasminCode.append("istore ").append(tipoR.getPost()).append("\n");

                    }
                }
                else if(node.getKind().equals("ExprStmt"))
                {
                   if(node.getChildren().size()>1)
                   {
                       if(node.getJmmChild(node.getChildren().size()-1).getKind().equals("FunctionCall"))
                       {
                           if(node.getJmmChild(0).getKind().equals("ThisPointer"))
                           {jasminCode.append("invokestatic this");}
                           else
                           {jasminCode.append("invokestatis ");
                           }

                           int w =0;
                           while(node.getJmmChild(w) != node.getJmmChild(node.getChildren().size()-1))
                           {
                               if(node.getJmmChild(w).getKind().equals("FunctionCall"))
                               {
                                   jasminCode.append("/");
                                   jasminCode.append(node.getJmmChild(w).getJmmChild(0).get("id"));
                               }
                               w++;
                           }
                           jasminCode.append("/");
                           jasminCode.append(node.getJmmChild(w).getJmmChild(0).get("id"));
                           jasminCode.append("(");
                           if(node.getJmmChild(w).getJmmChild(1).getKind().equals("FuncArgs"))
                           {
                               for(JmmNode iter : node.getJmmChild(w).getJmmChild(1).getChildren())
                               {
                                   TypeR tipoR = registos.get(iter.get("id"));
                                   switch (tipoR.getTipo().getName())
                                   {
                                       case "int":
                                           jasminCode.append("I;");
                                           break;
                                       case "bool":
                                           jasminCode.append("B;");
                                           break;
                                       case "String":
                                           jasminCode.append("Ljava/lang/String;");
                                           break;
                                       default: break;
                                   }
                               }
                           }
                           jasminCode.append(")");
                          switch (  semanticsResult.getSymbolTable().getReturnType(methods.get(i)).getName()
                          )
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
                }
            }
            jasminCode.append("\n");
            jasminCode.append(".end method\n");
        }
    }
    public void BinOp(JmmNode node, Map<String, TypeR> registos)
    {
        if(node.getJmmChild(0).getKind().equals("BinOp"))
        {BinOp(node.getJmmChild(0), registos);}
        else {
            if (node.getJmmChild(0).getKind().equals("IntLiteral")) {
                jasminCode.append("iconst_").append(node.getJmmChild(0).get("value")).append("\n");
            } else if (node.getJmmChild(0).getKind().equals("Identifier")) {
                TypeR typeR = registos.get(node.getJmmChild(0).get("id"));
                switch (typeR.getTipo().getName()) {

                    case "int":
                        if (!typeR.getTipo().isArray()) {
                            jasminCode.append("istore_").append(typeR.getPost()).append("\n");

                        } else {
                            jasminCode.append("iastore_").append(typeR.getPost()).append("\n");
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        if(node.getJmmChild(1).getKind().equals("BinOp"))
        {BinOp(node.getJmmChild(1), registos);}
        else
        {
            if(node.getJmmChild(1).getKind().equals("IntLiteral"))
            {
                jasminCode.append("iconst_").append(node.getJmmChild(1).get("value")).append("\n");
            }
            else if(node.getJmmChild(1).getKind().equals("Identifier"))
            {
                TypeR typeR = registos.get(node.getJmmChild(1).get("id"));
                switch (typeR.getTipo().getName())
                {

                    case "int":
                        if(!typeR.getTipo().isArray()) {jasminCode.append("istore_").append(typeR.getPost()).append("\n");;}
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


            case "sub":
                jasminCode.append("isub \n");
                break;

            case "mul":
                jasminCode.append("imul \n");
                break;

            case "div":
                jasminCode.append("idiv \n");
                break;

            default: break;
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

    public void addExpressions(JmmSemanticsResult semanticsResult) {
        var methods = semanticsResult.getSymbolTable().getMethods();

        for (String nomeMethod : methods) {
            System.out.println(nomeMethod);
            System.out.println(visitor.getExprs(nomeMethod));
        }

    }
}
