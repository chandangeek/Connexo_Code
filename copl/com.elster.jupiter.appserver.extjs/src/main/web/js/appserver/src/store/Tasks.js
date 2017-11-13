/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.store.Tasks', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.Task',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/tsk/task',
        reader: {
            type: 'json',
            root: 'tasks'
        }
    },
    listeners: {
        'beforeLoad': function () {
            var extraParams = this.proxy.extraParams;
            // replace filter extra params with new ones
            //      if (this.proxyFilter) {
            //          extraParams = _.omit(extraParams, this.proxyFilter.getFields());
            //          Ext.merge(extraParams, this.getFilterParams());
            //      }

            //     this.proxy.extraParams = extraParams;
        }
    },
});