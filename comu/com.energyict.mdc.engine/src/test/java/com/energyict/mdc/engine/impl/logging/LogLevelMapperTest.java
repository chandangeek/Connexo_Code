/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.logging;

import com.energyict.mdc.engine.config.ComServer;

import java.util.logging.Level;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link LogLevelMapper} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-15 (09:59)
 */
public class LogLevelMapperTest {

    @Test
    public void testForJavaUtilLoggingToLogLevel () {
        assertThat(LogLevelMapper.forJavaUtilLogging().toLogLevel(Level.INFO)).isEqualTo(LogLevel.INFO);
        assertThat(LogLevelMapper.forJavaUtilLogging().toLogLevel(Level.WARNING)).isEqualTo(LogLevel.WARN);
        assertThat(LogLevelMapper.forJavaUtilLogging().toLogLevel(Level.SEVERE)).isEqualTo(LogLevel.ERROR);
        assertThat(LogLevelMapper.forJavaUtilLogging().toLogLevel(Level.FINE)).isEqualTo(LogLevel.DEBUG);
        assertThat(LogLevelMapper.forJavaUtilLogging().toLogLevel(Level.FINER)).isEqualTo(LogLevel.DEBUG);
        assertThat(LogLevelMapper.forJavaUtilLogging().toLogLevel(Level.FINEST)).isEqualTo(LogLevel.TRACE);
        assertThat(LogLevelMapper.forJavaUtilLogging().toLogLevel(Level.OFF)).isEqualTo(LogLevel.INFO);
    }

    @Test
    public void testForJavaUtilLoggingToComServerLogLevel () {
        assertThat(LogLevelMapper.forJavaUtilLogging().toComServerLogLevel(Level.INFO)).isEqualTo(ComServer.LogLevel.INFO);
        assertThat(LogLevelMapper.forJavaUtilLogging().toComServerLogLevel(Level.WARNING)).isEqualTo(ComServer.LogLevel.WARN);
        assertThat(LogLevelMapper.forJavaUtilLogging().toComServerLogLevel(Level.SEVERE)).isEqualTo(ComServer.LogLevel.ERROR);
        assertThat(LogLevelMapper.forJavaUtilLogging().toComServerLogLevel(Level.FINE)).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(LogLevelMapper.forJavaUtilLogging().toComServerLogLevel(Level.FINER)).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(LogLevelMapper.forJavaUtilLogging().toComServerLogLevel(Level.FINEST)).isEqualTo(ComServer.LogLevel.TRACE);
        assertThat(LogLevelMapper.forJavaUtilLogging().toComServerLogLevel(Level.OFF)).isEqualTo(ComServer.LogLevel.INFO);
    }

    @Test
    public void testForJavaUtilLoggingFromComServerLogLevel() {
        assertThat(LogLevelMapper.forJavaUtilLogging().fromComServerLogLevel(ComServer.LogLevel.INFO)).isEqualTo(Level.INFO);
        assertThat(LogLevelMapper.forJavaUtilLogging().fromComServerLogLevel(ComServer.LogLevel.WARN)).isEqualTo(Level.WARNING);
        assertThat(LogLevelMapper.forJavaUtilLogging().fromComServerLogLevel(ComServer.LogLevel.ERROR)).isEqualTo(Level.SEVERE);
        assertThat(LogLevelMapper.forJavaUtilLogging().fromComServerLogLevel(ComServer.LogLevel.DEBUG)).isEqualTo(Level.FINE);
        assertThat(LogLevelMapper.forJavaUtilLogging().fromComServerLogLevel(ComServer.LogLevel.TRACE)).isEqualTo(Level.FINEST);
    }

    @Test
    public void testForJavaUtilLoggingLevelToCommonLogLevel () {
        assertThat(LogLevelMapper.forJavaUtilLogging().fromLogLevel(LogLevel.INFO)).isEqualTo(Level.INFO);
        assertThat(LogLevelMapper.forJavaUtilLogging().fromLogLevel(LogLevel.WARN)).isEqualTo(Level.WARNING);
        assertThat(LogLevelMapper.forJavaUtilLogging().fromLogLevel(LogLevel.ERROR)).isEqualTo(Level.SEVERE);
        assertThat(LogLevelMapper.forJavaUtilLogging().fromLogLevel(LogLevel.DEBUG)).isEqualTo(Level.FINE);
        assertThat(LogLevelMapper.forJavaUtilLogging().fromLogLevel(LogLevel.TRACE)).isEqualTo(Level.FINEST);
    }

    @Test
    public void testForComServerLogLevelToCommonLogLevel () {
        assertThat(LogLevelMapper.forComServerLogLevel().toLogLevel(ComServer.LogLevel.INFO)).isEqualTo(LogLevel.INFO);
        assertThat(LogLevelMapper.forComServerLogLevel().toLogLevel(ComServer.LogLevel.WARN)).isEqualTo(LogLevel.WARN);
        assertThat(LogLevelMapper.forComServerLogLevel().toLogLevel(ComServer.LogLevel.ERROR)).isEqualTo(LogLevel.ERROR);
        assertThat(LogLevelMapper.forComServerLogLevel().toLogLevel(ComServer.LogLevel.DEBUG)).isEqualTo(LogLevel.DEBUG);
        assertThat(LogLevelMapper.forComServerLogLevel().toLogLevel(ComServer.LogLevel.TRACE)).isEqualTo(LogLevel.TRACE);
    }

    @Test
    public void testForComServerLogLevelFromCommonLogLevel () {
        assertThat(LogLevelMapper.forComServerLogLevel().fromLogLevel(LogLevel.INFO)).isEqualTo(ComServer.LogLevel.INFO);
        assertThat(LogLevelMapper.forComServerLogLevel().fromLogLevel(LogLevel.WARN)).isEqualTo(ComServer.LogLevel.WARN);
        assertThat(LogLevelMapper.forComServerLogLevel().fromLogLevel(LogLevel.ERROR)).isEqualTo(ComServer.LogLevel.ERROR);
        assertThat(LogLevelMapper.forComServerLogLevel().fromLogLevel(LogLevel.DEBUG)).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(LogLevelMapper.forComServerLogLevel().fromLogLevel(LogLevel.TRACE)).isEqualTo(ComServer.LogLevel.TRACE);
    }

    @Test
    public void testForComServerLogLevelToJavaUtilLoggingLevel () {
        assertThat(LogLevelMapper.forComServerLogLevel().toJavaUtilLogLevel(ComServer.LogLevel.INFO)).isEqualTo(Level.INFO);
        assertThat(LogLevelMapper.forComServerLogLevel().toJavaUtilLogLevel(ComServer.LogLevel.WARN)).isEqualTo(Level.WARNING);
        assertThat(LogLevelMapper.forComServerLogLevel().toJavaUtilLogLevel(ComServer.LogLevel.ERROR)).isEqualTo(Level.SEVERE);
        assertThat(LogLevelMapper.forComServerLogLevel().toJavaUtilLogLevel(ComServer.LogLevel.DEBUG)).isEqualTo(Level.FINE);
        assertThat(LogLevelMapper.forComServerLogLevel().toJavaUtilLogLevel(ComServer.LogLevel.TRACE)).isEqualTo(Level.FINEST);
    }

    @Test
    public void testForComServerLogLevelFromJavaUtilLoggingLevel() {
        assertThat(LogLevelMapper.forComServerLogLevel().fromJavaUtilLogLevel(Level.INFO)).isEqualTo(ComServer.LogLevel.INFO);
        assertThat(LogLevelMapper.forComServerLogLevel().fromJavaUtilLogLevel(Level.WARNING)).isEqualTo(ComServer.LogLevel.WARN);
        assertThat(LogLevelMapper.forComServerLogLevel().fromJavaUtilLogLevel(Level.SEVERE)).isEqualTo(ComServer.LogLevel.ERROR);
        assertThat(LogLevelMapper.forComServerLogLevel().fromJavaUtilLogLevel(Level.FINE)).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(LogLevelMapper.forComServerLogLevel().fromJavaUtilLogLevel(Level.FINER)).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(LogLevelMapper.forComServerLogLevel().fromJavaUtilLogLevel(Level.FINEST)).isEqualTo(ComServer.LogLevel.TRACE);
    }

}