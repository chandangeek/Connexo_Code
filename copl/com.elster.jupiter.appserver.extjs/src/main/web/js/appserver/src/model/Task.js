Ext.define('Apr.model.Task', {
    extend: 'Ext.data.Model',
    fields: [
        'name', 'application','queue','queueStatus','queueStatusDate','nextRun',
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
        }
    ]
});
