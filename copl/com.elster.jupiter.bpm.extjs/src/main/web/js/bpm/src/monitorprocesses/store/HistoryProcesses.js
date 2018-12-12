/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorprocesses.store.HistoryProcesses', {
    extend: 'Ext.data.Store',
    model: 'Bpm.monitorprocesses.model.HistoryProcess',

    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/bpm/runtime/historyprocesses?variableid={variableid}&variablevalue={variablevalue}',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'processHistories'
        },
        setUrl: function (variableid, variablevalue) {
            this.url = this.urlTpl.replace('{variableid}', variableid)
                .replace('{variablevalue}', encodeURIComponent(variablevalue));
        }
    },
    listeners: {
        beforeload: function (store, options) {
            var me = this,
                queryString = Uni.util.QueryString.getQueryStringValues(false),
                params = {};

            options.params = options.params || {};

            var result = [];
            for (var dataIndex in queryString) {
                if (dataIndex === 'activeTab') {
                    continue;
                }
                var value = queryString[dataIndex];

                if (queryString.hasOwnProperty(dataIndex) && Ext.isDefined(value) && !Ext.isEmpty(value)) {
                    if (!Ext.isArray(value)) {
                        value = [value];
                    }
                    var filter = {
                        property: dataIndex,
                        value: value
                    };

                    result.push(filter);
                }
            }
            params.filter = Ext.encode(result);
            Ext.apply(options.params, params);
        }
    }

});
