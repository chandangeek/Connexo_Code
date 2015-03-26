Ext.define('Dlc.devicelifecyclestates.model.DeviceLifeCycleState', {
  extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name:'name', type: 'string'}
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/dld/devicelifecycles/{id}/states',
        reader: {
            type: 'json',
            root: 'deviceLifeCycleStates'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{id}', params.deviceLifeCycleId);
        }
    }
});
