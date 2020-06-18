/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.model.FirmwareCampaign', {
    extend: 'Uni.model.Version',
    requires: [
        'Uni.property.model.Property',
        'Fwc.model.DeviceType',
        'Fwc.model.FirmwareType',
        'Fwc.model.DeviceGroup',
        'Fwc.firmwarecampaigns.model.FirmvareVersionsOption'
    ],
    fields: [
        'id',
        'name',
        {name: 'status', defaultValue: null},
        {name: 'manuallyCancelled', defaultValue: null},
        {name: 'validationTimeout', defaultValue: null},
        {name: 'firmwareVersion', defaultValue: null},
        {name: 'devices', defaultValue: null},
        {name: 'managementOption', defaultValue: null, convert: function (value, record) {
            return record.convertObjectField(value);
        }},
        {name: 'deviceType', defaultValue: null, convert: function (value, record) {
            return record.convertObjectField(value);
        }},
        {name: 'firmwareType', defaultValue: null, convert: function (value, record) {
            return record.convertObjectField(value);
        }},
        {name: 'deviceGroup', defaultValue: null },
        {name: 'startedOn', type: 'date', dateFormat: 'time', persist: false},
        {name: 'finishedOn', type: 'date', dateFormat: 'time', persist: false},
        {name: 'timeBoundaryStart',type: 'int',useNull: true},
        {name: 'timeBoundaryEnd',type: 'int',useNull: true},
        {name: 'timeBoundaryStartTimeInSec',type: 'auto',useNull: true, persist: false, defaultValue: 64800, mapping: function (data){// 18:00 by default
             var value = 64800;
             if (data.timeBoundaryStart){
                 var timeBoundaryStartDate = new Date(data.timeBoundaryStart);
                 value = ( timeBoundaryStartDate.getHours() * 60 + timeBoundaryStartDate.getMinutes() ) * 60;
             }
             return value;
        }},
        {name: 'timeBoundaryEndTimeInSec',type: 'auto',useNull: true, persist: false, defaultValue: 82800, mapping: function (data){// 23:00 by default
             var value = 82800;
             if (data.timeBoundaryEnd){
                 var timeBoundaryEndDate = new Date(data.timeBoundaryEnd);
                 value = ( timeBoundaryEndDate.getHours() * 60 + timeBoundaryEndDate.getMinutes() ) * 60;
             }
             return value;
        }},
        {
            name: 'timeBoundaryAsText',
            persist: false,
            mapping: function (data) {
                if ( !Ext.isEmpty(data.timeBoundaryStart) || !Ext.isEmpty(data.timeBoundaryEnd)) {
                        var startHours =  new Date(data.timeBoundaryStart).getHours(),
                        startMinutes = new Date(data.timeBoundaryStart).getMinutes(),
                        endHours = new Date(data.timeBoundaryEnd).getHours(),
                        endMinutes = new Date(data.timeBoundaryEnd).getMinutes(),
                        addZeroIfOneDigit = function (timeCount) {
                            var timeInString = timeCount.toString();
                            if (timeInString.length === 1) {
                                timeInString = '0' + timeInString;
                            }
                            return timeInString;
                        },
                        doFormat = function(hours, minutes) {
                            return addZeroIfOneDigit(hours) + ':' + addZeroIfOneDigit(minutes);
                        };
                    return [ doFormat(startHours, startMinutes) , doFormat(endHours, endMinutes)]
                }
                return '-';
            }
        },
        {name : 'serviceCall', type: 'auto', persist: false, defaultValue: null}, {
            name: 'validationComTask',
            type: 'auto',
            useNull: true,
            defaultValue: undefined
        },
        {
            name: 'calendarUploadComTask',
            type: 'auto',
            useNull: true,
            defaultValue: undefined
        }, {
            name: 'validationComTask',
            type: 'auto',
            useNull: true,
            defaultValue: undefined
        }, {
            name: 'calendarUploadConnectionStrategy',
            type: 'auto',
            useNull: true,
            defaultValue: undefined
        }, {
            name: 'validationConnectionStrategy',
            type: 'auto',
            useNull: true,
            defaultValue: undefined
        },
        {name : 'checkOptions', type: 'auto'},
        {name : 'withUniqueFirmwareVersion', type: 'auto'}
    ],
    associations: [
        {
            type: 'hasMany',
            name: 'properties',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties'
        },
        {
            type: 'hasOne',
            name: 'checkOptions',
            model: 'Fwc.firmwarecampaigns.model.FirmvareVersionsOption',
            associationKey: 'checkOptions',
            foreignKey: 'checkOptions',
            getterName: 'getFirmvareVersionsOptions',
            setterName: 'setFirmvareVersionsOptions',
            reader: {
                type: 'json'
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/fwc/campaigns',
        reader: {
            type: 'json'
        }
    },
    convertObjectField: function (value) {
        if (Ext.isObject(value) || value === null) {
            return value
        } else {
            return {
                id: value
            }
        }
    }
});
