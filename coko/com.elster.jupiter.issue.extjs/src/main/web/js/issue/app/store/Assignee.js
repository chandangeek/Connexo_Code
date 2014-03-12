Ext.define('Isu.store.Assignee', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Isu.model.Assignee',
    pageSize: 100,
    autoLoad: false,

    data: [
        {
            "id": 1,
            "type": "USER",
            "title": "Monique"
        },
        {
            "id": 2,
            "type": "GROUP",
            "title": "USA Operators"
        },
        {
            "id": 3,
            "type": "ROLE",
            "title": "Meter operator"
        }
    ]
});
