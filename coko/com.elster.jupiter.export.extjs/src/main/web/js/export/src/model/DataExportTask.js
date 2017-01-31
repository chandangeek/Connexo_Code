/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.model.DataExportTask', {
    extend: 'Uni.model.Version',
    requires: [
        'Uni.property.model.Property',
        'Dxp.model.DataSelector',
        'Dxp.model.StandardDataSelector',
        'Dxp.model.Destination'
    ],
    fields: [
        'id', 'name', 'dataProcessor', 'dataSelector', 'destinations', 'exportComplete', 'validatedDataOption',
        {
            name: 'standardDataSelector',
            defaultValue: null
        },
        {
            name: 'schedule',
            defaultValue: null
        },
        {name: 'recurrence', type: 'auto'},
        {
            name: 'nextRun',
            defaultValue: null
        },
        {
            name: 'lastRun',
            defaultValue: null
        },
        {
            name: 'lastExportOccurrence',
            persist: false
        },
        {
            name: 'deviceGroup',
            persist: false,
            mapping: function (data) {
                if (data.standardDataSelector && data.standardDataSelector.deviceGroup) {
                    return data.standardDataSelector.deviceGroup.name;
                } else {
                    return null;
                }
            }
        },
        {
            name: 'usagePointGroup',
            persist: false,
            mapping: function (data) {
                if (data.standardDataSelector && data.standardDataSelector.usagePointGroup) {
                    return data.standardDataSelector.usagePointGroup.name;
                } else {
                    return null;
                }
            }
        },
        {
            name: 'exportPeriod',
            persist: false,
            mapping: function (data) {
                if ((data.standardDataSelector) && (data.standardDataSelector.exportPeriod)) {
                    return data.standardDataSelector.exportPeriod.name;
                } else {
                    return null;
                }
            }
        },
        {
            name: 'exportComplete',
            persist: false,
            mapping: function (data) {
                if ((data.standardDataSelector)) {
                    return data.standardDataSelector.exportComplete?'true':'false';
                }
            }
        },
        {
            name: 'validatedDataOption',
            persist: false,
            mapping: function (data) {
                if ((data.standardDataSelector)) {
                    return data.standardDataSelector.validatedDataOption;
                } else {
                    return null;
                }
            }
        },
        {
            name: 'exportUpdate',
            persist: false,
            mapping: function (data) {
                if ((data.standardDataSelector)) {
                    return data.standardDataSelector.exportUpdate?'true':'false';
                }
            }
        },
        {
            name: 'updatePeriod',
            persist: false,
            mapping: function (data) {
                if ((data.standardDataSelector)) {
                    return data.standardDataSelector.updatePeriod;
                } else {
                    return null;
                }
            }
        },
        {
            name: 'updateWindow',
            persist: false,
            mapping: function (data) {
                if ((data.standardDataSelector)) {
                    return data.standardDataSelector.updateWindow;
                } else {
                    return null;
                }
            }
        },
        {
            name: 'exportContinuousData',
            persist: false,
            mapping: function (data) {
                if ((data.standardDataSelector)) {
                    return data.standardDataSelector.exportContinuousData?'true':'false';
                }
            }
        },

        {
            name: 'readingTypes',
            persist: false,
            mapping: function (data) {
                if ((data.standardDataSelector) && (data.standardDataSelector.readingTypes)) {
                    return data.standardDataSelector.readingTypes;
                } else {
                    return null;
                }
            }
        },
        {
            name: 'eventTypes',
            persist: false,
            mapping: function (data) {
                if ((data.standardDataSelector) && (data.standardDataSelector.eventTypeCodes)) {
                    return data.standardDataSelector.eventTypeCodes;
                } else {
                    return null;
                }
            }
        },
        {
            name: 'lastRun_formatted',
            persist: false,
            mapping: function (data) {
                var result;
                if (data.lastRun && (data.lastRun !== 0)) {
                    result = Uni.DateTime.formatDateTimeLong(new Date(data.lastRun));
                } else {
                    result = '-'
                }
                return result;
            }
        },
        {
            name: 'nextRun_formatted',
            persist: false,
            mapping: function (data) {
                var result;
                if (data.schedule && data.nextRun && (data.nextRun !== 0)) {
                    result = Uni.DateTime.formatDateTimeLong(new Date(data.nextRun));
                } else {
                    result = Uni.I18n.translate('general.notScheduled', 'DES', 'Not scheduled')
                }
                return result;
            }
        },
        {
            name: 'status',
            persist: false,
            mapping: function (data) {
                if (data.lastExportOccurrence && data.lastExportOccurrence.status) {
                    return data.lastExportOccurrence.status;
                } else {
                    return Uni.I18n.translate('general.created', 'DES', 'Created');
                }
            }
        },
        {
            name: 'statusOnDate',
            persist: false,
            mapping: function (data) {
                if (data.lastExportOccurrence && data.lastExportOccurrence.statusDate && data.lastExportOccurrence.statusDate != 0) {
                    return data.lastExportOccurrence.statusPrefix + ' ' + Uni.DateTime.formatDateTimeLong(new Date(data.lastExportOccurrence.statusDate));
                } else if (data.lastExportOccurrence) {
                    return data.lastExportOccurrence.statusPrefix
                } else {
                    return Uni.I18n.translate('general.created', 'DES', 'Created');
                }
            }
        },
        {
            name: 'reason',
            persist: false,
            mapping: function (data) {
                if (data.lastExportOccurrence && data.lastExportOccurrence.reason) {
                    return data.lastExportOccurrence.reason;
                } else {
                    return '';
                }
            }
        },
        {
            name: 'trigger',
            persist: false,
            mapping: function (data) {
                if (data.lastExportOccurrence && data.lastExportOccurrence.trigger) {
                    return data.lastExportOccurrence.trigger;
                } else {
                    return '-'
                }
            }
        },
        {
            name: 'startedOn',
            persist: false,
            mapping: function (data) {
                if (data.lastExportOccurrence && data.lastExportOccurrence.startedOn) {
                    return Uni.DateTime.formatDateTimeLong(new Date(data.lastExportOccurrence.startedOn));
                } else {
                    return '-';
                }
            }
        },
        {
            name: 'finishedOn',
            persist: false,
            mapping: function (data) {
                if (data.lastExportOccurrence && data.lastExportOccurrence.finishedOn) {
                    return Uni.DateTime.formatDateTimeLong(new Date(data.lastExportOccurrence.finishedOn));
                } else {
                    return '-';
                }
            }
        },
        {
            name: 'duration',
            persist: false,
            mapping: function (data) {
                if (data.lastExportOccurrence && data.lastExportOccurrence.duration) {
                    return data.lastExportOccurrence.duration;
                } else {
                    return '-';
                }
            }
        },
        {
            name: 'exportUpdateForPreview',
            persist: false,
            convert: function(value,record){
                return record.getData().exportUpdate==='true'?
                    Uni.I18n.translate('general.exportWithinWindowX', 'DES', 'Export within the update window {0}',[record.getData().standardDataSelector.updatePeriod?record.getData().standardDataSelector.updatePeriod.name:Uni.I18n.translate('general.notDefined', 'DES', 'Not defined')]):
                    Uni.I18n.translate('general.noExportForUpdated', 'DES', 'Do not export');
            }
        },
        {
            name: 'updatedValuesForPreview',
            persist: false,
            convert: function(value,record){
                return record.getData().updateWindow?Uni.I18n.translate('general.exportValuesAndAdjacentX', 'DES', "updated values and adjacent data within timeframe '{0}'",[record.getData().updateWindow.name]):Uni.I18n.translate('general.updateValuesOnly', 'DES', 'updated values only');
            }
        }
    ],

    associations: [
        {
            name: 'destinations',
            type: 'hasMany',
            model: 'Dxp.model.Destination',
            associationKey: 'destinations',
            foreignKey: 'destinations',
            getTypeDiscriminator: function (node) {
                return 'Dxp.model.Destination';
            }
        },
        {
            name: 'dataSelector',
            type: 'hasOne',
            model:'Dxp.model.DataSelector',
            associationKey: 'dataSelector',
            getterName: 'getDataSelector',
            setterName: 'setDataSelector'
        },
        {
            name: 'dataProcessor',
            type: 'hasOne',
            model:'Dxp.model.DataProcessor',
            associationKey: 'dataProcessor',
            getterName: 'getDataProcessor',
            setterName: 'setDataProcessor'
        },
        {
            name: 'standardDataSelector',
            type: 'hasOne',
            model:'Dxp.model.StandardDataSelector',
            associationKey: 'standardDataSelector',
            getterName: 'getStandardDataSelector',
            setterName: 'setStandardDataSelector'
        }


    ],
    proxy: {
        type: 'rest',
        url: '/api/export/dataexporttask',
        reader: {
            type: 'json'
        }
    }
});