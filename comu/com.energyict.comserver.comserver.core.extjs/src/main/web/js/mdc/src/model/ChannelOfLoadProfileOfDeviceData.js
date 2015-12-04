Ext.define('Mdc.model.ChannelOfLoadProfileOfDeviceData', {
    extend: 'Ext.data.Model',
    idProperty: 'interval_end',
    fields: [
        {name: 'interval', type: 'auto'},
        {name: 'readingTime', dateFormat: 'time', type: 'date'},
        {name: 'value', type: 'float', useNull: true },
        {name: 'isBulk', type: 'boolean'},
        {name: 'collectedValue', type: 'float', useNull: true},
        {name: 'intervalFlags', type: 'auto'},
        {name: 'validationStatus', type: 'auto'},
        {name: 'mainValidationInfo', type: 'auto'},
        {name: 'bulkValidationInfo', type: 'auto'},
        {name: 'confirmed', type: 'auto'},
        {name: 'dataValidated', type: 'auto'},
        {name: 'multiplier', type: 'int'},
        'plotband',
        'readingQualities',
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
                        date: data.reportedDateTime
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
                        date: data.reportedDateTime
                    }
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
                    result.delta.suspect = delta ? delta.validationResult.split('.')[1] == 'suspect' : false;
                    delta.validationResult == 'validationStatus.notValidated' ? result.delta.notValidated = true : result.delta.notValidated = false

                    if (delta.action == 'FAIL') {
                        result.delta.suspect = true;
                        result.delta['informative'] = false;
                    }
                    if (delta.action == 'WARN_ONLY') {
                        result.delta.suspect ? result.delta['informative'] = false : result.delta['informative'] = true;
                    }
                }

                if (bulk) {
                    bulk.validationResult == 'validationStatus.notValidated' ? result.bulk.notValidated = true : result.bulk.notValidated = false

                    if (bulk.action == 'FAIL') {
                        result.bulk.suspect = true;
                        result.bulk.informative = false;
                    }
                    if (bulk.action == 'WARN_ONLY') {
                        result.bulk.suspect ? result.bulk.informative = false : result.bulk.informative = true;
                    }
                }

                return result;
            }
        }
    ],

    refresh: function(device, channel, callback) {
        var me = this;

        Ext.Ajax.request({
            url: '/api/ddr/devices/{device}/channels/{channel}/data/{reading}/validation'
                .replace('{device}', device)
                .replace('{channel}', channel)
                .replace('{reading}', me.get('interval').end),
            success: function(response) {
                var data = Ext.decode(response.responseText);
                //Ext.apply(me.raw, data)
                me.beginEdit();
                me.set(data);
                me.endEdit(true);

                callback ? callback() : null;
            }
        })
    }
});