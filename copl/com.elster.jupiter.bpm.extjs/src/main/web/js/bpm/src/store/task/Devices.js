/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Bpm.store.task.Devices', {
    extend: 'Ext.data.Store',
    model: 'Bpm.model.task.Device',
    pageSize: 50,
    autoLoad: false,

    proxy: {
        type: 'rest',
        mdmUrl: '/api/udr/usagepoints',
        mdcUrl: '/api/ddr/devices',
        mdmReaderRoot: 'usagePoints',
        mdcReaderRoot: 'devices',

        setMdmUrl: function () {
            this.url = this.mdmUrl;
            this.setReader({
                type: 'json',
                root: 'usagePoints'
            });
        },
        setMdcUrl: function () {
            this.url = this.mdcUrl;
            this.setReader({
                type: 'json',
                root: 'devices'
            });
        }
    }
});