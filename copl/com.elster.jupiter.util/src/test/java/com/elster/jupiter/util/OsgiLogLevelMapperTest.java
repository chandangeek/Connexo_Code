package com.elster.jupiter.util;

import org.junit.Test;
import org.osgi.service.log.LogService;

import java.util.logging.Level;

import static org.fest.assertions.api.Assertions.assertThat;

public class OsgiLogLevelMapperTest {

    @Test
    public void testMapLevelFinest() {
        assertThat(OsgiLogLevelMapper.mapLevel(Level.FINEST)).isEqualTo(LogService.LOG_DEBUG);
    }

    @Test
    public void testMapLevelFiner() {
        assertThat(OsgiLogLevelMapper.mapLevel(Level.FINER)).isEqualTo(LogService.LOG_DEBUG);
    }

    @Test
    public void testMapLevelFine() {
        assertThat(OsgiLogLevelMapper.mapLevel(Level.FINE)).isEqualTo(LogService.LOG_DEBUG);
    }

    @Test
    public void testMapLevelInfo() {
        assertThat(OsgiLogLevelMapper.mapLevel(Level.INFO)).isEqualTo(LogService.LOG_INFO);
    }

    @Test
    public void testMapLevelWarning() {
        assertThat(OsgiLogLevelMapper.mapLevel(Level.WARNING)).isEqualTo(LogService.LOG_WARNING);
    }

    @Test
    public void testMapLevelAll() {
        assertThat(OsgiLogLevelMapper.mapLevel(Level.ALL)).isEqualTo(LogService.LOG_DEBUG);
    }

    @Test
    public void testMapLevelSevere() {
        assertThat(OsgiLogLevelMapper.mapLevel(Level.SEVERE)).isEqualTo(LogService.LOG_ERROR);
    }

    @Test
    public void testMapLevelConfig() {
        assertThat(OsgiLogLevelMapper.mapLevel(Level.CONFIG)).isEqualTo(LogService.LOG_INFO);
    }

    @Test
    public void testMapLevelOff() {
        assertThat(OsgiLogLevelMapper.mapLevel(Level.OFF)).isEqualTo(LogService.LOG_ERROR);
    }

}
