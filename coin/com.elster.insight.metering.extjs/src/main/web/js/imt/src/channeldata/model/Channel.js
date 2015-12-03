Ext.define('Imt.channeldata.model.Channel', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Imt.store.TimeUnits'
    ],
    fields: [
        {
            name: 'id', 
            type: 'string',
            mapping: function(data) {
                return data.readingType.mRID;
            }
        },
        {name: 'name', type: 'string'},
        {name: 'interval', type: 'auto'},
        {name: 'unitOfMeasure', type: 'auto'},
        {name: 'readingType', type: 'auto'},
        {name: 'calculatedReadingType', type: 'auto'},
        {name: 'obisCode', type: 'string'},
        {name: 'overflowValue', type: 'int'},
        {name: 'nbrOfFractionDigits', type: 'int'},
        {name: 'flowUnit', type: 'string'},
        {name: 'lastReading', dateFormat: 'time', type: 'date'},
        {name: 'lastValueTimestamp', dateFormat: 'time', type: 'date'},
        {name: 'lastChecked', dateFormat: 'time', type: 'date'},
        {name: 'validationInfo', type: 'auto'},
        {name: 'loadProfileId', type: 'auto'},
        {
            name: 'validationInfo_validationActive',
            persist: false,
            mapping: function (data) {
                return (data.validationInfo && data.validationInfo.validationActive) ? Uni.I18n.translate('general.active', 'IMT', 'Active') : Uni.I18n.translate('general.inactive', 'IMT', 'Inactive');
            }
        },
        {
            name: 'validationInfo_dataValidated',
            persist: false,
            mapping: function (data) {
                return (data.validationInfo && data.validationInfo.dataValidated) ? Uni.I18n.translate('general.yes', 'IMT', 'Yes')
                    : Uni.I18n.translate('general.no', 'IMT', 'No') + ' ' + '<span class="icon-validation icon-validation-black"></span>';
            }
        },
        {
            name: 'lastChecked_formatted',
            persist: false,
            mapping: function (data) {
                return (data.validationInfo && data.validationInfo.lastChecked)
                    ? Uni.DateTime.formatDateTimeLong(new Date(data.validationInfo.lastChecked))
                    : Uni.I18n.translate('general.never', 'IMT', 'Never');
            }
        },
        {
            name: 'interval_formatted',
            persist: false,
            mapping: function (data) {
                var value = data.interval,
                    timeUnitsStore = Ext.getStore('Imt.store.TimeUnits'),
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
                return data.lastReading && data.lastValueTimestamp
                    ? Uni.DateTime.formatDateTimeLong(new Date(data.lastValueTimestamp))
                    : '';
            }
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{mRID}/channels',
        reader: {
            type: 'json'
        },
        timeout: 300000,
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(params.mRID));
        }
    }
});