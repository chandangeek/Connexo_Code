Ext.define('Mdc.model.ChannelOfLoadProfileOfDeviceData', {
    extend: 'Ext.data.Model',
    idProperty: 'interval_end',
    fields: [
        {name: 'interval', type: 'auto'}, // required
        {name: 'readingTime', dateFormat: 'time', type: 'date'}, // preview
        {name: 'value', type: 'auto' }, // required
        {name: 'isBulk', type: 'boolean'}, // preview
        {name: 'collectedValue', type: 'auto'}, // required
        {name: 'intervalFlags', type: 'auto'}, // check - available on grid
        {name: 'validationStatus', type: 'auto'}, // preview
        {name: 'mainValidationInfo', type: 'auto'}, // struct
        {name: 'bulkValidationInfo', type: 'auto'}, // struct
        {name: 'confirmed', type: 'auto'}, // required
        {name: 'dataValidated', type: 'auto'}, // required
        'plotband',
        {
            name: 'interval_end', // required - grid
            persist: false,
            mapping: 'interval.end',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'interval_formatted', // preview
            persist: false,
            mapping: function (data) {
                return data.interval
                    ? Uni.DateTime.formatDateLong(new Date(data.interval.start))
                    + ' ' + Uni.I18n.translate('general.at', 'MDC', 'At').toLowerCase() + ' '
                    + Uni.DateTime.formatTimeLong(new Date(data.interval.start))
                    + ' - '
                    + Uni.DateTime.formatDateLong(new Date(data.interval.end))
                    + ' ' + Uni.I18n.translate('general.at', 'MDC', 'At').toLowerCase() + ' '
                    + Uni.DateTime.formatTimeLong(new Date(data.interval.end))
                    : '';
            }
        },
        {
            name: 'readingTime_formatted', // preview
            persist: false,
            mapping: function (data) {
                return data.readingTime
                    ? Uni.DateTime.formatDateLong(new Date(data.readingTime))
                    + ' ' + Uni.I18n.translate('general.at', 'MDC', 'At').toLowerCase() + ' '
                    + Uni.DateTime.formatTimeLong(new Date(data.readingTime))
                    : '';
            }
        },
        {
            name: 'mainModificationState', // grid
            persist: false,
            mapping: function (data) {
                var result = null,
                    mainValidationInfo = data.mainValidationInfo;

                if (mainValidationInfo.valueModificationFlag && data.reportedDateTime) {
                    result = {
                        flag: mainValidationInfo.valueModificationFlag,
                        date: data.reportedDateTime
                    }
                }

                return result;
            }
        },
        {
            name: 'bulkModificationState', // grid
            persist: false,
            mapping: function (data) {
                var result = null,
                    bulkValidationInfo = data.bulkValidationInfo;

                if (bulkValidationInfo.valueModificationFlag && data.reportedDateTime) {
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
                    result.delta.suspect = delta.validationResult.split('.')[1] == 'suspect';
                    delta.validationResult == 'validationStatus.notValidated' ? result.delta.notValidated = true : result.delta.notValidated = false
                }

                if (delta && delta.action) {
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
                }

                if (bulk && bulk.action) {
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
                me.set(data);

                callback ? callback() : null;
            }
        })
    }
});