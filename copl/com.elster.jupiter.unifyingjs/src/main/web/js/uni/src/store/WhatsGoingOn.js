Ext.define('Uni.store.WhatsGoingOn', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.WhatsGoingOn',
    requires: [
        'Uni.model.WhatsGoingOn'
    ],
    //constructor: function(){
    //    this.callParent(arguments);
    //    this.model.prototype.idProperty = 'internalId';
    //},

    storeId: 'whatsgoingon',
    mrId: null,
    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
           //root: 'users'
        }
    },
    data: [
        {
            "type": "servicecall",
            "id": 145,
            "description": "Connection failed",
            "dueDate": 1458573933000,
            "severity": "high",
            "status": "active"
        },
        {
            "type": "issue",
            "id": 145,
            "description": "Connection failed",
            "due date": 1458573933000,
            "severity": "warning"
        },
        {
            "type": "servicecall",
            "id": 415,
            "description": "SC_00012301 (SAP MDUS)"
        },
        {
            "type": "servicecall",
            "id": 416,
            "description": "SC_00012301 (SAP MDUS)",
            "severity": "high",
            "assignee": "Pol",
            "assigneeIsCurrentUser": true
        },
        {
            "type": "issue",
            "id": 152,
            "description": "Connection failed",
            "dueDate": 1458573933000,
            "severity": "high",
            "status": "active"
        },
        {
            "type": "issue",
            "id": 153,
            "description": "Connection failed",
            "due date": 1458573933000,
            "severity": "warning"
        },
        {
            "type": "servicecall",
            "id": 154,
            "description": "SC_00012301 (SAP MDUS)"
        },
        {
            "type": "servicecall",
            "id": 155,
            "description": "SC_00012301 (SAP MDUS)",
            "severity": "high",
            "assignee": "Pol",
            "assigneeIsCurrentUser": true
        }
        //{
        //    name: 'aaaaaaaaaaaaaaaa',
        //    type: 'issue',
        //    severity: 'info'
        //},
        //{
        //    name: 'bbbbbbbbbbbbbbbb',
        //    type: 'process',
        //    severity: 'warning'
        //},
        //{
        //    name: 'cccccccccccccccc',
        //    type: 'servicecall',
        //    severity: 'severe'
        //},
        //{
        //    name: 'ddddddddddddddddd',
        //    type: 'alarm',
        //    severity: 'info'
        //},
        //{
        //    name: 'eeeeeeeeeeeeeeee',
        //    type: 'issue',
        //    severity: 'warning'
        //},
        //{
        //    name: 'fffffffffffffff',
        //    type: 'issue',
        //    severity: 'severe'
        //},
        //{
        //    name: 'gggggggggggggggggg',
        //    type: 'issue',
        //    severity: 'info'
        //},
        //{
        //    name: 'hhhhhhhhhhhhhhhhh',
        //    type: 'issue',
        //    severity: 'info'
        //},
        //{
        //    name: 'iiiiiiiiiiiiiiii',
        //    type: 'issue',
        //    severity: 'info'
        //},
        //{
        //    name: 'jjjjjjjjjjjjjjjjjjj',
        //    type: 'issue',
        //    severity: 'info'
        //},
        //{
        //    name: 'kkkkkkkkkkkkkkkkk',
        //    type: 'issue',
        //    severity: 'info'
        //},
        //{
        //    name: 'lllllllllllllll',
        //    type: 'issue',
        //    severity: 'info'
        //},
        //{
        //    name: 'mmmmmmmmmmmmmmmmm',
        //    type: 'issue',
        //    severity: 'info'
        //},
        //{
        //    name: 'nnnnnnnnnnnnnnnnn',
        //    type: 'issue',
        //    severity: 'info'
        //},
        //{
        //    name: 'ooooooooooooooooo',
        //    type: 'issue',
        //    severity: 'info'
        //}
    ]
    /*sorters: [{
     property: 'name',
     direction: 'ASC'
     }],  */
    //proxy: {
    //    type: 'rest',
    //    url: '../../api/ddr/devicegroups',
    //    reader: {
    //        type: 'json',
    //        root: 'devicegroups'
    //    }
    //}
});