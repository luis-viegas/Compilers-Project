package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySymbolTable implements SymbolTable {

    List<String> imports;
    String className;
    String superClass;
    List<Symbol> fields;
    List<String> methods;
    Map<String,Type> returnType;
    Map<String,List<Symbol>> params;
    Map<String,List<Symbol>> local_vars;

    public MySymbolTable() {
        this.imports = new ArrayList<>();
        this.className = "";
        this.superClass = "";
        this.fields = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.returnType = new HashMap<>();
        this.params = new HashMap<>();
        this.local_vars = new HashMap<>();
    }

    public void addImport(String import_) {
        this.imports.add(import_);
    }

    @Override
    public List<String> getImports() {
        return imports;
    }

    public void setClassName(String className){
        this.className = className;
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

    public void setFields(Symbol vars)
    {
        this.fields.add(vars);
    }


    @Override
    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(String method){
        this.methods.add(method);
    }


    @Override
    public Type getReturnType(String methodSignature) {
        return this.returnType.get(methodSignature);
    }

    public void addReturnType(String methodSignature, Type returnType){
        this.returnType.put(methodSignature,returnType);
    }

    @Override
    public List<Symbol> getParameters(String methodSignature) {
        return this.params.get(methodSignature);
    }

    public void addParameters(String methodSignature, List<Symbol> vars) {
        this.params.put(methodSignature, vars);
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        return this.local_vars.get(methodSignature);
    }

    public void addLocalVariables(String methodSignature, List<Symbol> vars) {
        this.local_vars.put(methodSignature, vars);
    }
}


