Ext.define('Mdc.model.DeviceConnection', {
    extend: 'Uni.model.ParentVersion',

    fields: [
        {name: 'id', type: 'number'},
        {name: 'name', type: 'auto'},
        {name: 'currentState', type: 'auto'},
        {name: 'latestStatus', type: 'auto'},
        {name: 'latestResult', type: 'auto'},
        {name: 'taskCount', type: 'auto'},
        {name: 'startDateTime', type: 'date', dateFormat: 'time'},
        {name: 'endDateTime', type: 'date', dateFormat: 'time'},
        {name: 'duration', type: 'auto'},
        {name: 'comPort', type: 'auto'},
        {name: 'comPortPool', type: 'auto'},
        {name: 'direction', type: 'string'},
        {name: 'connectionType', type: 'string'},
        {name: 'comServer', type: 'auto'},
        {name: 'connectionMethod', type: 'auto'},
        {name: 'window', type: 'string'},
        {name: 'connectionStrategy', type: 'auto'},
        {name: 'nextExecution', type: 'date', dateFormat: 'time'},
        {name: 'comSessionId', type: 'number'},

        // mapped fields
        {name: 'isDefault', type: 'boolean', mapping: function (data) {return data.connectionMethod.isDefault;}, persist: false}
    ],

    run: function(callback, body) {
        Ext.Ajax.request({
            method: 'PUT',
            url: this.proxy.url + '/{id}/run'.replace('{id}', this.getId()),
            callback: callback,
            isNotEdit: true,
            jsonData: body
        });
    },
    deactivate: function(callback, body) {
        Ext.Ajax.request({
            method: 'PUT',
            url: this.proxy.url + '/{id}'.replace('{id}', this.getId()),
            callback: callback,
            isNotEdit: true,
            jsonData: body
        });
    },

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/connections',
        reader: {
            type: 'json',
            root: 'connections',
            totalProperty: 'total'
        }
    }
});

