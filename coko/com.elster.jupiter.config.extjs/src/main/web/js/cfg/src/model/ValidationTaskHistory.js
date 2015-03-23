Ext.define('Cfg.model.ValidationTaskHistory', {
    extend: 'Ext.data.Model',
    requires: [        
        'Cfg.model.ValidationTask'
    ],
    fields: [
        {name: 'task', persist: false},
        {name: 'id', type: 'number'},
        {name: 'startedOn', type: 'number'},
        {name: 'finishedOn', type: 'number'},
        {name: 'duration', type: 'number'},
        {name: 'status', type: 'string'},
        {name: 'reason', type: 'string'},
        {name: 'statusDate', type: 'number'},
        {name: 'statusPrefix', type: 'string'},    
        {
            name: 'deviceGroup',
            persist:false,
            mapping:  function (data) {
                return data.task.deviceGroup;
            }
        },
        {
            name: 'name',
            persist:false,
            mapping:  function (data) {
                return data.task.name;
            }
        },
        {
            name: 'statusOnDate',
            persist: false,
            mapping: function (data) {
                if (data.statusDate && (data.statusDate !== 0)) {
                    return data.statusPrefix + ' ' + moment(data.statusDate).format('ddd, DD MMM YYYY HH:mm:ss');
                }
                return data.statusPrefix;
            }
        },
        {
            name: 'startedOn_formatted',
            persist: false,
            mapping: function (data) {
                if (data.startedOn && (data.startedOn !== 0)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(data.startedOn));
                }
                return '-';
            }
        },
        {
            name: 'finishedOn_formatted',
            persist: false,
            mapping: function (data) {
                if (data.finishedOn && (data.finishedOn !== 0)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(data.finishedOn));
                }
                return '-';
            }
        }
    ]
});