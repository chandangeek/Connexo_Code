Ext.define('Mdc.model.LoadProfilesOfDeviceData', {
    extend: 'Ext.data.Model',
    idProperty: 'interval_end',
    fields: [
        {name: 'interval', type: 'auto'},
        {name: 'readingTime', dateFormat: 'time', type: 'date'},
        {name: 'intervalFlags', type: 'auto'},
        {name: 'channelData', type: 'auto'},
        {name: 'channelValidationData', type: 'auto'},
        {name: 'channelCollectedData', type: 'auto'},
        {name: 'validationActive', type: 'auto'},
        {
            name: 'interval_end',
            persist: false,
            mapping: 'interval.end',
            dateFormat: 'time',
            type: 'date'
        }
    ],

    refresh: function(device, channel, callback) {
        var me = this;

        Ext.Ajax.request({
            url: '/api/ddr/devices/{device}/channels/{channel}/data/{reading}/validation'
                .replace('{device}', device)
                .replace('{channel}', _.keys(me.get('channelData'))[0])
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