package at.jku.ssw.java.bytecode.reducer.modules;

import at.jku.ssw.java.bytecode.reducer.runtypes.Reducer;
import at.jku.ssw.java.bytecode.reducer.utils.ClassUtils;
import at.jku.ssw.java.bytecode.reducer.utils.Javassist;
import at.jku.ssw.java.bytecode.reducer.utils.StringUtils;
import javassist.CtClass;

import java.io.IOException;
import java.io.InputStream;

/**
 * Superclass for {@link Reducer} tests.
 * Simplifies the access to byte codes / resources and keeps track
 * of a reducer's individual resources.
 *
 * @param <T> The type of the {@link Reducer} to test
 */
public abstract class ReducerTest<T extends Reducer> {

    /**
     * The directory name for byte codes.
     */
    public final String BYTE_CODE_DIR_NAME = "bytecodes";

    /**
     * The directory name for the source byte codes.
     */
    public final String ORIGINAL_DIR_NAME = "original";

    /**
     * The directory name for reduced / expected result byte codes.
     */
    public final String REDUCED_DIR_NAME = "reduced";

    /**
     * Postfix for class files.
     */
    public final String CLASS_POSTFIX = "class";

    /**
     * Path to original resources.
     */
    private final String originalResources;

    /**
     * Path to reduced resources.
     */
    private final String reducedResources;

    /**
     * The reducer instance to use.
     */
    protected T reducer;

    /**
     * Instantiate a new test and determine the resource paths accordingly.
     */
    @SuppressWarnings("unchecked")
    public ReducerTest() {
        Class<T> reducer = (Class<T>) ClassUtils.getGenericTypes(getClass())[0];

        String dirName   = StringUtils.snake_case(reducer.getSimpleName());
        String resources = BYTE_CODE_DIR_NAME + "/" + dirName + "/";
        originalResources = resources + ORIGINAL_DIR_NAME + "/";
        reducedResources = resources + REDUCED_DIR_NAME + "/";
    }

    /**
     * Load the original class identified by the given name.
     *
     * @param name The class name
     * @return the byte code of the requested class
     * @throws IOException if the file cannot be found
     */
    protected final byte[] loadOriginalBytecode(String name) throws IOException {
        try (InputStream is = getResourceStream(originalResources + name + "." + CLASS_POSTFIX)) {
            return is.readAllBytes();
        }
    }

    /**
     * Load the expected reduced class identified by the given name.
     *
     * @param name The class name
     * @return the byte code of the requested class
     * @throws IOException if the file cannot be found
     */
    protected final byte[] loadReducedBytecode(String name) throws IOException {
        try (InputStream is = getResourceStream(reducedResources + name + "." + CLASS_POSTFIX)) {
            return is.readAllBytes();
        }
    }

    /**
     * Fetch the resource at the given class path location.
     *
     * @param path The path of the resource
     * @return an input stream for the resource file
     */
    private InputStream getResourceStream(String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }

    protected static CtClass classFromBytecode(byte[] bytecode) throws IOException {
        return Javassist.loadClass(bytecode);
    }

}