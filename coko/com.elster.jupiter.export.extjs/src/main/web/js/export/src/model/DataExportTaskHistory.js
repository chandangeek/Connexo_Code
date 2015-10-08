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
        {name: 'exportPeriodFrom', type: 'number'},
        {name: 'exportPeriodTo', type: 'number'},
        {name: 'statusDate', type: 'number'},
        {name: 'statusPrefix', type: 'string'},
        {name: 'trigger', type: 'string'},

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
                if ((data.task.standardDataSelector) && (data.task.standardDataSelector.eventTypeCodes)) {
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
                    return 'From ' + Uni.DateTime.formatDateTimeLong(new Date(data.exportPeriodFrom)) +
                        ' to ' + Uni.DateTime.formatDateTimeLong(new Date(data.exportPeriodTo));
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
                        Uni.I18n.translate('general.exportWithinWindowX', 'DES', "Export within the update window '{0}'",[record.data.task.standardDataSelector.updatePeriod?record.data.task.standardDataSelector.updatePeriod.name:Uni.I18n.translate('general.notDefined', 'DES', 'Not defined')]):
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
    ]

});