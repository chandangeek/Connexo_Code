Ext.define('Mdc.store.UserFileReferences', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.UserFileReference'
    ],
    model: 'Mdc.model.UserFileReference',
    storeId: 'UserFileReferences',
    autoLoad: true,
    proxy: {
        type: 'rest',
        url: '../../api/plr/userfilereferences',
        reader: {
            type: 'json',
            root: 'UserFile'
        }
    }
});