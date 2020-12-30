/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm;

import java.util.Map;
import java.util.Optional;

public class BpmProcess {
    private String deploymentId;
    private String id;
    private String name;
    private String version;
    private String auth;
    private Map<String, Object> parameters;

    @SuppressWarnings("unused")
    private BpmProcess() {
        // default initializer for jackson
    }

    private BpmProcess(String deploymentId, String id, String name, String version, Map<String, Object> parameters, String auth) {
        this.deploymentId = deploymentId;
        this.id = id;
        this.name = name;
        this.version = version;
        this.parameters = parameters;
        this.auth = auth;
    }

    public static BpmProcess identifiedByDeploymentIdAndId(String deploymentId, String id, Map<String, Object> parameters) {
        return identifiedByDeploymentIdAndId(deploymentId, id, parameters, null);
    }

    public static BpmProcess identifiedByDeploymentIdAndId(String deploymentId, String id, Map<String, Object> parameters, String auth) {
        return new BpmProcess(deploymentId, id, null, null, parameters, auth);
    }

    public static BpmProcess identifiedByProcessNameAndVersion(String processName, String version, Map<String, Object> parameters) {
        return identifiedByProcessNameAndVersion(processName, version, parameters, null);
    }

    public static BpmProcess identifiedByProcessNameAndVersion(String processName, String version, Map<String, Object> parameters, String auth) {
        return new BpmProcess(null, null, processName, version, parameters, auth);
    }

    public Optional<String> getDeploymentId() {
        return Optional.ofNullable(deploymentId);
    }

    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getVersion() {
        return Optional.ofNullable(version);
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public Optional<String> getAuth() {
        return Optional.ofNullable(auth);
    }
}
