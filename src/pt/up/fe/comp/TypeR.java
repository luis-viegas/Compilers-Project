package pt.up.fe.comp;
import pt.up.fe.comp.jmm.analysis.table.Type;
public class TypeR {
    private Type tipo;
     private int pos;


    public TypeR(int pos, Type tipo)
    {
        this.pos=pos;
        this.tipo=tipo;
    }


    public int getPost(){return pos;}
    public Type getTipo(){return tipo;}
}
