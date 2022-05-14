package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.*;

public class MySymbolTable implements SymbolTable {

    List<String> imports;
    Map<String,List<Symbol>> params;
    List<String> methods;
    String className;
    String superClass;

    List<Symbol> fields;

    public MySymbolTable() {
        this.imports = new ArrayList<>();
        this.className = "";
        this.superClass = "";
        this.methods = new ArrayList<>();
        this.fields = new ArrayList<Symbol>();
        this.params = new HashMap<String,List<Symbol>>();

    }

    public void addImport(String import_) {
        this.imports.add(import_);
    }
    public void setMethods(String method){
        this.methods.add(method);
    }
    public List<String> getImports() {
        return imports;
    }


    public void setClassName(String className){
        this.className = className;
    }
    public void setFields(Symbol vars)
    {
        this.fields.add(vars);
    }

    @Override
    public String getClassName() {
        return this.className;
    }

    public void setSuper(String superClass) {
        this.superClass = superClass;
    }

    @Override
    public String getSuper() {
        return this.superClass;
    }

    @Override
    public List<Symbol> getFields() {
        return this.fields;
    }

    @Override
    public List<String> getMethods() {
        return methods;
    }


    @Override
    public Type getReturnType(String methodSignature) {
        return null;
    }

    @Override
    public List<Symbol> getParameters(String methodSignature) {
        for (Map.Entry<String,List<Symbol>> entry : params.entrySet())
        {
            if ( entry.getKey().equals(methodSignature)) {return entry.getValue();}
        }
        return new ArrayList<Symbol>();
    }

    public void setParameters(String methodSignature, Symbol simbolo)
    {
        for (Map.Entry<String,List<Symbol>> entry : params.entrySet())
        {
            List<Symbol> lista = entry.getValue();
            lista.add(simbolo);
            if ( entry.getKey().equals(methodSignature)) {entry.setValue(lista);}
        }
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        return null;
    }
}
