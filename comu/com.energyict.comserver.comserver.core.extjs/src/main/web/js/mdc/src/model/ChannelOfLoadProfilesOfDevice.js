Ext.define('Mdc.model.ChannelOfLoadProfilesOfDevice', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Mdc.store.TimeUnits'
    ],
    fields: [
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'},
        {name: 'interval', type: 'auto'},
        {name: 'readingType', type: 'auto'},
        {name: 'calculatedReadingType', type: 'auto'},
        {name: 'obisCode', type: 'string'},
        {name: 'multiplier', type: 'auto'},
        {name: 'useMultiplier', type: 'boolean'},
        {name: 'overflowValue', type: 'int'},
        {name: 'nbrOfFractionDigits', type: 'int'},
        {name: 'flowUnit', type: 'string'},
        {name: 'lastReading', type: 'long', useNull: true},
        {name: 'lastValueTimestamp', type: 'long', useNull: true},
        {name: 'lastChecked', dateFormat: 'time', type: 'date'},
        {name: 'validationInfo', type: 'auto'},
        {name: 'loadProfileId', type: 'auto'},
        {
            name: 'validationInfo_validationActive',
            persist: false,
            mapping: function (data) {
                return (data.validationInfo && data.validationInfo.validationActive) ? Uni.I18n.translate('general.active', 'MDC', 'Active') : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
            }
        },
        {
            name: 'validationInfo_dataValidated',
            persist: false,
            mapping: function (data) {
                return (data.validationInfo && data.validationInfo.dataValidated) ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                    : Uni.I18n.translate('general.no', 'MDC', 'No') + ' ' + '<span class="icon-validation icon-validation-black"></span>';
            }
        },
        {
            name: 'lastChecked_formatted',
            persist: false,
            mapping: function (data) {
                return (data.validationInfo && data.validationInfo.lastChecked)
                    ? Uni.DateTime.formatDateTimeLong(new Date(data.validationInfo.lastChecked))
                    : Uni.I18n.translate('general.never', 'MDC', 'Never');
            }
        },
        {
            name: 'interval_formatted',
            persist: false,
            mapping: function (data) {
                var value = data.interval,
                    timeUnitsStore = Ext.getStore('Mdc.store.TimeUnits'),
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
            name: 'lastValueTimestamp_formatted',
            persist: false,
            mapping: function (data) {
                if (data.lastReading && data.lastValueTimestamp) {
                    var date = new Date(data.lastValueTimestamp);
                    return Uni.DateTime.formatDateLong(date) + ' - ' + Uni.DateTime.formatTimeShort(date)
                }
                return '-';
            }
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/channels',
        reader: {
            type: 'json'
        },
        timeout: 300000,
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(params.mRID));
        }
    }
});