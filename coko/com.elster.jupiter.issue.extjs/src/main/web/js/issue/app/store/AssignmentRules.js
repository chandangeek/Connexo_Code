Ext.define('Mtr.store.AssignmentRules', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Mtr.model.AssignmentRules',
    pageSize: 100,
    autoLoad: false,

    /* proxy: {
     type: 'rest',
     url: '/api/isu/rules',
     reader: {
     type: 'json',
     root: 'rules'
     }
     }*/

    /*=========== TEMP ===========*/

    data: [
        {
            "id": 1,
            "name": "When smth then assign to user",
            "priority": 1,
            "status": "enabled",
            "assignee": {
                "id": 1,
                "type": "USER",
                "title": "Monique"
            },
            "version": 1
        },
        {
            "id": 2,
            "name": "When smth then assign to group",
            "priority": 2,
            "status": "enabled",
            "assignee": {
                "id": 2,
                "type": "GROUP",
                "title": "USA Operators"
            },
            "version": 1
        },
        {
            "id": 3,
            "name": "When smth then assign to Role",
            "priority": 3,
            "status": "disabled",
            "assignee": {
                "id": 3,
                "type": "ROLE",
                "title": "Meter operator"
            },
            "version": 1
        }
    ]
});
