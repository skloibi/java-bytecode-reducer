package at.jku.ssw.java.bytecode.reducer.modules.legacy;

import at.jku.ssw.java.bytecode.reducer.modules.methods.ReplaceMethodCalls;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReplaceMethodCallsTest extends ReducerTest<ReplaceMethodCalls> {

    @BeforeEach
    void setUp() {
        reducer = new ReplaceMethodCalls();
    }

    @AfterEach
    void tearDown() {
        reducer = null;
    }

    @Test
    void testSingleMethodCall() throws Exception {
        assertReduced("SingleMethodCall")
                .and(bytes -> assertNoMethodCall(bytes, "SingleMethodCall.anInt()"))
                .and(bytes -> assertNoMethodCall(bytes, "SingleMethodCall.main(String[])"));
    }

    @Test
    void testRecursiveCalls() throws Exception {
        assertReduced("RecursiveCalls")
                .and(bytes -> assertNoMethodCall(bytes, "RecursiveCalls.aFourthMethod(int)"))
                .and(bytes -> assertNoMethodCall(bytes, "RecursiveCalls.main(String[])"));
    }
}
