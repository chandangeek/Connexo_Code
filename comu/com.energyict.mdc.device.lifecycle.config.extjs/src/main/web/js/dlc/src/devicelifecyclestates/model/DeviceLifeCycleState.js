Ext.define('Dlc.devicelifecyclestates.model.DeviceLifeCycleState', {
  extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name:'name', type: 'string'},
        {name: 'isCustom', type: 'boolean'},
        {name: 'isInitial', type: 'boolean'},
        {
            name: 'sorted_name',
            persist: false,
            mapping: function (data) {
                return data.name;
            }
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/dld/devicelifecycles/{id}/states',
        reader: {
            type: 'json'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{id}', params.deviceLifeCycleId);
        }
    }
});
