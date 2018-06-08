package at.jku.ssw.java.bytecode.reducer.modules.methods;

import at.jku.ssw.java.bytecode.reducer.annot.Sound;
import at.jku.ssw.java.bytecode.reducer.runtypes.MemberReducer;
import at.jku.ssw.java.bytecode.reducer.utils.Javassist;
import javassist.CtClass;
import javassist.CtMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.stream.Stream;

@Sound
public class RemoveEmptyMethods implements MemberReducer<CtClass, CtMethod> {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public CtClass classFrom(byte[] bytecode) throws Exception {
        return Javassist.loadClass(bytecode);
    }

    @Override
    public byte[] bytecodeFrom(CtClass clazz) throws Exception {
        return Javassist.bytecode(clazz);
    }

    @Override
    public Stream<CtMethod> getMembers(CtClass clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(CtMethod::isEmpty);
    }

    @Override
    public CtClass process(CtClass clazz, CtMethod m) throws Exception {
        logger.debug("Removing empty method '{}'", m.getSignature());
        clazz.removeMethod(m);
        return clazz;
    }
}