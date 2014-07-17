Ext.define('Mdc.store.ComPortPoolComports',{
    extend: 'Ext.data.Store',
    model: 'Mdc.model.ComPort',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/mdc/comports',
        reader: {
            type: 'json',
            root: 'data'
        }
    },

    sortByType: function(type) {
        var me= this,
            arrayToRemove = [];
        me.each(function(record) {
                if (record.getData().comPortType != type) {
                    arrayToRemove.push(record)
                }
        });
        me.remove(arrayToRemove);
    },

    sortByExisted: function(recordsArr) {
        var me= this;
        Ext.Array.each (recordsArr, function(record) {
            if (me.getById(record.id)){
                me.remove(me.getById(record.id));
            }
        });
    }
});