package at.jku.ssw.java.bytecode.reducer.context;

import at.jku.ssw.java.bytecode.reducer.errors.DuplicateClassException;
import at.jku.ssw.java.bytecode.reducer.utils.FileUtils;
import at.jku.ssw.java.bytecode.reducer.utils.OSUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Factory that is initialized with properties and file paths
 * in order to generate a valid test context.
 */
public class ContextFactory {
    //-------------------------------------------------------------------------
    // region Constants

    private static final Logger logger = LogManager.getLogger();

    /**
     * The default output directory for the intermediate
     * and final result files.
     */
    public static final String DEFAULT_OUT = "out";

    /**
     * The default place to put the test's working directories.
     */
    public static final String DEFAULT_TEMP = ".tmp";

    /**
     * The default timeout in seconds.
     */
    public static final long DEFAULT_TIMEOUT = 10;

    // endregion
    //-------------------------------------------------------------------------
    // region Properties

    /**
     * Either relative or absolute paths to the class files.
     */
    private final String[] classFiles;

    /**
     * Either relative or absolute paths to the interestingness tests.
     */
    private final String[] iTests;

    /**
     * Relative or absolute path to the working directory that is used
     * to store the temporary and output directory.
     * The current directory is used, if no argument was specified.
     */
    private final String workingDir;

    /**
     * Output directory for the final / intermediate result files.
     * If a relative path is specified, the output directory for the context
     * is assumed to be a subdirectory of the working directory.
     */
    private final String outDir;

    /**
     * Temporary directory that is used to store the sources and
     * test files for the individual test runs.
     * If the path is relative, the temporary directory for the context is
     * assumed to be a subdirectory of the working directory.
     */
    private final String tempDir;

    /**
     * Keep temporary test files and directories instead of deleting
     * them after each run.
     */
    private final boolean keepTemp;

    /**
     * Matcher for test scripts.
     */
    private final PathMatcher scriptMatcher;

    /**
     * Matcher for class files.
     */
    private final PathMatcher classMatcher;

    /**
     * Timeout for interestingness test runs.
     */
    private final long timeout;

    /**
     * Possible filters for individual reducers.
     */
    private final String[] filters;

    // endregion
    //-------------------------------------------------------------------------
    // region Initialization

    public ContextFactory(String[] classFiles,
                          String[] iTests,
                          String workingDir,
                          String outDir,
                          String tempDir,
                          String[] filters,
                          boolean keepTemp,
                          long timeout) {

        this.classFiles = classFiles;
        this.iTests = iTests;
        this.workingDir = workingDir == null ? "" : workingDir;
        this.outDir = outDir == null ? DEFAULT_OUT : outDir;
        this.tempDir = tempDir == null ? DEFAULT_TEMP : tempDir;
        this.keepTemp = keepTemp;
        this.timeout = timeout == -1 ? DEFAULT_TIMEOUT : timeout;
        this.filters = filters;

        String scriptPattern = OSUtils.isWindows() ? "glob:*.bat" : "glob:*.sh";
        scriptMatcher = FileSystems.getDefault().getPathMatcher(scriptPattern);
        classMatcher = FileSystems.getDefault().getPathMatcher("glob:*.class");
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Overridden methods


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        var that = (ContextFactory) o;

        if (keepTemp != that.keepTemp) return false;
        if (timeout != that.timeout) return false;
        if (!Arrays.equals(classFiles, that.classFiles)) return false;
        if (!Arrays.equals(iTests, that.iTests)) return false;
        if (!workingDir.equals(that.workingDir)) return false;
        if (!outDir.equals(that.outDir)) return false;
        if (!tempDir.equals(that.tempDir)) return false;
        return Arrays.equals(filters, that.filters);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(classFiles);
        result = 31 * result + Arrays.hashCode(iTests);
        result = 31 * result + workingDir.hashCode();
        result = 31 * result + outDir.hashCode();
        result = 31 * result + tempDir.hashCode();
        result = 31 * result + (keepTemp ? 1 : 0);
        result = 31 * result + (int) (timeout ^ (timeout >>> 32));
        result = 31 * result + Arrays.hashCode(filters);
        return result;
    }

    @Override
    public String toString() {
        return "ContextFactory{" +
                "classFiles=" + Arrays.toString(classFiles) +
                ", iTests=" + Arrays.toString(iTests) +
                ", workingDir='" + workingDir + '\'' +
                ", outDir='" + outDir + '\'' +
                ", tempDir='" + tempDir + '\'' +
                ", filters='" + Arrays.toString(filters) + '\'' +
                '}';
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Factory methods

    /**
     * Initialize a new context and set up the directories.
     *
     * @return the new context
     */
    public Context createContext() {

        // retrieve the working / output directories as absolute paths
        Path workingDir = Paths.get(this.workingDir).toAbsolutePath();
        Path outDir     = workingDir.resolve(this.outDir).toAbsolutePath();
        Path tempDir    = workingDir.resolve(this.tempDir).toAbsolutePath();

        var noFilters = this.filters.length == 0;

        var modules = ModuleRegistry.allModules().stream()
                .filter(m -> noFilters || Arrays.stream(this.filters)
                        .anyMatch(f -> f.equalsIgnoreCase(m.getSimpleName())))
                .collect(Collectors.toList());

        return new Context(outDir, tempDir, modules, keepTemp);
    }

    public BytecodeCache initCache()
            throws IOException, DuplicateClassException {

        Set<Path> classFiles = validate(
                Paths.get(workingDir).toAbsolutePath(),
                this.classFiles,
                classMatcher
        );

        return new BytecodeCache(classFiles);
    }

    public TestSuite getTestSuite() throws IOException {
        Set<Path> iTests = validate(
                Paths.get(this.workingDir).toAbsolutePath(),
                this.iTests,
                scriptMatcher
        );

        return new TestSuite(iTests, timeout);
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Utility methods

    /**
     * Validates the given file names.
     *
     * @param workingDir The working directory (default reference for relative paths)
     * @param files      The files to analyze
     * @param matcher    The required file ending
     * @return the filtered and verified list of files
     * @throws IOException if the file handlers run into problems
     */
    private Set<Path> validate(Path workingDir,
                               String[] files,
                               PathMatcher matcher) throws IOException {

        // scan working directory if no targets are given
        if (files.length == 0)
            return FileUtils.scan(workingDir, matcher)
                    .collect(Collectors.toSet());

        return resolve(workingDir, files, matcher)
                .collect(Collectors.toSet());
    }

    /**
     * Resolve a number of file names / paths against a given root directory.
     * The given paths can either be absolute or relative.
     * If the paths are absolute the root path is ignored.
     *
     * @param root    Reference directory for relative paths
     * @param paths   The path descriptors that should be resolved to valid paths
     * @param matcher The required file ending
     * @return a stream of (absolute) paths representing the given descriptors
     */
    private Stream<Path> resolve(Path root, String[] paths, PathMatcher matcher) {
        return Arrays.stream(paths)
                .map(Paths::get)
                .map(root::resolve)
                .filter(p -> {
                    if (Files.isDirectory(p)) {
                        logger.warn("Skipping {} - not a file.", p);
                        return false;
                    } else if (Files.notExists(p)) {
                        logger.warn("Skipping {} - file not found.", p);
                        return false;
                    } else if (!matcher.matches(p.getFileName())) {
                        logger.warn("Skipping {} - file does not match the required extension (\"{}\").", p, matcher);
                        return false;
                    }

                    return true;
                });
    }

    // endregion
    //-------------------------------------------------------------------------
}
