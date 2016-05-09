package com.elster.jupiter.orm;

import org.flywaydb.core.api.MigrationVersion;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;


public final class Version implements Comparable<Version> {


    private static final Version LATEST = new Version(MigrationVersion.LATEST);
    private static final Version EARLIEST = new Version(MigrationVersion.EMPTY);
    private final MigrationVersion migrationVersion;

    private Version(MigrationVersion migrationVersion) {
        this.migrationVersion = migrationVersion;
    }

    public static Version version(String version) {
        return new Version(MigrationVersion.fromVersion(version));
    }

    public static Version version(int major, int... parts) {
        return version(Arrays.stream(parts).mapToObj(Integer::toString).collect(Collectors.joining(".")));
    }

    @Override
    public int compareTo(Version version) {
        return migrationVersion.compareTo(version.migrationVersion);
    }

    @Override
    public String toString() {
        return migrationVersion.toString();
    }

    public static Version latest() {
        return LATEST;
    }

    public static Version earliest() {
        return EARLIEST;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Version version = (Version) o;
        return Objects.equals(migrationVersion, version.migrationVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(migrationVersion);
    }
}
    
