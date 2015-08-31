package com.energyict.mdc.engine.impl.logging;

import com.energyict.mdc.engine.exceptions.CodingException;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Produces wrappers for a logging framework that uses interface based logging,
 * i.e. the client code will be programming against interfaces while logging
 * instead of using the logging framework methods directly.
 * <p>
 * The approach is that you define an interface that contains methods
 * and parameters that relate to the messages that you want to log.
 * The methods are required to return void and will have a
 * {@link Configuration} annotation telling the logging framework
 * how to format the method parameters in the logged message.
 * The <code>getLoggerFor</code> methode will produce an instance of
 * the logging message interface that you provide.
 * If your logging message interface has a method of the following form:
 * <pre><code>
 *     public String getLoggingCategoryName ();
 * </code></pre>
 * Then that will return the the name of the logging category of the underlying logging framework.
 * </p>
 * <p>
 * If you want/need support for internationalization/localization you will in addition
 * annotate your message interface class with the {@link I18N} annotation.
 * </p>
 * <p>
 * Example code:<pre><code>
 *     public interface ExampleLogMessages {
 *         \@Configuration(logLevel = LogLevel.INFO, format = "Example log message containing a Date {} and any arbitrary object as an argument: {}");
 *         public void logDateAndObject (Date aDate, Object any);
 *     }
 *
 *     public class ExampleLogClient {
 *          public static void main (String[] args) {
 *              ExampleLogMessages logger = LoggerFactory.getLoggerFor(Level.INFO, ExampleLogMessages.class);
 *              logger.logDateAndObject(new Date(), "Any object will do");
 *          }
 *     }
 * </code></pre>
 * </p>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-14 (13:55)
 */
public final class LoggerFactory {

    /**
     * The name of the method that can be defined on a logging message interface
     * that will return the name of the logging category of the underlying
     * logging framework.
     */
    private static final String GET_CATEGORY_NAME_METHOD_NAME = "getLoggingCategoryName";

    /**
     * Produces an instance of the message interface class
     * that will log message at the {@link LogLevel}
     * that is configured in the underlying logging framework
     * for the logging category that relates to the message interface class.
     *
     * @param messageInterfaceClass The message interface class
     * @param <MI> The message interface class
     * @return An instance of the message interface class
     */
    public static <MI> MI getLoggerFor (Class<MI> messageInterfaceClass) {
        Logger baseLogger = getBaseLogger(messageInterfaceClass);
        Level level = getLevel(baseLogger);
        return getLoggerFor(messageInterfaceClass, LogLevelMapper.forJavaUtilLogging().toLogLevel(level));
    }

    private static Level getLevel (Logger logger) {
        while (logger != null) {
            Level level = logger.getLevel();
            if (level == null) {
                logger = logger.getParent();
            }
            else {
                return level;
            }
        }
        return Level.INFO;
    }

    /**
     * Produces an instance of the message interface class
     * that will log message at the specified {@link LogLevel}.
     *
     * @param messageInterfaceClass The message interface class
     * @param logLevel The LogLevel
     * @param <MI> The message interface class
     * @return An instance of the message interface class
     */
    public static synchronized <MI> MI getLoggerFor (Class<MI> messageInterfaceClass, LogLevel logLevel) {
        Context<MI> context = getContext(messageInterfaceClass);
        return getLoggerFor(messageInterfaceClass, logLevel, context);
    }

    /**
     * Produces an instance of the message interface class
     * that will log message using an existing Logger.
     * Note that this will use the LogLevel that is already configured
     * on the Logger.
     *
     * @param messageInterfaceClass The message interface class
     * @param logger The existing Logger
     * @param <MI> The message interface class
     * @return An instance of the message interface class
     */
    public static synchronized <MI> MI getLoggerFor (Class<MI> messageInterfaceClass, Logger logger) {
        Context<MI> context = new ExistingLoggerStandardContext<>(logger);
        return getLoggerFor(messageInterfaceClass, LogLevelMapper.forJavaUtilLogging().toLogLevel(logger.getLevel()), context);
    }

    /**
     * Produces a unique instance of the message interface class
     * that will log message at the specified {@link LogLevel}.
     * Note that the getLoggingCategoryName method will return
     * <code>null</code> for these unique instances.
     *
     * @param messageInterfaceClass The message interface class
     * @param logLevel The LogLevel
     * @param <MI> The message interface class
     * @return An instance of the message interface class
     */
    public static synchronized <MI> MI getUniqueLoggerFor (Class<MI> messageInterfaceClass, LogLevel logLevel) {
        Context<MI> context = getUniqueContext(messageInterfaceClass);
        return getLoggerFor(messageInterfaceClass, logLevel, context);
    }

    private static <MI> MI getLoggerFor (Class<MI> messageInterfaceClass, LogLevel logLevel, Context<MI> context) {
        if (!messageInterfaceClass.isInterface()) {
            throw CodingException.loggerFactoryRequiresInterface(messageInterfaceClass);
        }
        try {
            Class<MI> subClass = context.findOrGenerateImplementationClass(messageInterfaceClass, logLevel);
            MI messageInterface = subClass.newInstance();
            context.injectLogger(messageInterfaceClass, messageInterface, logLevel);
            return messageInterface;
        }
        catch (InstantiationException | IllegalAccessException | CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }

    private static Logger getBaseLogger (Class clazz) {
        return Logger.getLogger(clazz.getName());
    }

    private static <MI> Context<MI> getContext (Class<MI> messageInterfaceClass) {
        I18N i18N = messageInterfaceClass.getAnnotation(I18N.class);
        if (i18N == null) {
            return new StandardContext<>();
        }
        else {
            return new I18NContext<>();
        }
    }

    private static <MI> Context<MI> getUniqueContext (Class<MI> messageInterfaceClass) {
        I18N i18N = messageInterfaceClass.getAnnotation(I18N.class);
        if (i18N == null) {
            return new UniqueStandardContext<>();
        }
        else {
            return new UniqueI18NContext<>();
        }
    }

    private LoggerFactory () {}

    private abstract static class Context<MI> {

        protected abstract void injectLogger (Class<MI> messageInterfaceClass, MI messageInterface, LogLevel logLevel);

        protected Class<MI> findOrGenerateImplementationClass (Class<MI> messageInterfaceClass, LogLevel logLevel) throws CannotCompileException {
            return this.newClassGenerator(messageInterfaceClass, logLevel).findOrGenerateImplementationClass();
        }

        protected abstract MessageInterfaceImplementationClassGenerator<MI> newClassGenerator (Class<MI> messageInterfaceClass, LogLevel logLevel);

    }

    private static class StandardContext<MI> extends Context<MI> {

        @Override
        public void injectLogger (Class<MI> messageInterfaceClass, MI messageInterface, LogLevel logLevel) {
            Logger logger = this.getBaseLoggerFor(messageInterfaceClass);
            logger.setLevel(LogLevelMapper.forJavaUtilLogging().fromLogLevel(logLevel));
            ((LoggerHolder) messageInterface).setLogger(logger);
        }

        protected Logger getBaseLoggerFor (Class<MI> messageInterfaceClass) {
            return getBaseLogger(messageInterfaceClass);
        }

        @Override
        protected MessageInterfaceImplementationClassGenerator<MI> newClassGenerator (Class<MI> messageInterfaceClass, LogLevel logLevel) {
            return new MessageInterfaceImplementationClassGenerator<>(
                            LoggerHolder.class,
                            new AnnotatedMethodFinder(Configuration.class),
                            this.newSourceCodeGenerator(logLevel),
                            messageInterfaceClass,
                            logLevel);
        }

        protected StandardSourceCodeGenerator newSourceCodeGenerator (LogLevel logLevel) {
            return new StandardSourceCodeGenerator(logLevel);
        }
    }

    private static final class ExistingLoggerStandardContext <MI> extends StandardContext<MI> {
        private Logger logger;

        private ExistingLoggerStandardContext (Logger logger) {
            super();
            this.logger = logger;
        }

        @Override
        protected Logger getBaseLoggerFor (Class<MI> messageInterfaceClass) {
            return this.logger;
        }
    }

    private static final class UniqueStandardContext <MI> extends StandardContext<MI> {
        @Override
        protected Logger getBaseLoggerFor (Class<MI> messageInterfaceClass) {
            return Logger.getAnonymousLogger();
        }
    }

    private static class I18NContext<MI> extends StandardContext<MI> {
        @Override
        protected StandardSourceCodeGenerator newSourceCodeGenerator (LogLevel logLevel) {
            return new InternationalizedSourceCodeGenerator(logLevel);
        }
    }

    private static final class UniqueI18NContext <MI> extends I18NContext<MI> {
        @Override
        protected Logger getBaseLoggerFor (Class<MI> messageInterfaceClass) {
            return Logger.getAnonymousLogger();
        }
    }

    /**
     * Holds onto a Logger.
     */
    public abstract static class LoggerHolder {
        private Logger logger;

        public LoggerHolder () {
            super();
        }

        public Logger getLogger () {
            return logger;
        }

        public void setLogger (Logger logger) {
            this.logger = logger;
        }

    }

    private static final class AnnotatedMethodFinder {

        private Class annotationClass;

        private AnnotatedMethodFinder (Class annotationClass) {
            super();
            this.annotationClass = annotationClass;
        }

        /**
         * Find the methods that need are annotated
         * with the annotation that this finder is looking for.
         *
         * @param clazz The Class in which methods are found
         * @return The methods that are annotated
         */
        @SuppressWarnings("unchecked")
        public Collection<Method> findMethods (Class clazz) {
            Collection<Method> annotatedMethods = new ArrayList<>();
            for (Method method : clazz.getMethods()) {
                Annotation annotation = method.getAnnotation(this.annotationClass);
                if (annotation != null) {
                    annotatedMethods.add(method);
                }
            }
            return annotatedMethods;
        }

    }

    private abstract static class SourceCodeGenerator {

        private static final String PARAMETER_REFERENCE_NAME = "$";

        /**
         * Builds the source code for one of the methods
         * of a message interface class.
         *
         * @param method The Method of the message interface class that needs a implementation
         * @return The method body
         */
        protected abstract String generate (Method method);

        protected Integer appendMessageParameters (Method method, StringBuilder methodBodyBuilder) {
            Integer throwableParameterIndex = this.validateExceptionParameters(method);
            Class<?>[] parameterTypes = this.getParameterTypes(method);
            if (0 != parameterTypes.length) {
                String separator = "\", new Object[]{";
                int parameterIndex = 0;
                for (int i = 0; i < parameterTypes.length; i++) {
                    methodBodyBuilder.append(separator);
                    if (throwableParameterIndex != null && throwableParameterIndex == i) {
                        parameterIndex++;
                    }
                    this.appendMessageParameter(methodBodyBuilder, parameterIndex, parameterTypes[i]);
                    separator = ", ";
                    parameterIndex++;
                }
                methodBodyBuilder.append("}");
            }
            return throwableParameterIndex;
        }

        protected Class<?>[] getParameterTypes (Method method) {
            Class<?>[] allParameterTypes = method.getParameterTypes();
            if (0 == allParameterTypes.length) {
                // No methods, done!
                return allParameterTypes;
            }
            else {
                List<Class<?>> parameterTypes = Stream
                        .of(allParameterTypes)
                        .filter(parameterType -> !this.isThrowable(parameterType))
                        .collect(Collectors.toList());
                return parameterTypes.toArray(new Class<?>[parameterTypes.size()]);
            }
        }

        protected Integer validateExceptionParameters (Method method) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (0 == parameterTypes.length) {
                // No parameters, done!
                return null;
            }
            else {
                int throwableParameterIndex = -1;
                for (int i = 0; i < parameterTypes.length; i++) {
                    if (isThrowable(parameterTypes[i])) {
                        if (throwableParameterIndex != -1) {
                            throw CodingException.loggerFactorySupportsOnlyOneThrowableParameter(method);
                        }
                        throwableParameterIndex = i;
                    }
                }
                if (throwableParameterIndex == -1) {
                    return null;
                }
                else {
                    return throwableParameterIndex;
                }
            }
        }

        private boolean isThrowable (Class<?> parameterType) {
            return Throwable.class.isAssignableFrom(parameterType);
        }

        private void appendMessageParameter (StringBuilder methodBodyBuilder, int i, Class parameterType) {
            if (parameterType.isPrimitive()) {
                methodBodyBuilder.append("new ");
                methodBodyBuilder.append(this.primitiveTypeToObjectType(parameterType).getSimpleName());
                methodBodyBuilder.append("(");
                this.parameterName(i, methodBodyBuilder);
                methodBodyBuilder.append(")");
            }
            else {
                this.parameterName(i, methodBodyBuilder);
            }
        }

        private Class primitiveTypeToObjectType (Class clazz) {
            if (Integer.TYPE.equals(clazz)) {
                return Integer.class;
            }
            else if (Long.TYPE.equals(clazz)) {
                return Long.class;
            }
            else if (Character.TYPE.equals(clazz)) {
                return Character.class;
            }
            else if (Byte.TYPE.equals(clazz)) {
                return Byte.class;
            }
            else if (Short.TYPE.equals(clazz)) {
                return Short.class;
            }
            else if (Float.TYPE.equals(clazz)) {
                return Float.class;
            }
            else if (Double.TYPE.equals(clazz)) {
                return Double.class;
            }
            else {
                // must be Boolean
                return Boolean.class;
            }
        }

        protected void parameterName (int zeroBasedParameterIndex, StringBuilder methodBodyBuilder) {
            methodBodyBuilder.append(PARAMETER_REFERENCE_NAME);
            methodBodyBuilder.append(zeroBasedParameterIndex + 1);
        }

    }

    private static class StandardSourceCodeGenerator extends SourceCodeGenerator {

        private LogLevel logLevel;

        private StandardSourceCodeGenerator (LogLevel logLevel) {
            super();
            this.logLevel = logLevel;
        }

        @Override
        protected String generate (Method method) {
            Configuration configuration = method.getAnnotation(Configuration.class);
            StringBuilder methodBodyBuilder = new StringBuilder();
            if (configuration.logLevel().compareTo(this.logLevel) <= 0) {
                // Method level <= execution logLevel so we emit the message
                this.validateExceptionParameters(method);
                methodBodyBuilder.append("{java.util.logging.LogRecord _logRecord = new java.util.logging.LogRecord(java.util.logging.Level.");
                methodBodyBuilder.append(LogLevelMapper.forJavaUtilLogging().fromLogLevel(configuration.logLevel()).getName());
                boolean noParameters = this.getParameterTypes(method).length == 0;
                if (noParameters) {
                    methodBodyBuilder.append(", \"");
                    methodBodyBuilder.append(this.toJavaUtilMessageParameter(configuration));
                    methodBodyBuilder.append("\");");
                }
                else {
                    methodBodyBuilder.append(", java.text.MessageFormat.format(\"");
                    methodBodyBuilder.append(this.toJavaUtilMessageParameter(configuration));
                    Integer throwableParameterIndex = this.appendMessageParameters(method, methodBodyBuilder);
                    methodBodyBuilder.append("));");
                    if (throwableParameterIndex != null) {
                        methodBodyBuilder.append("_logRecord.setThrown(");
                        this.parameterName(throwableParameterIndex, methodBodyBuilder);
                        methodBodyBuilder.append(");");
                    }
                }
                methodBodyBuilder.append("this.getLogger().log(_logRecord);}");
            }
            else {
                // Ignore the message as the level associated with it is not active
                methodBodyBuilder.append("{}");
            }
            return methodBodyBuilder.toString();
        }

        /**
         * Converts the {@link Configuration} to a String value
         * that will be used to pass to the java.util.Logging framework
         * as the message parameter.
         *
         * @param configuration The Configuration
         * @return The message parameter for the java.util.Logging framework
         */
        protected String toJavaUtilMessageParameter (Configuration configuration) {
            return configuration.format();
        }

    }

    private static final class InternationalizedSourceCodeGenerator extends StandardSourceCodeGenerator {

        private InternationalizedSourceCodeGenerator (LogLevel logLevel) {
            super(logLevel);
        }

        @Override
        protected String toJavaUtilMessageParameter (Configuration configuration) {
            // Todo: use NlsService and Thesaurus classes
            return super.toJavaUtilMessageParameter(configuration);
        }

    }

    private static final class MessageInterfaceImplementationClassGenerator<MI> {

        private ClassPool classPool;
        private Class superClass;
        private AnnotatedMethodFinder methodFinder;
        private SourceCodeGenerator sourceCodeGenerator;
        private Class<MI> messageInterfaceClass;
        private LogLevel logLevel;

        private MessageInterfaceImplementationClassGenerator (Class superClass, AnnotatedMethodFinder methodFinder, SourceCodeGenerator sourceCodeGenerator, Class<MI> messageInterfaceClass, LogLevel logLevel) {
            this.superClass = superClass;
            this.methodFinder = methodFinder;
            this.sourceCodeGenerator = sourceCodeGenerator;
            this.messageInterfaceClass = messageInterfaceClass;
            this.logLevel = logLevel;
            this.initializeClassPool();
        }

        private void initializeClassPool () {
            this.classPool = ClassPool.getDefault();
            this.classPool.insertClassPath(new ClassClassPath(LoggerHolder.class));
            this.classPool.insertClassPath(new ClassClassPath(LogLevel.class));
            this.classPool.insertClassPath(new ClassClassPath(Configuration.class));
        }

        @SuppressWarnings("unchecked")
        private Class<MI> findOrGenerateImplementationClass () throws CannotCompileException {
            try {
                return (Class<MI>) getClass().getClassLoader().loadClass(this.implementationClassName());
                //return (Class<MI>) Class.forName(this.implementationClassName());
            }
            catch (RuntimeException e) {
                if (this.causeIsClassNotFound(e)) {
                    return this.generateAndReturnImplementationClass();
                }
                else {
                    // Enable for debugging purposes: System.out.println("Rethrowing RuntimeException because the cause was not a javassist.NotFoundException but: " + e.getCause().getClass().getName());
                    // Rethrowing RuntimeException because the cause was not a javassist.NotFoundException
                    throw e;
                }
            }
            catch (ClassNotFoundException e) {
                return this.generateAndReturnImplementationClass();
            }
        }

        private boolean causeIsClassNotFound (RuntimeException e) {
            return e.getCause().getClass().getName().equals(NotFoundException.class.getName());
        }

        private Class<MI> generateAndReturnImplementationClass () throws CannotCompileException {
            this.generateImplementationClass();
            return this.getSubClassAfterGeneration();
        }

        private String implementationClassName () {
            return this.messageInterfaceClass.getPackage().getName() + "." + this.implementationClassSimpleName();
        }

        private String implementationClassSimpleName () {
            return this.messageInterfaceClass.getSimpleName() + "CT_Impl" + this.logLevel.name();
        }

        @SuppressWarnings("unchecked")
        private void generateImplementationClass () throws CannotCompileException {
            CtClass superClass = this.getSuperClassFromPool();
            CtClass implementationClass = this.classPool.makeClass(this.implementationClassName());
            implementationClass.setSuperclass(superClass);
            implementationClass.addInterface(this.getClassFromPool(this.messageInterfaceClass));
            this.addInterfaceMethods(implementationClass);
            implementationClass.toClass(this.messageInterfaceClass.getClassLoader());
        }

        private void addInterfaceMethods (CtClass implementationClass)
            throws CannotCompileException {
            for (Method method : this.getConfiguredMethods(this.messageInterfaceClass)) {
                this.addImplementationFor(method, implementationClass);
            }
            try {
                Method getLoggingCategoryNameMethod = this.messageInterfaceClass.getMethod(GET_CATEGORY_NAME_METHOD_NAME, null);
                if (getLoggingCategoryNameMethod != null) {
                    this.addImplementationFor(getLoggingCategoryNameMethod, implementationClass);
                }
            }
            catch (NoSuchMethodException e) {
                // Ok, method is not defined on the interface, no need to generate it.
            }
        }

        private <MI> Collection<Method> getConfiguredMethods (Class<MI> messageInterfaceClass) {
            return this.methodFinder.findMethods(messageInterfaceClass);
        }

        private CtClass getSuperClassFromPool () {
            return this.getClassFromPool(this.superClass, "Could not find the private inner class {0}. Always expect the unexpected ;-)");
        }

        private CtClass getClassFromPool (Class clazz) {
            this.classPool.insertClassPath(new ClassClassPath(clazz));
            return this.getClassFromPool(clazz, "Could not find the class {0} from the JavaAssist class pool. Always expect the unexpected ;-)");
        }

        private CtClass getClassFromPool (Class clazz, String errorMessagePattern) {
            try {
                return this.classPool.get(clazz.getName());
            }
            catch (NotFoundException e) {
                throw new RuntimeException(MessageFormat.format(errorMessagePattern, clazz.getName()), e);
            }
        }

        private void addImplementationFor (Method method, CtClass messageInterfaceImplementationClass) throws CannotCompileException {
            String methodBody;
            if (this.isGetCategoryNameMethod(method)) {
                methodBody = "return this.getLogger().getName();";
            }
            else {
                methodBody = this.generateBody(method);
            }
            CtMethod implementation =
                    CtNewMethod.make(
                            this.getClassFromPool(method.getReturnType()),
                            method.getName(),
                            this.getParameterClasses(method),
                            this.getExceptionClasses(method),
                            methodBody,
                            messageInterfaceImplementationClass);
            messageInterfaceImplementationClass.addMethod(implementation);
        }

        private boolean isGetCategoryNameMethod (Method method) {
            return GET_CATEGORY_NAME_METHOD_NAME.equals(method.getName()) && String.class.equals(method.getReturnType());
        }

        private String generateBody (Method method) {
            return this.sourceCodeGenerator.generate(method);
        }

        private CtClass[] getParameterClasses (Method method) {
            return this.getClassesFromPool(method.getParameterTypes());
        }

        private CtClass[] getExceptionClasses (Method method) {
            return this.getClassesFromPool(method.getExceptionTypes());
        }

        private CtClass[] getClassesFromPool (Class[] classes) {
            CtClass[] clClasses = new CtClass[classes.length];
            for (int i = 0; i < classes.length; i++) {
                clClasses[i] = this.getClassFromPool(classes[i]);
            }
            return clClasses;
        }

        @SuppressWarnings("unchecked")
        private Class<MI> getSubClassAfterGeneration () {
            try {
                return (Class<MI>) getClass().getClassLoader().loadClass(this.implementationClassName());
                //return (Class<MI>) Class.forName(this.implementationClassName());
            }
            catch (ClassNotFoundException e) {
                /* Technically impossible as we have just generated the class successfully.
                 * Therefore, it must exist and be on the classpath. */
                throw new RuntimeException("Always expect the unexpected ;-)", e);
            }
        }

    }

}