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
        buildUrl: function (request) {
            var idx = request.params.id,
                params;

            if (idx) {
                return this.url + '/' + request.params.id;
            } else {
                return this.url
            }
        }
    }
});