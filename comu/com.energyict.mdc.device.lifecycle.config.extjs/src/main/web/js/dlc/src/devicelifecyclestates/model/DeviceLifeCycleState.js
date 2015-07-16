Ext.define('Dlc.devicelifecyclestates.model.DeviceLifeCycleState', {
    extend: 'Ext.data.Model',
    alias: 'deviceLifeCycleState',
    requires: [
        'Dlc.devicelifecyclestates.model.TransitionBusinessProcess'
    ],
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'isCustom', type: 'boolean'},
        {name: 'isInitial', type: 'boolean'},
        {
            name: 'sorted_name',
            persist: false,
            mapping: function (data) {
                return data.name;
            }
        },
        'onEntry',
        'onExit'
    ],

    associations: [
        {name: 'onEntry', type: 'hasMany', model: 'Dlc.devicelifecyclestates.model.TransitionBusinessProcess', associationKey: 'onEntry'},
        {name: 'onExit', type: 'hasMany', model: 'Dlc.devicelifecyclestates.model.TransitionBusinessProcess', associationKey: 'onExit'}
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
