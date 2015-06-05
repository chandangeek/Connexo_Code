Ext.define('Dsh.store.ConnectionTaskProperties', {
    extend: 'Uni.data.store.Filterable',
    requires: [
        'Dsh.model.ConnectionTaskProperties'
    ],
    model: 'Dsh.model.ConnectionTaskProperties',
    autoLoad: false,
    remoteFilter: true
});
