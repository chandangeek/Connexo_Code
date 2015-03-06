Ext.define('Cfg.model.DeviceGroup', {
    extend: 'Ext.data.Model',
    proxy: 'memory',
    fields: [
        { name: 'id', type: 'int'},
        { name: 'mRID', type: 'string'},
        { name: 'name', type: 'string'},
        { name: 'dynamic', type: 'boolean'},
        { name: 'criteria'}
    ]
});
