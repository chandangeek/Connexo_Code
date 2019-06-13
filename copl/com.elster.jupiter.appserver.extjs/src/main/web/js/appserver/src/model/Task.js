/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.model.Task', {
    extend: 'Ext.data.Model',
    fields: [
        'id', 'name', 'application', 'queue', 'queueStatus', 'queueStatusDate', 'nextRun', 'trigger', 'lastRunStatus', 'lastRunDate', 'isExtraQueueCreationEnabled',
         'suspendUntil123',
        {
            name: 'queueStatusString',
            convert: function (value, record) {
                if(record.get('queueStatus')==='Busy'){
                    return Uni.I18n.translate('general.busySince','APR','Busy since {0}',Uni.DateTime.formatDateTimeShort(new Date(record.get('queueStatusDate'))),false);
                } else if (record.get('queueStatus')==='Planned'){
                    return Uni.I18n.translate('general.plannedOn','APR','Planned on {0}',Uni.DateTime.formatDateTimeShort(new Date(record.get('queueStatusDate'))),false);
                } else {
                    return Uni.I18n.translate('general.notScheduled', 'APR', 'Not scheduled');
                }
            }
        },
        {
            name: 'nextRun',
            type: 'number',
            defaultValue: null,
           convert: function(value){
               return value != null ? Uni.DateTime.formatDateTimeLong(new Date(value)) : Uni.I18n.translate('general.notScheduled', 'APR', 'Not scheduled');
           }
        },
        {
            name: 'suspendUntil123',
            type: 'string',
            //defaultValue: Uni.I18n.translate('general.suspended.yes','APR','Yes')//,   // Lau
            convert: function(value, record){  // test : daca exista valoare in suspendedDataTime atunci e Da, daca nu exista valoare, nu e suspendat (NU)
                return record.get('suspendUntil123') ? Uni.I18n.translate('general.suspended.yes','APR','YesYES') : Uni.I18n.translate('general.suspended.no','APR','NoNO');
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

    ],
    proxy: {
        type: 'rest',
        url: '/api/tsk/task',
        reader: {
            type: 'json'
        }
    }
});
