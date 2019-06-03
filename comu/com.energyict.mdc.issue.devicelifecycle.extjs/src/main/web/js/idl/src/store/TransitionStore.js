/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idl.store.TransitionStore', {
    extend: 'Ext.data.Store',

    fields: [
        //{name: 'deviceType', type: 'auto'},
        {name: 'deviceLifecycle', type: 'auto'},
        {name: 'transition', type: 'auto'},
        {name: 'failedStateChange', type: 'auto'},
        {name: 'cause', type: 'auto'},
        {name: 'modTime', type: 'auto'}
    ]
});