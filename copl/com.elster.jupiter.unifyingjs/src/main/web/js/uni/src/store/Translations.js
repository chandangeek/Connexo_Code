Ext.define('Uni.store.Translations', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.Translation',
    autoLoad: false,
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