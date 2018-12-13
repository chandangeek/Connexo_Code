/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.model.AlarmAssignee', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'auto'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/dal/assignees',
        reader: {
            type: 'json',
            root: 'data'
        },
        buildUrl: function(request) {
            var idx = request.params.id,
                me = this,
                url = this.url;
            if (idx) {
                url = this.url + '/' + request.params.id;
            }

            if (me.noCache) {
                url = Ext.urlAppend(url, Ext.String.format("{0}={1}", me.cacheString, Ext.Date.now()));
            }

            request.url = url;

            return url;
        }
    }
});