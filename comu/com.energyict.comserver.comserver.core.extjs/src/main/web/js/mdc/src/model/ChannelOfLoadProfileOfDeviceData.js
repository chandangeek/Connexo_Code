Ext.define('Mdc.model.ChannelOfLoadProfileOfDeviceData', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.ChannelReadingValidationResult'
    ],
    idgen: 'sequential',
    fields: [
        {name: 'interval', type: 'auto'},
        {name: 'readingTime', dateFormat: 'time', type: 'date'},
        {name: 'value', type: 'auto' },
        {name: 'rawValue', type: 'auto'},
        {name: 'delta', type: 'auto'},
        {name: 'isBulk', type: 'boolean'},
        {name: 'collectedValue', type: 'auto'},
        {name: 'intervalFlags', type: 'auto'},
        {name: 'validationStatus', type: 'auto'},

        /*{
         name: 'suspect_rules',
         persist: false,
         mapping: function (data) {
         if (!Ext.isEmpty(data.suspectReason)) {
         var str = '',
         prop,
         failEqualDataValue,
         intervalFlagsValue = '';
         Ext.Array.each(data.suspectReason, function (rule) {
         if (!Ext.isEmpty(rule.properties)) {
         switch (rule.implementation) {
         case 'com.elster.jupiter.validators.impl.ThresholdValidator':
         prop = ' - ' + rule.properties[0].key.charAt(0).toUpperCase() + rule.properties[0].key.substring(1) + ': ' + rule.properties[0].propertyValueInfo.value + ', ' +
         rule.properties[1].key.charAt(0).toUpperCase() + rule.properties[1].key.substring(1) + ': ' + rule.properties[1].propertyValueInfo.value;
         break;
         case 'com.elster.jupiter.validators.impl.RegisterIncreaseValidator':
         if (rule.properties[0].propertyValueInfo.value) {
         failEqualDataValue = Uni.I18n.translate('general.yes', 'MDC', 'Yes');
         } else {
         failEqualDataValue = Uni.I18n.translate('general.no', 'MDC', 'No');
         }
         prop = ' - ' + Uni.I18n.translate('device.registerData.failEqualData', 'MDC', 'Fail equal data') + ': ' + failEqualDataValue;
         break;
         case 'com.elster.jupiter.validators.impl.IntervalStateValidator':
         Ext.Array.each(rule.properties[0].propertyValueInfo.value, function (idValue) {
         Ext.Array.each(rule.properties[0].propertyTypeInfo.predefinedPropertyValuesInfo.possibleValues, function (item) {
         if (idValue === item.id) {
         intervalFlagsValue += item.name + ', ';
         }
         });
         });
         intervalFlagsValue = intervalFlagsValue.slice(0, -2);
         prop = ' - ' + Uni.I18n.translate('deviceloadprofiles.intervalFlags', 'MDC', 'Interval flags') + ': ' + intervalFlagsValue;
         break;
         default:
         prop = '';
         break;
         }
         } else {
         prop = '';
         }
         if (rule.name === 'removed rule') {
         str += Uni.I18n.translate('device.registerData.removedRule', 'MDC', 'removed rule') + '<br>';
         } else {
         str += '<span style="word-wrap: break-word; display: inline-block; width: 800px">' + '<a href="#/administration/validation/rulesets/' + rule.ruleSet.id + '/rules/' + rule.id + '">' + rule.name + '</a>' + prop + '</span>' + '<br>';
         }
         });
         return str;
         } else {
         return '';
         }
         }
         },  */

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
            name: 'modificationState',
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
                    delta = data.deltaValidationInfo,
                    bulk = data.bulkValidationInfo;
                if (delta && delta.validationRules) {
                    _.each(delta.validationRules, function (item) {
                        if (item.action == 'FAIL') {
                            result.delta.suspect = true;
                            result.delta['informative'] = false;
                        }
                        if (item.action == 'WARN_ONLY') {
                            result.delta.suspect ?
                                result.delta['informative'] = false :
                                result.delta['informative'] = true;
                        }
                        console.log(result);
                    });
                }
                if (delta) {
                    delta.validationResult == 'validationStatus.notValidated' ?
                        result.delta.notValidated = true :
                        result.delta.notValidated = false
                }

                if (bulk && bulk.validationRules) {
                    _.each(bulk.validationRules, function (item) {
                        if (item.action == 'FAIL') {
                            result.bulk.suspect = true;
                            result.bulk.informative = false;
                        }
                        if (item.action == 'WARN_ONLY') {
                            result.bulk.suspect ?
                                result.bulk.informative = false :
                                result.bulk.informative = true;
                        }
                    });
                }
                if (bulk) {
                    bulk.validationResult == 'validationStatus.notValidated' ?
                        result.bulk.notValidated = true :
                        result.bulk.notValidated = false
                }

                return result;
            }
        },
        {
            name: 'deltaValidationInformation',
            persist: false,
            mapping: function (data) {
                var result = {};
                if (data.bulkValidationInfo) {
                    result = data.bulkValidationInfo
                }
                return result;
            }
        },
        {
            name: 'bulkValidationInformation',
            persist: false,
            mapping: function (data) {
                var result = {};
                if (data.bulkValidationInfo) {
                    result = data.bulkValidationInfo
                }
                return result;
            }
        },
        {
            name: 'deltaModificationState',
            persist: false,
            mapping: function (data) {
                var result = null;
                if (data.bulkValidationInfo) {
                    result = data.bulkValidationInfo.modificationState
                }
                return result;
            }
        },
        {
            name: 'bulkModificationState',
            persist: false,
            mapping: function (data) {
                var result = null;
                if (data.bulkValidationInfo) {
                    result = data.bulkValidationInfo.modificationState
                }
                return result;
            }
        }
    ],
    associations: [
        {
            type: 'hasOne',
            associatedName: 'deltaValidationInfo',
            associationKey: 'deltaValidationInfo',
            model: 'Mdc.model.ChannelReadingValidationResult',
            getterName: 'getDeltaValidationInfo'
        },
        {
            type: 'hasOne',
            associatedName: 'bulkValidationInfo',
            associationKey: 'bulkValidationInfo',
            model: 'Mdc.model.ChannelReadingValidationResult',
            getterName: 'getBulkValidationInfo'
        }
    ]
});