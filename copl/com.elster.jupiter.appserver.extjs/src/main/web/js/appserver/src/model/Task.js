Ext.define('Apr.model.Task', {
    extend: 'Ext.data.Model',
    fields: [
        'name', 'application','queue','queueStatus','queueStatusDate','nextRun','trigger','lastRunStatus','lastRunDate',
        {
            name: 'queueStatusString',
            convert: function (value, record) {
                if(record.get('queueStatus')==='Busy'){
                    return Uni.I18n.translate('general.busySince','APR','Busy since {0}',Uni.DateTime.formatDateTimeShort(new Date(record.get('queueStatusDate'))),false);
                } else if (record.get('queueStatus')==='Planned'){
                    return Uni.I18n.translate('general.plannedOn','APR','Planned on {0}',Uni.DateTime.formatDateTimeShort(new Date(record.get('queueStatusDate'))),false);
                }
            }
        },
        {
            name: 'nextRun',
            convert: function(value){
                return Uni.DateTime.formatDateTimeLong(new Date(value));
            }
        },
        {
            name: 'lastRunStatusString',
            convert: function(value,record){
                if(record.get('lastRunStatus')==='Ongoing'){
                    return Uni.I18n.translate('general.busySince','APR','Busy since {0}',Uni.DateTime.formatDateTimeShort(new Date(record.get('lastRunDate'))),false);
                } else if (record.get('lastRunStatus')==='Successful'){
                    return Uni.I18n.translate('general.successOn','APR','Success on {0}',Uni.DateTime.formatDateTimeShort(new Date(record.get('lastRunDate'))),false);
                } else if (record.get('lastRunStatus')==='Failed'){
                    return Uni.I18n.translate('general.failedOn','APR','Failed on {0}',Uni.DateTime.formatDateTimeShort(new Date(record.get('lastRunDate'))),false);
                } else if (record.get('lastRunStatus')==='Created'){
                    return Uni.I18n.translate('general.notExecutedYet','APR','Not executed yet');
                }
            }
        },
        {
            name: 'lastRunDuration',
            persist: false,
            mapping: function (data) {
                if (data.lastRunDuration) {
                    return data.lastRunDuration;
                } else {
                    return '-';
                }
            }
        },
        {
            name: 'currentRunDuration',
            persist: false,
            mapping: function (data) {
                if (data.currentRunDuration) {
                    return data.currentRunDuration;
                } else {
                    return '-';
                }
            }
        }

    ]
});
