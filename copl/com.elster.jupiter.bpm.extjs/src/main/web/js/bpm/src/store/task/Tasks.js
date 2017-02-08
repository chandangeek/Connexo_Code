/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.store.task.Tasks', {
    extend: 'Uni.data.store.Filterable',
    model: 'Bpm.model.task.Task',
    //autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/tasks',
        reader: {
            type: 'json',
            root: 'tasks'
        }
    },
    listeners : {
        beforeload: function (store, options) {
            var me = this,
                queryString = Uni.util.QueryString.getQueryStringValues(false),
                params = {};

            options.params = options.params || {};
            if (queryString.sort) {
                params.sort = queryString.sort;
            }

            var result = [];
            for (var dataIndex in queryString) {
                if (dataIndex === 'sort'){
                    continue;
                }
                var value = queryString[dataIndex];

                if (queryString.hasOwnProperty(dataIndex) && Ext.isDefined(value) && !Ext.isEmpty(value)) {
                    if(!Ext.isArray(value)){
                        value = [value];
                    }
                    var filter = {
                        property: dataIndex,
                        value: value
                    };

                    result.push(filter);
                }
            }
            if (result.length > 0) {
                params.filter = Ext.encode(result);
            }
            Ext.apply(options.params, params);
        }
    }
});
