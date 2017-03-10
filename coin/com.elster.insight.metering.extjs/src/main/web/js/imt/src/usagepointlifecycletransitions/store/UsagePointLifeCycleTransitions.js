/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitions', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointlifecycletransitions.model.UsagePointLifeCycleTransition',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/upl/lifecycle/{id}/transitions',
        reader: {
            type: 'json',
            root: 'transitions'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{id}', params.usagePointLifeCycleId);
        }
    }
});