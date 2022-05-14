package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.List;

public class MySymbolTable implements SymbolTable {

    List<String> imports;
    String className;
    String superClass;
    List<Symbol> fields;

    public MySymbolTable() {
        this.imports = new ArrayList<>();
        this.className = "";
        this.superClass = "";
        this.fields = new ArrayList<>();
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
        return null;
    }

    @Override
    public Type getReturnType(String methodSignature) {
        return null;
    }

    @Override
    public List<Symbol> getParameters(String methodSignature) {
        return null;
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        return null;
    }
}


