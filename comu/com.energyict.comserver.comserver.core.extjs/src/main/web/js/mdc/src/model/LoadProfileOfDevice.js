/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.LoadProfileOfDevice', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Mdc.store.TimeUnits'
    ],
    fields: [
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'},
        {name: 'obisCode', type: 'string'},
        {name: 'interval', type: 'auto'},
        {name: 'lastReading', type: 'long', useNull: true},
        {name: 'channels', type: 'auto'},
        //{name: 'lastChecked', type: 'long', useNull: true},
        {name: 'validationInfo', type: 'auto'},
        {
            name: 'loadProfile',
            persist: false,
            mapping: function (data) {
                return {
                    name: data.name,
                    id: data.id
                }
            }
        },
        {
            name: 'validationInfo_validationActive',
            persist: false,
            mapping: function (data) {
                return (data.validationInfo && data.validationInfo.validationActive) ? Uni.I18n.translate('general.active', 'MDC', 'Active') : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
            }
        },
        {
            name: 'validationInfo_channelValidationStatus',
            persist: false,
            mapping: function (data) {
                return (data.validationInfo && data.validationInfo.channelValidationStatus) ? Uni.I18n.translate('general.active', 'MDC', 'Active') : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
            }
        },
        {
            name: 'validationInfo_dataValidated',
            persist: false,
            mapping: function (data) {
                return (data.validationInfo && data.validationInfo.dataValidated)
                    ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                    : Uni.I18n.translate('general.no', 'MDC', 'No') + '<span class="icon-flag6" style="margin-left:10px; position:absolute;"></span>';
            }
        },
        {
            name: 'interval_formatted',
            persist: false,
            mapping: function (data) {
                var value = data.interval,
                    timeUnitsStore = Ext.getStore('TimeUnits'),
                    result = '',
                    timeUnit,
                    index;
                if (value) {
                    value.count && (result += value.count);
                    if (value.timeUnit) {
                        index = timeUnitsStore.find('timeUnit', value.timeUnit);
                        (index !== -1) && (timeUnit = timeUnitsStore.getAt(index).get('localizedValue'));
                        timeUnit && (result += ' ' + timeUnit);
                    }
                }
                return result;
            }
        },
        {
            name: 'lastReading_formatted',
            persist: false,
            mapping: function (data) {
                return data.lastReading ? Uni.DateTime.formatDateTimeShort(new Date(data.lastReading)) : '';
            }
        },
        {
            name: 'lastChecked_formatted',
            persist: false,
            mapping: function (data) {
                return (data.validationInfo && data.validationInfo.lastChecked)
                    ? Uni.DateTime.formatDateTimeLong(new Date(data.validationInfo.lastChecked))
                    : '';
            }
        },
        {
            name: 'dataUntil',
            persist: false,
            mapping: function (data) {
                var mostRecentLastValueTimestamp = 0;
                if (data.channels) {
                    Ext.Array.each(data.channels, function(channel) {
                        if (channel.lastValueTimestamp && channel.lastValueTimestamp > mostRecentLastValueTimestamp) {
                            mostRecentLastValueTimestamp = channel.lastValueTimestamp;
                        }
                    });
                    return mostRecentLastValueTimestamp>0 ? mostRecentLastValueTimestamp : undefined;
                }
                return undefined;
            }
        }
    ],
    proxy: {
        type: 'rest',
        timeout: 120000,
        url: '/api/ddr/devices/{deviceId}/loadprofiles',
        reader: {
            type: 'json'
        }
    }
});