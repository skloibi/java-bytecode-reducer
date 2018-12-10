package samples;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * Abstract superclass for generic bytecode samples.
 * This class assigns the default properties like the name, type and internals
 * and also provides convenience method to create class writers.
 */
public abstract class BytecodeSample {

    /**
     * The default superclass for sample classes ({@link java.lang.Object}).
     */
    static final String DEFAULT_SUPERCLASS = "java/lang/Object";

    /**
     * The class name.
     */
    final String className;

    /**
     * The internal type name.
     */
    final String internalName;

    /**
     * The actual type descriptor.
     */
    final Type type;

    /**
     * Initializes the basic properties and derives the types.
     */
    BytecodeSample() {
        className = getClass().getSimpleName();
        internalName = Type.getInternalName(getClass());
        type = Type.getType(getClass());
    }

    /**
     * Creates the class writer and initializes the basic class properties.
     *
     * @param version The class file version
     * @return a class writer instance that already contains the class header
     */
    public ClassWriter assemble(int version) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(version, ACC_PUBLIC, className, null, DEFAULT_SUPERCLASS, new String[]{});
        return prepareFields(cw);
    }

    /**
     * Method that may be overridden to specify custom fields.
     *
     * @param cw The class writer
     * @return the same class writer
     */
    public ClassWriter prepareFields(ClassWriter cw) {
        return cw;
    }

    /**
     * Internal wrapper for bytecodes / class files.
     */
    public class Bytecode {

        /**
         * The class name.
         */
        public final String className;

        /**
         * The bytecode of the class file.
         */
        public final byte[] bytecode;

        /**
         * Initializes a new wrapper for this sample class
         * using the provided bytecode.
         *
         * @param bytecode The generated bytecode of the sample class
         */
        public Bytecode(byte[] bytecode) {
            this.className = BytecodeSample.this.className;
            this.bytecode = bytecode;
        }
    }
}
