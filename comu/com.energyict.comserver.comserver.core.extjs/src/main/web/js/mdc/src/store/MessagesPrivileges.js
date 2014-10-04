Ext.define('Mdc.store.MessagesPrivileges', {
    extend: 'Ext.data.Store',
    storeId: 'MessagesPrivileges',
    autoLoad: true,
    fields: [
        { name: 'name', type: 'string' },
        { name: 'roles', type: 'auto' }
    ],
    proxy: {
        type: 'rest',
//        url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/validationrulesets',
        url: '/apps/mdc/fakedata/privileges.json',
        reader: {
            type: 'json',
            root: 'privileges'
        }
    }
});