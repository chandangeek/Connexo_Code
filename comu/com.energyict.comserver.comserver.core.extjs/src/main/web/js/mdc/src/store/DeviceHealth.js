Ext.define('Mdc.store.DeviceHealth', {
    extend: 'Ext.data.Store',
    requires: [
    ],
    fields: [
        'type',
        'id',
        'description',
        'due date',
        'severity',
        'assignee',
        {
            name: 'displayValue',
            mapping: function(record){
                return {
                    type: record.type,
                    id: record.id,
                    description: record.description,
                    dueDate: record.dueDate,
                    severity: record.severity,
                    assignee: record.assignee
                }
            }
        }

    ],
    storeId: 'DeviceHealth',
    data: [
        {
            "type": "issue",
            "id": 145,
            "description": "Connection failed",
            "due date": 1458573933000,
            "severity": "oeioeioei"
        },
        {
            "type": "servicecall",
            "id": 416,
            "description": "SC_00012301 (SAP MDUS)"
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