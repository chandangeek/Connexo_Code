Ext.define('Imt.channeldata.model.ChannelData', {
    extend: 'Ext.data.Model',
    //idgen: 'sequential',
    fields: [
        {name: 'value', type: 'auto'},
        {name: 'interval', type: 'auto'},
        {
            name: 'interval_start',
            persist: false,
            mapping: function (data) {
                return data.interval ? Uni.I18n.formatDate('channeldata.dateFormat', new Date(data.interval.start), 'IMT', 'M d, Y \\a\\t H:i') : '';
            }
        },
        {
            name: 'interval_end',
            persist: false,
            mapping: 'interval.end',
            dateFormat: 'time',
            type: 'date'
        }
    ]
});
