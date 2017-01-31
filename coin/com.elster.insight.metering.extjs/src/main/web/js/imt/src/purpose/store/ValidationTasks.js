/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.store.ValidationTasks', {
    extend: 'Ext.data.Store',
    model: 'Cfg.model.ValidationTask',
    autoLoad: false,

    proxy: {
        type: 'rest',
        tplUrl: '/api/udr/usagepoints/{mRID}/validationtasks',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'dataValidationTasks'
        },
        setUrl: function (mRID) {
            this.url = this.tplUrl.replace('{mRID}', encodeURIComponent(mRID))
        }
    }
});
