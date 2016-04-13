package com.elster.jupiter.orm;

import org.flywaydb.core.api.MigrationVersion;

import java.util.Arrays;
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

    public static Version version(int... parts) {
        return version(Arrays.stream(parts).mapToObj(Integer::toString).collect(Collectors.joining(".")));
    }

    @Override
    public int compareTo(Version version) {
        if (version instanceof Version) {
            return migrationVersion.compareTo(((Version) version).migrationVersion);
        }
        return migrationVersion.compareTo(MigrationVersion.fromVersion(version.toString()));
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
}
    
