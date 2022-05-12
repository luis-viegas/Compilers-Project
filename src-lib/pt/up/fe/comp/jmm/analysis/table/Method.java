package pt.up.fe.comp.jmm.analysis.table;

import java.util.List;

public class Method {
    private final String returned;
    private final String name;
    private final List<Symbol> vars;
    private final List<Symbol> params;

    public Method(String returned, String name, List<Symbol> vars, List<Symbol> params) {
        this.returned = returned;
        this.name = name;
        this.vars = vars;
        this.params = params;
    }

    public String getReturned() {
        return returned;
    }

    public String getName() {
        return name;
    }

    public List<Symbol> getVars() {
        return vars;
    }

    public List<Symbol> getParams() {
        return params;
    }
}
