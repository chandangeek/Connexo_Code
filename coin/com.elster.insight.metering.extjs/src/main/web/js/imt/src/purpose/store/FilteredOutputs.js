/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.store.FilteredOutputs', {
    extend: 'Ext.data.Store',
    model: 'Imt.purpose.model.Output',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/outputs',
        reader: {
            type: 'json',
            root: 'outputs'
        }
    },
    storeFilter: '',
    listeners: {
        beforeload: function (store, operation, eOpts) {
            operation.params = operation.params || {};
            if (store.storeFilter) {
                Ext.apply(operation.params, {filter: store.storeFilter});
            }
        }
    }
});