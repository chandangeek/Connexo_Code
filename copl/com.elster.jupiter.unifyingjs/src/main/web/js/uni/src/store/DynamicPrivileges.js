Ext.define('Uni.store.DynamicPrivileges', {
    extend: 'Ext.data.Store',
    singleton: true,

    fields: [
        'name'
    ],

    autoLoad: false
});