Ext.define('Mdc.model.ChannelOfLoadProfilesOfDevice', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.store.TimeUnits'
    ],
    fields: [
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'},
        {name: 'interval', type: 'auto'},
        {name: 'unitOfMeasure', type: 'auto'},
        {name: 'cimReadingType', type: 'string'},
        {name: 'obisCode', type: 'string'},
        {name: 'multiplier', type: 'int'},
        {name: 'overflowValue', type: 'int'},
        {name: 'flowUnit', type: 'string'},
        {name: 'lastReading', dateFormat: 'time', type: 'date'},
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
            name: 'unitOfMeasure_formatted',
            persist: false,
            mapping: function (data) {
                return (data.unitOfMeasure && data.unitOfMeasure.localizedValue) ? data.unitOfMeasure.localizedValue : '';
            }
        },
        {
            name: 'lastReading_formatted',
            persist: false,
            mapping: function (data) {
                return data.lastReading ? Uni.I18n.formatDate('deviceloadprofiles.dateFormat', new Date(data.lastReading), 'MDC', 'M d, Y H:i') : '';
            }
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/loadprofiles/{loadProfileId}/channels',
        reader: {
            type: 'json'
        },

        setUrl: function (params) {
            this.url = this.urlTpl.replace('{mRID}', params.mRID).replace('{loadProfileId}', params.loadProfileId);
        }
    }
});