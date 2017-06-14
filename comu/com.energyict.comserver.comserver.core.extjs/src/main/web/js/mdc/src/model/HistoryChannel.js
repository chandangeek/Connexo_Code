/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.HistoryChannel', {
    extend: 'Ext.data.Model',
    // idProperty: 'interval_end',
    fields: [
        {name: 'interval', type: 'auto'},
        {name: 'readingTime', dateFormat: 'time', type: 'date'},
        {name: 'value', type: 'float', useNull: true},
        {name: 'isBulk', type: 'boolean'},
        {name: 'collectedValue', type: 'float', useNull: true},
        {name: 'intervalFlags', type: 'auto'},
        {name: 'validationStatus', type: 'auto'},
        {name: 'validationActive', type: 'auto'},
        {name: 'mainValidationInfo', type: 'auto'},
        {name: 'bulkValidationInfo', type: 'auto'},
        {name: 'confirmed', type: 'auto'},
        {name: 'dataValidated', type: 'auto'},
        {name: 'multiplier', type: 'auto'},
        {name: 'reportedDateTime', dateFormat: 'time', type: 'date'},
        {name: 'slaveChannel', type: 'auto', defaultValue: null},
        {name: 'isActive', type: 'boolean'},
        'version',
        'userName',
        'journalTime',
        //  {name: 'journalTime', dateFormat: 'time', type: 'date'},
        'plotband',
        {name: 'readingQualities', type: 'auto', defaultValue: null},
        {
            name: 'interval_end',
            persist: false,
            mapping: 'interval.end',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'mainModificationState',
            persist: false,
            mapping: function (data) {
                var result = null,
                    mainValidationInfo = data.mainValidationInfo;

                if (mainValidationInfo && mainValidationInfo.valueModificationFlag && data.reportedDateTime) {
                    result = {
                        flag: mainValidationInfo.valueModificationFlag,
                        date: data.reportedDateTime,
                        app: mainValidationInfo.editedInApp
                    }
                }
                return result;
            }
        },
        {
            name: 'validationResult',
            persist: false,
            mapping: function (data) {
                return {
                    main: data.mainValidationInfo ? data.mainValidationInfo.validationResult.split('.')[1] : '',
                    bulk: data.bulkValidationInfo ? data.bulkValidationInfo.validationResult.split('.')[1] : ''
                }
            }
        },
        {
            name: 'bulkModificationState',
            persist: false,
            mapping: function (data) {
                var result = null,
                    bulkValidationInfo = data.bulkValidationInfo;

                if (bulkValidationInfo && bulkValidationInfo.valueModificationFlag && data.reportedDateTime) {
                    result = {
                        flag: bulkValidationInfo.valueModificationFlag,
                        date: data.reportedDateTime,
                        app: bulkValidationInfo.editedInApp
                    }
                }
                return result;
            }
        },
        {
            name: 'mainCommentValue',
            type: 'string',
            mapping: function (data) {
                var result = null,
                    mainValidationInfo = data.mainValidationInfo;

                if (mainValidationInfo && mainValidationInfo.commentValue) {
                    result = mainValidationInfo.commentValue;
                }
                return result;
            }
        },
        {
            name: 'bulkCommentValue',
            type: 'string',
            mapping: function (data) {
                var result = null,
                    bulkValidationInfo = data.bulkValidationInfo;

                if (bulkValidationInfo && bulkValidationInfo.commentValue) {
                    result = bulkValidationInfo.commentValue;
                }
                return result;
            }
        },
        {
            name: 'readingProperties',
            persist: false,
            mapping: function (data) {
                var result = {
                        delta: {},
                        bulk: {}
                    },
                    delta = data.mainValidationInfo,
                    bulk = data.bulkValidationInfo;

                if (delta) {
                    result.delta.suspect = delta ? delta.validationResult.split('.')[1] === 'suspect' : false;
                    result.delta.notValidated = delta.validationResult === 'validationStatus.notValidated' ? true : false;

                    if (delta.action === 'FAIL') {
                        result.delta.suspect = true;
                        result.delta['informative'] = false;
                    }
                    if (delta.action === 'WARN_ONLY') {
                        result.delta['informative'] = result.delta.suspect ? false : true;
                    }
                }
                if (bulk) {
                    result.bulk.notValidated = bulk.validationResult === 'validationStatus.notValidated' ? true : false;

                    if (bulk.action === 'FAIL') {
                        result.bulk.suspect = true;
                        result.bulk.informative = false;
                    }
                    if (bulk.action === 'WARN_ONLY') {
                        result.bulk.informative = result.bulk.suspect ? false : true;
                    }
                }
                return result;
            }
        }
    ],

    getDetailedInformation: function (deviceId, channelId, callback) {
        var me = this;
        Ext.Ajax.request({
            url: me.get('isActive') ? Ext.String.format('/api/ddr/devices/{0}/channels/{1}/data/{2}/validation', deviceId, channelId, me.get('interval').end) :
                Ext.String.format('/api/ddr/devices/{0}/channels/{1}/data/{2}/{3}/validation', deviceId, channelId, me.get('interval').end, me.get('journalTime')),
            success: function (response) {
                var detailRecord = me.get('isActive') ? Ext.create(Mdc.model.HistoryChannel) : Ext.create(Mdc.model.ChannelOfLoadProfileOfDeviceData),
                    data = Ext.decode(response.responseText);

                detailRecord.set(data);
                if (Ext.isFunction(callback)) {
                    callback(detailRecord);
                }
            }
        })
    }
});