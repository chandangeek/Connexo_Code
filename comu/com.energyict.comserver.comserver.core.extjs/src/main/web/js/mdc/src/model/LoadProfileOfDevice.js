Ext.define('Mdc.model.LoadProfileOfDevice', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.store.TimeUnits'
    ],
    fields: [
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'},
        {name: 'obisCode', type: 'string'},
        {name: 'interval', type: 'auto'},
        {name: 'lastReading', dateFormat: 'time', type: 'date'},
        {name: 'channels', type: 'auto'},
        {name: 'lastChecked', dateFormat: 'time', type: 'date'},
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
            name: 'validationInfo_dataValidated',
            persist: false,
            mapping: function (data) {
                return (data.validationInfo && data.validationInfo.dataValidated) ? Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                    : Uni.I18n.translate('general.no', 'MDC', 'No') + ' ' + '<span class="icon-validation icon-validation-black"></span>';
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
        }
    ],
    proxy: {
        type: 'rest',
        timeout: 120000,
        urlTpl: '/api/ddr/devices/{mRID}/loadprofiles',
        reader: {
            type: 'json'
        },

        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }
});