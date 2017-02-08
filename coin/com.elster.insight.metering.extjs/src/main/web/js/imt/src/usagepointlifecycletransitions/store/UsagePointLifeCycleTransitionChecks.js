/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitionChecks', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointlifecycletransitions.model.UsagePointLifeCycleTransitionCheck',
    proxy: {
        type: 'rest',
        url: '/api/upl/lifecycle/microChecks',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'microChecks'
        },
        setUrl: function (fromState, toState) {
            this.extraParams = {fromState: {id: fromState}, toState: {id: toState}};
        }
    }
});