Ext.define('Mdc.store.DeviceHealth', {
    extend: 'Ext.data.Store',
    requires: [
    ],
    fields: [
        'name',
        'type',
        'severity',
        {
            name: 'displayValue',
            mapping: function(record){
                return {
                    name: record.name,
                    type: record.type,
                    severity: record.severity
                }
            }
        }

    ],
    storeId: 'DeviceHealth',
    data: [
        {
            name: 'aaaaaaaaaaaaaaaa',
            type: 'issue',
            severity: 'info'
        },
        {
            name: 'bbbbbbbbbbbbbbbb',
            type: 'process',
            severity: 'warning'
        },
        {
            name: 'cccccccccccccccc',
            type: 'servicecall',
            severity: 'severe'
        },
        {
            name: 'ddddddddddddddddd',
            type: 'alarm',
            severity: 'info'
        },
        {
            name: 'eeeeeeeeeeeeeeee',
            type: 'issue',
            severity: 'warning'
        },
        {
            name: 'fffffffffffffff',
            type: 'issue',
            severity: 'severe'
        },
        {
            name: 'gggggggggggggggggg',
            type: 'issue',
            severity: 'info'
        },
        {
            name: 'hhhhhhhhhhhhhhhhh',
            type: 'issue',
            severity: 'info'
        },
        {
            name: 'iiiiiiiiiiiiiiii',
            type: 'issue',
            severity: 'info'
        },
        {
            name: 'jjjjjjjjjjjjjjjjjjj',
            type: 'issue',
            severity: 'info'
        },
        {
            name: 'kkkkkkkkkkkkkkkkk',
            type: 'issue',
            severity: 'info'
        },
        {
            name: 'lllllllllllllll',
            type: 'issue',
            severity: 'info'
        },
        {
            name: 'mmmmmmmmmmmmmmmmm',
            type: 'issue',
            severity: 'info'
        },
        {
            name: 'nnnnnnnnnnnnnnnnn',
            type: 'issue',
            severity: 'info'
        },
        {
            name: 'ooooooooooooooooo',
            type: 'issue',
            severity: 'info'
        }
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