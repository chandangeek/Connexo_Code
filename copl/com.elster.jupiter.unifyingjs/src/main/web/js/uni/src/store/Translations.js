Ext.define('Uni.store.Translations', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.Translation',
    storeId: 'translations',
    singleton: true,
    autoLoad: false,
    clearOnPageLoad: false,
    clearRemovedOnLoad: false,
    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
            root: 'items'
        }
    }
//    data: [
//        {
//            key: 'navigation',
//            value: 'Navigation'
//        }
//    ]
});