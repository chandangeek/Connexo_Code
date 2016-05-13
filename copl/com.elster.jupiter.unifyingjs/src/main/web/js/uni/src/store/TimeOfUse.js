Ext.define('Uni.store.TimeOfUse', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.TimeOfUse',

    proxy: {
        type: 'rest',
        url: '/api/mtr/fields/timeofuse',
        reader: {
            type: 'json',
            root: 'timeOfUse'
        },

        pageParam: false,
        startParam: false,
        limitParam: false
    }
});