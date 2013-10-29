Ext.define('Uni.util.JsonWriterOverride', {
    override: 'Ext.data.writer.Json',

    /**
     * Adds the associated data to the returned record.
     * @param record
     * @returns {*}
     */
    getRecordData: function (record) {
        Ext.apply(record.data, record.getAssociatedData());
        return record.data;
    }

});