Ext.define('Dlc.store.DeviceLifeCycles', {
    extend: 'Ext.data.Store',
    model: 'Dlc.model.DeviceLifeCycle',
    data: [
        {name: 'Standard device life cycle'},
        {name: 'Short device life cycle'},
        {name: 'Extended device life cycle'}
    ]
});