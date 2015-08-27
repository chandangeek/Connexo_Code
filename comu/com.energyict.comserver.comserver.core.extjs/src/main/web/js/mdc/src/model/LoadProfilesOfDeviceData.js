Ext.define('Mdc.model.LoadProfilesOfDeviceData', {
    extend: 'Ext.data.Model',
    idgen: 'sequential',
    fields: [
        {name: 'interval', type: 'auto'},
        {name: 'readingTime', dateFormat: 'time', type: 'date'},
        {name: 'intervalFlags', type: 'auto'},
        {name: 'channelData', type: 'auto',
            convert: function (v) {
                for(var key in v) {
                    if(v.hasOwnProperty(key)) {
                        if(!Ext.isEmpty((v[key]))) {
                           v[key] = Uni.Number.formatNumber(v[key], -1);
                        }
                    }

                }
            return v;
            }
        },
        {name: 'channelValidationData', type: 'auto'},
        {name: 'channelCollectedData', type: 'auto'},
        {name: 'validationActive', type: 'auto'},
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
        }
    ]
});