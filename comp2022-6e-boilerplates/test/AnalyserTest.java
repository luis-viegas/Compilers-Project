import org.junit.Test;
import pt.up.fe.comp.ParserTest;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class AnalyserTest {
    @Test
    public void HelloWorld() {
        var result = TestUtils.analyse(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));

        ParserTest helloWorld = new ParserTest();
        helloWorld.helloWorld();

        System.out.println("Symbol Table " + result.getSymbolTable().print());

    }
}
