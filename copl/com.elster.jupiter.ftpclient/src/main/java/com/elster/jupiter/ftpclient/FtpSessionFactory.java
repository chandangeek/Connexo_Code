/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ftpclient;

import java.io.IOException;

public interface FtpSessionFactory {

    void runInSession(IOConsumer ftpSessionBehavior) throws IOException;
}
