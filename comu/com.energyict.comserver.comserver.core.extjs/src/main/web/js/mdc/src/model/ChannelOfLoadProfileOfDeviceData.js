Ext.define('Mdc.model.ChannelOfLoadProfileOfDeviceData', {
    extend: 'Ext.data.Model',
    idgen: 'sequential',
    fields: [
        {name: 'interval', type: 'auto'},
        {name: 'readingTime', dateFormat: 'time', type: 'date'},
        {name: 'value', type: 'auto' },
        {name: 'isBulk', type: 'boolean'},
        {name: 'collectedValue', type: 'auto'},
        {name: 'intervalFlags', type: 'auto'},
        {name: 'validationStatus', type: 'auto'},
        {name: 'validationInfo', type: 'auto'},
        {name: 'confirmed', type: 'auto'},
        'plotband',
        {
            name: 'interval_end',
            persist: false,
            mapping: 'interval.end',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'interval_formatted',
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
            name: 'readingTime_formatted',
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
            name: 'mainModificationState',
            persist: false,
            mapping: function (data) {
                var result = null,
                    mainValidationInfo = data.validationInfo.mainValidationInfo;

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
            name: 'bulkModificationState',
            persist: false,
            mapping: function (data) {
                var result = null,
                    bulkValidationInfo = data.validationInfo.bulkValidationInfo;

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
                    delta = data.validationInfo.mainValidationInfo,
                    bulk = data.validationInfo.bulkValidationInfo;

                if (delta) {
                    result.delta.suspect = delta.validationResult.split('.')[1] == 'suspect';
                    delta.validationResult == 'validationStatus.notValidated' ? result.delta.notValidated = true : result.delta.notValidated = false
                }

                if (delta && delta.validationRules) {
                    _.each(delta.validationRules, function (item) {
                        if (item.action == 'FAIL') {
                            result.delta.suspect = true;
                            result.delta['informative'] = false;
                        }
                        if (item.action == 'WARN_ONLY') {
                            result.delta.suspect ? result.delta['informative'] = false : result.delta['informative'] = true;
                        }
                    });
                }

                if (bulk) {
                    bulk.validationResult == 'validationStatus.notValidated' ? result.bulk.notValidated = true : result.bulk.notValidated = false
                }

                if (bulk && bulk.validationRules) {
                    _.each(bulk.validationRules, function (item) {
                        if (item.action == 'FAIL') {
                            result.bulk.suspect = true;
                            result.bulk.informative = false;
                        }
                        if (item.action == 'WARN_ONLY') {
                            result.bulk.suspect ? result.bulk.informative = false : result.bulk.informative = true;
                        }
                    });
                }

                return result;
            }
        }
    ]
});