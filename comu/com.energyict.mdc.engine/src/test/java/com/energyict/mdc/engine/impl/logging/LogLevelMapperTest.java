package com.energyict.mdc.engine.impl.logging;

import com.energyict.mdc.engine.model.ComServer;

import java.util.logging.Level;

import org.junit.*;

import static junit.framework.Assert.assertEquals;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.logging.LogLevelMapper} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-15 (09:59)
 */
public class LogLevelMapperTest {

    @Test
    public void testToComServerLogLevel () {
        assertEquals(LogLevel.INFO, LogLevelMapper.toComServerLogLevel(Level.INFO));
        assertEquals(LogLevel.WARN, LogLevelMapper.toComServerLogLevel(Level.WARNING));
        assertEquals(LogLevel.ERROR, LogLevelMapper.toComServerLogLevel(Level.SEVERE));
        assertEquals(LogLevel.DEBUG, LogLevelMapper.toComServerLogLevel(Level.FINE));
        assertEquals(LogLevel.DEBUG, LogLevelMapper.toComServerLogLevel(Level.FINER));
        assertEquals(LogLevel.TRACE, LogLevelMapper.toComServerLogLevel(Level.FINEST));
        assertEquals(LogLevel.INFO, LogLevelMapper.toComServerLogLevel(Level.OFF));
    }

    @Test
    public void testComServerLogLevelToCommonLogLevel () {
        assertEquals(LogLevel.INFO, LogLevelMapper.map(ComServer.LogLevel.INFO));
        assertEquals(LogLevel.WARN, LogLevelMapper.map(ComServer.LogLevel.WARN));
        assertEquals(LogLevel.ERROR, LogLevelMapper.map(ComServer.LogLevel.ERROR));
        assertEquals(LogLevel.DEBUG, LogLevelMapper.map(ComServer.LogLevel.DEBUG));
        assertEquals(LogLevel.TRACE, LogLevelMapper.map(ComServer.LogLevel.TRACE));
    }

    @Test
    public void testToJavaUtilLogLevel () {
        assertEquals(Level.INFO, LogLevelMapper.toJavaUtilLogLevel(LogLevel.INFO));
        assertEquals(Level.WARNING, LogLevelMapper.toJavaUtilLogLevel(LogLevel.WARN));
        assertEquals(Level.SEVERE, LogLevelMapper.toJavaUtilLogLevel(LogLevel.ERROR));
        assertEquals(Level.FINE, LogLevelMapper.toJavaUtilLogLevel(LogLevel.DEBUG));
        assertEquals(Level.FINEST, LogLevelMapper.toJavaUtilLogLevel(LogLevel.TRACE));
    }

}