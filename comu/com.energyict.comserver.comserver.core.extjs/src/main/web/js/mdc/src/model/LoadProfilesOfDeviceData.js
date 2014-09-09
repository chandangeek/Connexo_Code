Ext.define('Mdc.model.LoadProfilesOfDeviceData', {
    extend: 'Ext.data.Model',
    idgen: 'sequential',
    fields: [
        {name: 'interval', type: 'auto'},
        {name: 'readingTime', dateFormat: 'time', type: 'date'},
        {name: 'intervalFlags', type: 'auto'},
        {name: 'channelData', type: 'auto'},
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
            mapping: function (data) {
                return data.interval ? Uni.I18n.formatDate('deviceloadprofiles.data.dateFormat', new Date(data.interval.end), 'MDC', 'M d, Y \\a\\t H:i') : '';
            }
        },
        {
            name: 'interval_formatted',
            persist: false,
            mapping: function (data) {
                return data.interval ? Uni.I18n.formatDate('deviceloadprofiles.data.dateFormat', new Date(data.interval.start), 'MDC', 'M d, Y \\a\\t H:i') + ' - ' + Uni.I18n.formatDate('deviceloadprofiles.data.dateFormat', new Date(data.interval.end), 'MDC', 'M d, Y \\a\\t H:i') : '';
            }
        },
        {
            name: 'readingTime_formatted',
            persist: false,
            mapping: function (data) {
                return data.readingTime ? Uni.I18n.formatDate('deviceloadprofiles.data.dateFormat', new Date(data.readingTime), 'MDC', 'M d, Y \\a\\t H:i') : '';
            }
        }
    ]
});