/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
/**
 * An event that is fired when an exception is received from a {@link Ext.direct.RemotingProvider}
 */
Ext.define('Ext.direct.ExceptionEvent', {
    extend: 'Ext.direct.RemotingEvent',
    alias:  'direct.exception',
   
   status: false
});
