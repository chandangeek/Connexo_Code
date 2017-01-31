/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.model.DataExportTaskHistory', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property',
        'Dxp.model.DataExportTask'
    ],
    fields: [
        {name: 'task', persist: false},
        {name: 'id', type: 'number'},
        {name: 'startedOn', type: 'number'},
        {name: 'finishedOn', type: 'number'},
        {name: 'duration', type: 'number'},
        {name: 'status', type: 'string'},
        {name: 'reason', type: 'string'},
        {name: 'summary', type: 'string'},
        {name: 'exportPeriodFrom', type: 'number'},
        {name: 'exportPeriodTo', type: 'number'},
        {name: 'statusDate', type: 'number'},
        {name: 'statusPrefix', type: 'string'},
        {name: 'trigger', type: 'string'},
        {name: 'updatePeriodFrom', type: 'string'},
        {name: 'updatePeriodTo', type: 'string'},

        {
            name: 'dataProcessor',
            persist:false,
            mapping:  function (data) {
                return data.task.dataProcessor;
            }
        },

        {
            name: 'exportperiod',
            persist:false,
            mapping:  function (data) {
                if(data.task.standardDataSelector){
                    return data.task.standardDataSelector.exportPeriod;
                } else {
                    return '-';
                }
            }
        },

        {
            name: 'readingTypes',
            persist:false,
            mapping:  function (data) {
                if(data.task.standardDataSelector){
                    return data.task.standardDataSelector.readingTypes;
                } else {
                    return '-';
                }
            }
        },
        {
            name: 'eventTypes',
            persist: false,
            mapping: function (data) {
                if (data.task.standardDataSelector && data.task.standardDataSelector.eventTypeCodes) {
                    return data.task.standardDataSelector.eventTypeCodes;
                } else {
                    return null;
                }
            }
        },
        {
            name: 'deviceGroup',
            persist:false,
            mapping:  function (data) {
                if(data.task.standardDataSelector){
                    return data.task.standardDataSelector.deviceGroup;
                } else {
                    return '-';
                }
            }
        },
        {
            name: 'usagePointGroup',
            persist: false,
            mapping: function (data) {
                if (data.task.standardDataSelector && data.task.standardDataSelector.usagePointGroup) {
                    return data.task.standardDataSelector.usagePointGroup.name;
                } else {
                    return '-';
                }
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
            name: 'taskId',
            persist:false,
            mapping:  function (data) {
                return data.task.id;
            }
        },
        {
            name: 'statusOnDate',
            persist: false,
            mapping: function (data) {
                if (data.statusDate && (data.statusDate !== 0)) {
                    return data.statusPrefix + ' ' + Uni.DateTime.formatDateTimeLong(new Date(data.statusDate));
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
        },
        {
            name: 'exportPeriodFrom_formatted',
            persist: false,
            mapping: function (data) {
                if (data.exportPeriodFrom && (data.exportPeriodFrom !== 0)) {
                    return moment(data.exportPeriodFrom).format('ddd, DD MMM YYYY HH:mm:ss');
                }
                return '-';
            }
        },
        {
            name: 'exportPeriodTo_formatted',
            persist: false,
            mapping: function (data) {
                if (data.exportPeriodTo && (data.exportPeriodTo !== 0)) {
                    return moment(data.exportPeriodTo).format('ddd, DD MMM YYYY HH:mm:ss');
                }
                return '-';
            }
        },
        {
            name: 'exportPeriod_range',
            persist: false,
            mapping: function (data) {
                if ((data.exportPeriodFrom && data.exportPeriodFrom !== 0) &&
                    (data.exportPeriodTo && data.exportPeriodTo !== 0)) {
                    return Uni.I18n.translate('general.fromTo', 'DES', 'From {0} to {1}', [Uni.DateTime.formatDateTimeLong(new Date(data.exportPeriodFrom)),Uni.DateTime.formatDateTimeLong(new Date(data.exportPeriodTo))], false);
                }
                return '-';
            }
        },
        {
            name: 'updatePeriodFrom_formatted',
            persist: false,
            mapping: function (data) {
                if (data.updatePeriodFrom && (data.updatePeriodFrom !== 0)) {
                    return moment(data.updatePeriodFrom).format('ddd, DD MMM YYYY HH:mm:ss');
                }
                return '-';
            }
        },
        {
            name: 'updatePeriodTo_formatted',
            persist: false,
            mapping: function (data) {
                if (data.updatePeriodTo && (data.updatePeriodTo !== 0)) {
                    return moment(data.updatePeriodTo).format('ddd, DD MMM YYYY HH:mm:ss');
                }
                return '-';
            }
        },
        {
            name: 'updatePeriod_range',
            persist: false,
            mapping: function (data) {
                if ((data.updatePeriodFrom && data.updatePeriodFrom !== 0) &&
                    (data.updatePeriodTo && data.updatePeriodTo !== 0)) {
                    return Uni.I18n.translate('general.toPeriod', 'DES', '{0} to {1}', [Uni.DateTime.formatDateTimeLong(new Date(data.updatePeriodFrom)),Uni.DateTime.formatDateTimeLong(new Date(data.updatePeriodTo))], false);
                }
                return '-';
            }
        },
        {
            name: 'dataSelector',
            persist: false,
            mapping: function(data) {
                return data.task.dataSelector;
            }
        },
        {
            name: 'exportPeriod',
            persist: false,
            mapping: function(data){
                if(data.task.standardDataSelector){
                    return data.task.standardDataSelector?data.task.standardDataSelector.exportPeriod.name:'';
                } else {
                     return '';
                }
            }
        },
        {
            name: 'exportUpdateForPreview',
            persist: false,
            convert: function(value,record){
                if(record.data.task.standardDataSelector){
                    return record.data.task.standardDataSelector.exportUpdate ?
                        Uni.I18n.translate('general.exportWithinWindowX', 'DES', 'Export within the update window {0}', [record.data.task.standardDataSelector.updatePeriod ? record.data.updatePeriod_range : Uni.I18n.translate('general.notDefined', 'DES', 'not defined')]):
                        Uni.I18n.translate('general.noExportForUpdated', 'DES', 'Do not export');
                } else {
                    return '';
                }
            }
        },


        {
            name: 'updatedValuesForPreview',
            persist: false,
            convert: function(value,record){
                if(record.data.task.standardDataSelector){
                    return record.data.task.standardDataSelector.updateWindow?Uni.I18n.translate('general.exportValuesAndAdjacentX', 'DES', "updated values and adjacent data within timeframe '{0}'",[record.data.task.standardDataSelector.updateWindow.name]):Uni.I18n.translate('general.updateValuesOnly', 'DES', 'updated values only');
                }
            }
        },
        {
            name: 'exportComplete',
            persist: false,
            mapping: function (data) {
                if ((data.task.standardDataSelector)) {
                    return data.task.standardDataSelector.exportComplete?'true':'false';
                }
            }
        },
        {
            name: 'validatedDataOption',
            persist: false,
            mapping: function (data) {
                if ((data.task.standardDataSelector)) {
                    return data.task.standardDataSelector.validatedDataOption;
                } else {
                    return null;
                }
            }
        },
        {
            name: 'destinations',
            persist: false,
            mapping: function (data) {
                return data.task.destinations;
            }
        }

    ],

    associations: [
        {
            type: 'hasOne',
            model: 'Dxp.model.DataExportTask',
            associationKey: 'task',
            name: 'task',
            getterName: 'getTask'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/export/dataexporttask/history',
        reader: {
            type: 'json'
        }
    }

});