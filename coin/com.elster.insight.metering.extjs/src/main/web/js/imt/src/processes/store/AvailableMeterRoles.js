/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.store.AvailableMeterRoles', {
    extend: 'Ext.data.Store',
    model: 'Imt.processes.model.MeterRole',
    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{usagePointId}/availablemeterroles/{timestamp}',
        reader: {
            type: 'json',
            root: 'meterRoles'
        },
        setUrl: function (usagePointId, timestamp) {
            this.url = this.urlTpl.replace('{usagePointId}', usagePointId).replace('{timestamp}', timestamp);
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
