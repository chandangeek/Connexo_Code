/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.model.ServerDetails', {
    extend: 'Ext.data.Model',
    fields: ['serverName', 'serverId', 'started', 'duration', 'currentDate']
});