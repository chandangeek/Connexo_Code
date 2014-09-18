Ext.define('Mdc.store.SelectedMessageCategories', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.MessageCategory'
    ],
    model: 'Mdc.model.MessageCategory',
    storeId: 'SelectedMessageCategories'

});