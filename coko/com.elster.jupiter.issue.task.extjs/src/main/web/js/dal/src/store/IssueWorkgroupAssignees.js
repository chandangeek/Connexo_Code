/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.store.IssueWorkgroupAssignees', {
    extend: 'Ext.data.Store',
    model: 'Itk.model.IssueWorkgroupAssignee',
    proxy: {
        type: 'rest',
        url: '/api/itk/workgroups',
        reader: {
            type: 'json',
            root: 'workgroups'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});