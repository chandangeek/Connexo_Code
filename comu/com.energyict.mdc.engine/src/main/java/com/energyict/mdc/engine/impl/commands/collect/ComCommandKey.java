package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;

/**
 * Serves as a key for a {@link ComCommand} so that it can be used
 * in map like structures to make sure that there is a single
 * ComCommand of a certain {@link ComCommandTypes type}
 * for a unique device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-30 (13:49)
 */
public final class ComCommandKey {

    private final ComCommandType type;
    private final long deviceId;
    private final long comTaskExecutionId;
    private final long securitySetCommandGroupId;

    public ComCommandKey(ComCommandType type, long deviceId, long securitySetCommandGroupId) {
        this(type, deviceId, 0, securitySetCommandGroupId);
    }

    public ComCommandKey(ComCommandType type, ComTaskExecution comTaskExecution, long securitySetCommandGroupId) {
        this(type, extractDeviceIdFrom(comTaskExecution), extractComTaskExecutionIdFrom(comTaskExecution), securitySetCommandGroupId);
    }

    private static long extractDeviceIdFrom(ComTaskExecution comTaskExecution) {
        if (comTaskExecution == null || comTaskExecution.getDevice() == null) {
            return 0L;
        }
        else {
            return comTaskExecution.getDevice().getId();
        }
    }

    private static long extractComTaskExecutionIdFrom(ComTaskExecution comTaskExecution) {
        if (comTaskExecution == null) {
            return 0L;
        }
        else {
            return comTaskExecution.getId();
        }
    }

    private ComCommandKey(ComCommandType type, long deviceId, long comTaskExecutionId, long securitySetCommandGroupId) {
        this.type = type;
        this.deviceId = deviceId;
        this.comTaskExecutionId = comTaskExecutionId;
        this.securitySetCommandGroupId = securitySetCommandGroupId;
    }

    public ComCommandType getCommandType() {
        return type;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public long getComTaskExecutionId() {
        return comTaskExecutionId;
    }

    public long getSecuritySetCommandGroupId() {
        return securitySetCommandGroupId;
    }

    public boolean equalsIgnoreComTaskExecution(ComCommandKey other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }

        if (deviceId != other.deviceId) {
            return false;
        }
        if (securitySetCommandGroupId != other.securitySetCommandGroupId) {
            return false;
        }
        return type == other.type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ComCommandKey that = (ComCommandKey) o;

        if (deviceId != that.deviceId) {
            return false;
        }
        if (comTaskExecutionId != that.comTaskExecutionId) {
            return false;
        }
        if (securitySetCommandGroupId != that.securitySetCommandGroupId) {
            return false;
        }
        return type == that.type;

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (int) (deviceId ^ (deviceId >>> 32));
        result = 31 * result + (int) (comTaskExecutionId ^ (comTaskExecutionId >>> 32));
        result = 31 * result + (int) (securitySetCommandGroupId ^ (securitySetCommandGroupId >>> 32));
        return result;
    }

}