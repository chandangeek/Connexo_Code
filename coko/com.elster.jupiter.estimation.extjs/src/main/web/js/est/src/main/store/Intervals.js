Ext.define('Est.main.store.Intervals', {
    extend: 'Ext.data.Store',
    model: 'Est.main.model.Interval',

    proxy: {
        type: 'rest',
        url: '/api/mtr/fields/intervals',
        reader: {
            type: 'json',
            root: 'intervals'
        },

        pageParam: false,
        startParam: false,
        limitParam: false
    }
});