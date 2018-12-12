package com.elster.protocolimpl.lis200.commands;

import java.io.IOException;

/**
 * User: heuckeg
 * Date: 23.02.12
 * Time: 11:23
 *
 * Extend IOException to detect empty archives.
 * "Old" software didn't see any difference to old function AbstractCommand.checkResponseForErrors
 */
public class ArchiveEmptyException extends IOException {

    public ArchiveEmptyException(String msg) {
        super(msg);
    }
}
