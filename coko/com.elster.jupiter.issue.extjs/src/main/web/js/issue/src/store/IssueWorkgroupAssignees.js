/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.IssueWorkgroupAssignees', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.IssueWorkgroupAssignee',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/isu/workgroups',
        reader: {
            type: 'json',
            root: 'workgroups'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});