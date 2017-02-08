/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.model.opendatacollectionissues.Overview', {
    extend: 'Ext.data.Model',
    requires: [
        'Dsh.model.AssignedToMeIssues',
        'Dsh.model.UnassignedIssues'
    ],
    hasOne: [
        {
            model: 'Dsh.model.UnassignedIssues',
            associationKey: 'unassignedIssues',
            name: 'unassignedIssues',
            getterName: 'getUnassignedIssues',
            setterName: 'setUnassignedIssues'
        },
        {
            model: 'Dsh.model.AssignedToMeIssues',
            associationKey: 'assignedToMeIssues',
            name: 'assignedToMeIssues',
            getterName: 'getAssignedToMeIssues',
            setterName: 'setAssignedToMeIssues'
        }
    ],

    proxy: {
        type: 'ajax',
        url: '/api/dsr/myopenissuesoverview'
    }
});