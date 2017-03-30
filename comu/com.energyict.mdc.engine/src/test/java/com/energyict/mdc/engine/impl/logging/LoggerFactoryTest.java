/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.logging;

import com.energyict.mdc.engine.exceptions.CodingException;

import org.joda.time.DateTime;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.logging.LoggerFactory} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-09 (12:09)
 */
@RunWith(MockitoJUnitRunner.class)
public class LoggerFactoryTest {

    private static final String I18N_DEVICE_NEEDS_DIAL_CALENDAR = "deviceNeedsDialCalendar";
    private static final String I18N_DUPLICATE_DEVICE_X = "duplicateDeviceX";
    private static final String I18N_HELLO_WORLD = "helloWorld";

    public interface NoI18N {

        public String getLoggingCategoryName ();

        @Configuration(logLevel = LogLevel.ERROR, format = "A simple error message without parameters")
        public void errorMessageWithoutParameters ();

        @Configuration(logLevel = LogLevel.INFO, format = "A simple info message without parameters")
        public void infoMessageWithoutParameters ();

        @Configuration(logLevel = LogLevel.INFO, format = "Tests have started on {0,date,yyyy-MM-dd HH:mm:ss}")
        public void testsStarted (Date startDate);

        @Configuration(logLevel = LogLevel.INFO, format = "For your information int={0}, long={1}, char={2}, byte={3}, short={4}, float={5}, double={6}, boolean={7}")
        public void infoMessageWithPrimitiveTypeParam (int anInt, long aLong, char aChar, byte aByte, short aShort, float aFloat, double aDouble, boolean aBoolean);

    }

    public class MessageClass {

        public String getLoggingCategoryName () {
            return null;
        }

        @Configuration(logLevel = LogLevel.ERROR, format = "A simple message without parameters")
        public void errorMessageWithoutParameters() {}

        @Configuration(logLevel = LogLevel.INFO, format = "Tests have started on {0,date,yyyy-MM-dd HH:mm:ss}")
        public void testsStarted (Date startDate) {}

    }

    public interface ExceptionLogger {

        @Configuration(logLevel = LogLevel.ERROR, format = "An unexpected IllegalArgumentException occurred in context {0}!")
        public void error (String context, IllegalArgumentException e);

    }

    public interface NewLineLogger {

        @Configuration(logLevel = LogLevel.ERROR, format = "A simple message with newlines between {0} \\r\\n and {1}")
        public void errorMessageWithNewLinesBetweenParameters (String one, String two);

    }

    public interface MultipleExceptions {

        @Configuration(logLevel = LogLevel.ERROR, format = "Multiple exceptions are not supported by the LoggerFactory.")
        public void error (IllegalArgumentException b, SQLException sql);

    }

    @I18N
    public interface WithI18N {
        @Configuration(logLevel = LogLevel.ERROR, format = I18N_DEVICE_NEEDS_DIAL_CALENDAR)
        public void deviceNeedsDialCalendar ();

        @Configuration(logLevel = LogLevel.ERROR, format = I18N_DUPLICATE_DEVICE_X)
        public void duplicateDevice (String name);

        @Configuration(logLevel = LogLevel.WARN, format = I18N_HELLO_WORLD)
        public void helloWorld (String yourName, Date now);

    }

    @BeforeClass
    public static void initializeLoggingFrameworks () throws IOException {
        InputStream configStream = LoggerFactoryTest.class.getClassLoader().getResourceAsStream("logging.properties");
        LogManager.getLogManager().readConfiguration(configStream);
    }

    @Test
    public void testLogLevelCopiedFromUnderlyingLogger () {
        Level expectedLevel = getLevel(Logger.getLogger(NoI18N.class.getName()));
        NoI18N noI18N = LoggerFactory.getLoggerFor(NoI18N.class);
        Logger logger = Logger.getLogger(noI18N.getLoggingCategoryName());
        assertEquals("The LogLevel does not match.", expectedLevel, logger.getLevel());
    }

    @Test
    public void testLogLevel () {
        NoI18N noI18N = LoggerFactory.getLoggerFor(NoI18N.class, LogLevel.INFO);
        Logger logger = Logger.getLogger(noI18N.getLoggingCategoryName());
        assertEquals("The LogLevel does not match.", Level.INFO, logger.getLevel());
    }

    @Test
    public void testUniqueLoggerLogLevel () {
        NoI18N noI18N = LoggerFactory.getUniqueLoggerFor(NoI18N.class, LogLevel.ERROR);
        LoggerFactory.LoggerHolder loggerHolder = (LoggerFactory.LoggerHolder) noI18N;
        assertEquals("The LogLevel does not match.", Level.SEVERE, loggerHolder.getLogger().getLevel());
    }

    @Test
    public void testUniqueLoggerI18NLogLevel () {
        WithI18N noI18N = LoggerFactory.getUniqueLoggerFor(WithI18N.class, LogLevel.ERROR);
        LoggerFactory.LoggerHolder loggerHolder = (LoggerFactory.LoggerHolder) noI18N;
        assertEquals("The LogLevel does not match.", Level.SEVERE, loggerHolder.getLogger().getLevel());
    }

    @Test
    public void testUniqueLoggerReturnsDifferentLoggerInstances () {
        NoI18N first = LoggerFactory.getUniqueLoggerFor(NoI18N.class, LogLevel.INFO);
        NoI18N second = LoggerFactory.getUniqueLoggerFor(NoI18N.class, LogLevel.INFO);
        LoggerFactory.LoggerHolder firstLoggerHolder = (LoggerFactory.LoggerHolder) first;
        LoggerFactory.LoggerHolder secondLoggerHolder = (LoggerFactory.LoggerHolder) second;
        assertTrue("Expected two unique instances", firstLoggerHolder.getLogger() != secondLoggerHolder.getLogger());
    }

    /**
     * Tests that the log method of the underlying logging framework
     * is NOT called when the level does not match.
     * Example, set the log level to ERROR and then call an interface
     * method that is configured for the INFO level then
     * this should NOT trigger the log method on the underlying logging framework.
     */
    @Test
    public void testNo18NAtInfoLevel () {
        NoI18N noI18N = LoggerFactory.getLoggerFor(NoI18N.class, LogLevel.ERROR);
        LoggerFactory.LoggerHolder loggerHolder = (LoggerFactory.LoggerHolder) noI18N;
        Logger loggerSpy = spy(loggerHolder.getLogger());
        loggerHolder.setLogger(loggerSpy);

        // Target method
        noI18N.infoMessageWithoutParameters();

        //Asserts
        verifyNoMoreInteractions(loggerSpy);
    }

    /**
     * Tests that the implementation class for all log levels is different.
     */
    @Test
    public void testLoggerClassIsDifferentForAllLevels () {
        Set<Class> loggerClasses = new HashSet<Class>();
        for (LogLevel logLevel : LogLevel.values()) {
            Class<? extends NoI18N> loggerClass = LoggerFactory.getLoggerFor(NoI18N.class, logLevel).getClass();
            assertFalse("The generated class for level " + logLevel + " already exists", loggerClasses.contains(loggerClass));
        }
    }

    @Test
    public void testPrimitiveTypeParameters () {
        NoI18N noI18N = LoggerFactory.getLoggerFor(NoI18N.class, LogLevel.INFO);
        LoggerFactory.LoggerHolder loggerHolder = (LoggerFactory.LoggerHolder) noI18N;
        LogHandler logHandler = new LogHandler();
        loggerHolder.getLogger().addHandler(logHandler);

        int anInt = 1;
        long aLong = 2l;
        char aChar = 'a';
        byte aByte = 122;   // char 'z'
        short aShort = 3;
        float aFloat = 3.14f;
        double aDouble = 3.14;
        boolean aBoolean = true;
        String expectedMessage = MessageFormat.format("For your information int={0}, long={1}, char={2}, byte={3}, short={4}, float={5}, double={6}, boolean={7}", anInt, aLong, aChar, aByte, aShort, aFloat, aDouble, aBoolean);

        // Target method
        noI18N.infoMessageWithPrimitiveTypeParam(anInt, aLong, aChar, aByte, aShort, aFloat, aDouble, aBoolean);

        // Asserts
        assertEquals(expectedMessage, logHandler.getLogRecords().get(0).getMessage());
    }

    @Test
    @Ignore
    public void testWithI18N () {
        WithI18N withI18N = LoggerFactory.getLoggerFor(WithI18N.class, LogLevel.DEBUG);
        LoggerFactory.LoggerHolder loggerHolder = (LoggerFactory.LoggerHolder) withI18N;
        LogHandler logHandler = new LogHandler();
        loggerHolder.getLogger().addHandler(logHandler);

        // Target method
        withI18N.deviceNeedsDialCalendar();

        // Asserts
        assertEquals("The number of log records does not match", 1, logHandler.getLogRecords().size());
        assertThat(logHandler.getLogRecords().get(0).getMessage()).doesNotContain(I18N_DEVICE_NEEDS_DIAL_CALENDAR);
    }

    @Test
    @Ignore
    public void testWithI18NAndParameterReplacement () {
        WithI18N withI18N = LoggerFactory.getLoggerFor(WithI18N.class, LogLevel.DEBUG);
        LoggerFactory.LoggerHolder loggerHolder = (LoggerFactory.LoggerHolder) withI18N;
        LogHandler logHandler = new LogHandler();
        loggerHolder.getLogger().addHandler(logHandler);
        Date now = new DateTime(2012, Calendar.MAY, 2, 1, 42, 0, 0).toDate();

        // Target method
        withI18N.helloWorld("Rudi", now);

        // Asserts
        assertEquals("The number of log records does not match", 1, logHandler.getLogRecords().size());
        assertThat(logHandler.getLogRecords().get(0).getMessage()).doesNotContain(I18N_HELLO_WORLD);
        String expectedMessage = MessageFormat.format(I18N_HELLO_WORLD, "Rudi", now);
        String actualMessage = logHandler.getLogRecords().get(0).getMessage();
        assertEquals("Parameter replacement did not work", expectedMessage, actualMessage);
    }

    @Test
    public void testWithExceptionParameter () {
        ExceptionLogger exceptionLogger = LoggerFactory.getLoggerFor(ExceptionLogger.class, LogLevel.ERROR);
        LoggerFactory.LoggerHolder loggerHolder = (LoggerFactory.LoggerHolder) exceptionLogger;
        LogHandler logHandler = new LogHandler();
        loggerHolder.getLogger().addHandler(logHandler);

        // Target method
        exceptionLogger.error(LoggerFactoryTest.class.getSimpleName(), new IllegalArgumentException("Exception expected as part of unit testing."));

        // Asserts
        assertEquals("The number of log records does not match", 1, logHandler.getLogRecords().size());
        assertNotNull("Exception was not injected!", logHandler.getLogRecords().get(0).getThrown());
        assertTrue("Exception class does not match", logHandler.getLogRecords().get(0).getThrown() instanceof IllegalArgumentException);
    }

    @Test(expected = CodingException.class)
    public void testMultipleExceptionsParameterAreNotSupported () {
        LoggerFactory.getLoggerFor(MultipleExceptions.class, LogLevel.ERROR);

        // Creating the logger should fail because multiple exception parameters are NOT supported
    }

    @Test(expected = CodingException.class)
    public void testCreateFromClass () {
        LoggerFactory.getLoggerFor(MessageClass.class, LogLevel.ERROR);

        // Creating the logger should fail because only interfaces are supported
    }

    @Test
    public void testLogMessageWithNewLines () {
        Logger anonymousLogger = Logger.getAnonymousLogger();
        anonymousLogger.setLevel(Level.FINEST);
        Logger logger = spy(anonymousLogger);
        NewLineLogger newLineLogger = LoggerFactory.getLoggerFor(NewLineLogger.class, logger);

        newLineLogger.errorMessageWithNewLinesBetweenParameters("first", "second");
        verify(logger).log(any(LogRecord.class));
    }

    @Test
    public void testWithExistingLoggerAndCorrectLevel () {
        Logger anonymousLogger = Logger.getAnonymousLogger();
        anonymousLogger.setLevel(Level.SEVERE); // Accept at least SEVERE
        Logger logger = spy(anonymousLogger);
        NoI18N noI18N = LoggerFactory.getLoggerFor(NoI18N.class, logger);
        noI18N.errorMessageWithoutParameters();

        ArgumentCaptor<LogRecord> logRecordArgumentCaptor = ArgumentCaptor.forClass(LogRecord.class);
        verify(logger).log(logRecordArgumentCaptor.capture());
        assertThat(logRecordArgumentCaptor.getValue().getLevel()).isEqualTo(Level.SEVERE);
    }

    @Test
    public void testWithExistingLoggerAndWrongLevel () {
        Logger anonymousLogger = Logger.getAnonymousLogger();
        anonymousLogger.setLevel(Level.SEVERE); // Anything except INFO
        Logger logger = spy(anonymousLogger);
        NoI18N noI18N = LoggerFactory.getLoggerFor(NoI18N.class, logger);
        noI18N.infoMessageWithoutParameters();

        verify(logger, never()).log(any(LogRecord.class));
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

    private class LogHandler extends Handler {
        private List<LogRecord> logRecords = new ArrayList<>();

        @Override
        public void publish (LogRecord record) {
            this.logRecords.add(record);
        }

        public List<LogRecord> getLogRecords () {
            return logRecords;
        }

        @Override
        public void flush () {
            // No implementation required
        }

        @Override
        public void close () throws SecurityException {
            // No implementation required
        }
    }

}