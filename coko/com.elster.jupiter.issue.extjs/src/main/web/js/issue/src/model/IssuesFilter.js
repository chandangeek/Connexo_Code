/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.model.IssuesFilter', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.data.proxy.QueryStringProxy'
    ],
    fields: [
        'id',
        'issueType',
        'status',
        'userAssignee',
        'workGroupAssignee',
        'reason',
        'meter',
        'dueDate',
        'grouping',
        'sorting'
    ],

    proxy: {
        type: 'querystring',
        root: 'filter',
        destroy: function () {
            var filter = new this.router.filter.self({
                grouping: this.router.filter.get('grouping'),
                sorting: this.router.filter.get('sorting')
            });
            filter.save();
        }
    }
});