/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointlifecycletransitions.model.UsagePointLifeCycleTransition', {
    extend: 'Uni.model.ParentVersion',
    fields: [
        'id',
        'name',
        'fromState',
        'toState',
        'privileges',
        {name: 'microActions', defaultValue: null},
        {name: 'microChecks', defaultValue: null},
        {
            name: 'fromState_name',
            persist: false,
            mapping: function (data) {
                if (data.fromState) {
                    return data.fromState.name;
                }
            }
        },
        {
            name: 'toState_name',
            persist: false,
            mapping: function (data) {
                if (data.toState) {
                    return data.toState.name;
                }
            }
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/upl/lifecycle/{id}/transitions',
        reader: {
            type: 'json'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{id}', params.usagePointLifeCycleId);
        }
    }
});
