/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.model.FirmwareCampaign', {
    extend: 'Uni.model.Version',
    requires: [
        'Uni.property.model.Property',
        'Fwc.model.DeviceType',
        'Fwc.model.FirmwareType',
        'Fwc.model.DeviceGroup'
    ],
    fields: [
        'id',
        'name',
        {name: 'status', defaultValue: null},
        {name: 'firmwareVersion', defaultValue: null},
        {name: 'devicesStatus', defaultValue: null},
        {name: 'managementOption', defaultValue: null, convert: function (value, record) {
            return record.convertObjectField(value);
        }},
        {name: 'deviceType', defaultValue: null, convert: function (value, record) {
            return record.convertObjectField(value);
        }},
        {name: 'firmwareType', defaultValue: null, convert: function (value, record) {
            return record.convertObjectField(value);
        }},
        {name: 'deviceGroup', defaultValue: null, convert: function (value, record) {
            return record.convertObjectField(value);
        }},
        {name: 'startedOn', type: 'date', dateFormat: 'time', persist: false},
        {name: 'finishedOn', type: 'date', dateFormat: 'time', persist: false},
        {name: 'timeBoundaryStart',type: 'int',useNull: true, defaultValue: 64800}, // 18:00 by default
        {name: 'timeBoundaryEnd',type: 'int',useNull: true, defaultValue: 82800},   // 23:00 by default
        {
            name: 'timeBoundaryAsText',
            persist: false,
            mapping: function (data) {
                if ( !Ext.isEmpty(data.timeBoundaryStart) || !Ext.isEmpty(data.timeBoundaryEnd)) {
                    var startMinutes = (data.timeBoundaryStart / 3600 | 0),
                        startSeconds = (data.timeBoundaryStart / 60 - startMinutes * 60),
                        endMinutes = (data.timeBoundaryEnd / 3600 | 0),
                        endSeconds = (data.timeBoundaryEnd / 60 - endMinutes * 60),
                        addZeroIfOneDigit = function (timeCount) {
                            var timeInString = timeCount.toString();
                            if (timeInString.length === 1) {
                                timeInString = '0' + timeInString;
                            }
                            return timeInString;
                        },
                        doFormat = function(minutes, seconds) {
                            return addZeroIfOneDigit(minutes) + ':' + addZeroIfOneDigit(seconds);
                        };

                    return Uni.I18n.translate('general.betweenXandY', 'FWC', 'Between {0} and {1}',
                        [ doFormat(startMinutes, startSeconds), doFormat(endMinutes, endSeconds) ]
                    );
                }
                return '-';
            }
        }
    ],
    associations: [
        {
            type: 'hasMany',
            name: 'properties',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties'
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