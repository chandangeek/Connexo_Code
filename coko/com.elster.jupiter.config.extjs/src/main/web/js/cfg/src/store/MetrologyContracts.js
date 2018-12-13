/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.store.MetrologyContracts', {
    extend: 'Ext.data.Store',
    requires: [
        'Cfg.model.MetrologyContract'
    ],
    model: 'Cfg.model.MetrologyContract',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/val/field/metrologyconfigurations/{configId}/contracts',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'metrologyContracts'
        },

        setUrl: function (configId) {
            this.url = this.urlTpl.replace('{configId}', configId);
        }
    }
});
