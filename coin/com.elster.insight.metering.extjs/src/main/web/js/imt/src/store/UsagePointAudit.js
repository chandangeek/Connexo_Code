/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.store.UsagePointAudit', {
    extend: 'Ext.data.Store',
    require: [
        'Cfg.audit.model.Audit'
    ],
    model: 'Cfg.audit.model.Audit',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{usagePointId}/history/audit',
        reader: {
            type: 'json',
            root: 'audit'
        },
        setUrl: function (usagePointId) {
            this.url = this.urlTpl.replace('{usagePointId}', usagePointId);
        }
    }
});
