Ext.define('Mdc.model.ChannelOfLoadProfileOfDeviceData', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.ChannelReadingValidationResult',
        'Mdc.model.ChannelReadingValidationInfo'
    ],
    idgen: 'sequential',
    fields: [
        {name: 'interval', type: 'auto'},
        {name: 'readingTime', dateFormat: 'time', type: 'date'},
        {name: 'value', type: 'auto' },
        {name: 'isBulk', type: 'boolean'},
        {name: 'collectedValue', type: 'auto'},
        {name: 'intervalFlags', type: 'auto'},
        {name: 'validationStatus', type: 'auto'},
        {name: 'modificationFlag', type: 'auto'},
        'validationInfo',
        {
            name: 'interval_start',
            persist: false,
            mapping: function (data) {
                return data.interval ? Uni.I18n.formatDate('deviceloadprofiles.data.dateFormat', new Date(data.interval.start), 'MDC', 'M d, Y \\a\\t H:i') : '';
            }
        },
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
                var result = null;

                if (data.modificationFlag && data.reportedDateTime) {
                    result = {
                        flag: data.modificationFlag,
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
                if (delta) {
                    delta.validationResult == 'validationStatus.notValidated' ? result.delta.notValidated = true : result.delta.notValidated = false
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
                if (bulk) {
                    bulk.validationResult == 'validationStatus.notValidated' ? result.bulk.notValidated = true : result.bulk.notValidated = false
                }

                return result;
            }
        },
        {
            name: 'mainValidationInformation',
            persist: false,
            mapping: function (data) {
                var result = {};
                if (data.validationInfo && data.validationInfo.mainValidationInfo) {
                    result = data.validationInfo.mainValidationInfo;
                }
                return result;
            }
        },
        {
            name: 'bulkValidationInformation',
            persist: false,
            mapping: function (data) {
                var result = {};
                if (data.validationInfo && data.validationInfo.bulkValidationInfo) {
                    result = data.validationInfo.bulkValidationInfo;
                }
                return result;
            }
        },
        {
            name: 'plotBand',
            persist: false
        }
    ],
    associations: [
        {
            type: 'hasOne',
            associatedName: 'validationInfo',
            associationKey: 'validationInfo',
            model: 'Mdc.model.ChannelReadingValidationInfo',
            getterName: 'getValidationInfo'
        }
    ]
});