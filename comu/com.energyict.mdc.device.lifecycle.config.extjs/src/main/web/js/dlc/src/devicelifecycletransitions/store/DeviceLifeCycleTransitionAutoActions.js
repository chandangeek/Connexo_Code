/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionAutoActions', {
    extend: 'Ext.data.Store',
    model: 'Dlc.devicelifecycletransitions.model.DeviceLifeCycleTransitionAutoAction',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/dld/devicelifecycles/{id}/actions/microactions',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'microActions'
        },
        setUrl: function (params, fromState, toState) {
            this.url = this.urlTpl.replace('{id}', params.deviceLifeCycleId);
            this.extraParams = {fromState: {id: fromState}, toState: {id: toState}};
        }
    },
    remoteFilter: false,
    listeners: {
        load: function() {
            var me = this,
                insRules = ['LINK_TO_USAGE_POINT'];

            if (Uni.store.Apps.checkApp('Insight')) {
                me.filterBy(function(r){
                    return _.indexOf(insRules, r.get('key')) < 0
                })
            }
        }
    }
});