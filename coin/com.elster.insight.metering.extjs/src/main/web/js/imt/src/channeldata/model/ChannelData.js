Ext.define('Imt.channeldata.model.ChannelData', {
    extend: 'Ext.data.Model',
    //idgen: 'sequential',
    fields: [
        {name: 'value', type: 'auto'},
        {name: 'interval', type: 'auto'},
        {name: 'readingTime', dateFormat: 'time', type: 'date'},
        {name: 'mainValidationInfo', type: 'auto'},
        'readingQualities',
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
    ],
    // Called to get more details regarding validation state, rules, etc.
    refresh: function(mrid, channel, callback) {
        var me = this;

        Ext.Ajax.request({
            url: '/api/udr/usagepoints/{mrid}/channels/{channel}/data/{reading}/validation'
                .replace('{mrid}', mrid)
                .replace('{channel}', channel)
                .replace('{reading}', me.get('interval').end),
            success: function(response) {
                var data = Ext.decode(response.responseText);
                //Ext.apply(me.raw, data)
                me.beginEdit();
                me.set(data);
                me.endEdit(true);

                callback ? callback() : null;
            }
        })
    }
});
