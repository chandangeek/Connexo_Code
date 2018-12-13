/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.store.task.TaskWorkgroupAssignees', {
    extend: 'Ext.data.Store',
    model: 'Bpm.model.task.TaskWorkgroupAssignee',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/bpm/workgroups',
        reader: {
            type: 'json',
            root: 'workgroups'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});