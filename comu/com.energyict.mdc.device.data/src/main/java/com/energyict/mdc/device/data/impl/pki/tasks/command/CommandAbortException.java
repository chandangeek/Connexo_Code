package com.energyict.mdc.device.data.impl.pki.tasks.command;

/**
 * this shall be used for a normal use case (not error) when command execution chain should be aborted.
 */
public class CommandAbortException extends Throwable {
    public CommandAbortException(String msg) {
        super(msg);
    }
}
