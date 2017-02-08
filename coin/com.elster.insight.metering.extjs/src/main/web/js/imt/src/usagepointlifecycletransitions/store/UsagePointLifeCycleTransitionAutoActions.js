/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitionAutoActions', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointlifecycletransitions.model.UsagePointLifeCycleTransitionAutoAction',
    proxy: {
        type: 'rest',
        url: '/api/upl/lifecycle/microActions',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'microActions'
        },
        setUrl: function (fromState, toState) {
            this.extraParams = {fromState: {id: fromState}, toState: {id: toState}};
        }
    }
});