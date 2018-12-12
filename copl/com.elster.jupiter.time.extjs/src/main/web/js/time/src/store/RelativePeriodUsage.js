/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tme.store.RelativePeriodUsage', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Tme.model.RelativePeriodUsage',
    proxy: {
        type: 'rest',
        urlTpl: '/api/tmr/relativeperiods/{periodId}/usage',
        reader: {
            type: 'json',
            root: 'usage'
        },

        setUrl: function (periodId) {
            this.url = this.urlTpl.replace('{periodId}', periodId);
        }
    }
});
