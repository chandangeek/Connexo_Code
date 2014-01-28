Ext.define('Mdc.store.LoadProfileTypes', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.LoadProfileType'
    ],
    model: 'Mdc.model.LoadProfileType',
    storeId: 'LoadProfileTypes',
    autoLoad: true,
    proxy: {
        type: 'rest',
        url: '../../api/mdc/loadprofiletypes',
        reader: {
            type: 'json',
            root: 'LoadProfileType'
        }
    }
});