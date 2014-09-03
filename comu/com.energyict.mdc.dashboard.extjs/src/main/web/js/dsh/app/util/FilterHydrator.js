Ext.define('Dsh.util.FilterHydrator', {
    extract: function(record) {
        return record.getData(true);
    },
    hydrate: function(data, record) {
        record.data = data;
    }
});