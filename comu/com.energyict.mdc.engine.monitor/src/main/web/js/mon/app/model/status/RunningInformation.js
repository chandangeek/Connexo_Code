/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.model.status.RunningInformation', {
    extend: 'Ext.data.Model',
    fields: ['numberOfEvents', 'maxMemory', 'usedMemory', 'unit']
});