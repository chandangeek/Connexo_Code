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
//        url: '../dsh/src/fakeData/MyOpenDataCollectionIssues.json'
        url: '/api/dsr/myopenissuesoverview'
    }
});