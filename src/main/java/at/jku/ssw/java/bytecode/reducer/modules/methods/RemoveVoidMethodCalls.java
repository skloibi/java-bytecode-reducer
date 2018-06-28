package at.jku.ssw.java.bytecode.reducer.modules.methods;

import at.jku.ssw.java.bytecode.reducer.annot.Expensive;
import at.jku.ssw.java.bytecode.reducer.annot.Unsound;
import at.jku.ssw.java.bytecode.reducer.context.Reduction.Base;
import at.jku.ssw.java.bytecode.reducer.context.Reduction.Result;
import at.jku.ssw.java.bytecode.reducer.runtypes.ForcibleReducer;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TConsumer;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TFunction;
import at.jku.ssw.java.bytecode.reducer.utils.functional.TPredicate;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Expressions;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Instrumentation;
import at.jku.ssw.java.bytecode.reducer.utils.javassist.Javassist;
import javassist.CtClass;
import javassist.expr.MethodCall;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Remove void method calls.
 */
@Expensive
@Unsound
public class RemoveVoidMethodCalls implements ForcibleReducer<Integer> {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Result<Integer> apply(Base<Integer> base) throws Exception {
        final CtClass clazz = Javassist.loadClass(base.bytecode());

        final AtomicReference<MethodCall> call = new AtomicReference<>();

        Instrumentation.forMethodCalls(
                clazz,
                (TPredicate<MethodCall>) c ->
                        c.getMethod().getReturnType().equals(CtClass.voidType) &&
                                !base.cache().contains(c.indexOfBytecode()) &&
                                call.compareAndSet(null, c),
                (TConsumer<MethodCall>) c -> {

                    logger.debug(
                            "Removing call of method '{}' at index {}",
                            c.getMethodName(),
                            c.indexOfBytecode()
                    );

                    c.replace(Expressions.NO_EXPRESSION);
                }
        );

        // if no applicable member was found, the reduction is minimal
        return Optional.ofNullable(call.get())
                .map((TFunction<MethodCall, Result<Integer>>) f ->
                        base.toResult(Javassist.bytecode(clazz), f.indexOfBytecode()))
                .orElseGet(base::toMinimalResult);
    }
}