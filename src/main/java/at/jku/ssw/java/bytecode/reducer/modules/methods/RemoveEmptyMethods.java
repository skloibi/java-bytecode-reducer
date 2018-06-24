package at.jku.ssw.java.bytecode.reducer.modules.methods;

import at.jku.ssw.java.bytecode.reducer.annot.Sound;
import at.jku.ssw.java.bytecode.reducer.runtypes.JavassistHelper;
import at.jku.ssw.java.bytecode.reducer.runtypes.MemberReducer;
import javassist.CtClass;
import javassist.CtMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.stream.Stream;

@Sound
public class RemoveEmptyMethods implements MemberReducer<CtClass, CtMethod>, JavassistHelper {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Stream<CtMethod> getMembers(CtClass clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(CtMethod::isEmpty);
    }

    @Override
    public CtClass process(CtClass clazz, CtMethod m) throws Exception {
        logger.debug("Removing empty method '{}'", m.getLongName());
        clazz.removeMethod(m);
        return clazz;
    }
}
