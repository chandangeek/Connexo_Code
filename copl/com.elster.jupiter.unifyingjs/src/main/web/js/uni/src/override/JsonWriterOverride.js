/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.override.JsonWriterOverride
 */
Ext.define('Uni.override.JsonWriterOverride', {
    override: 'Ext.data.writer.Json',

    /**
     * Adds the associated data to the returned record.
     * @param record
     * @returns {*}
     */
    getRecordData: function (record, operation) {
     //   Ext.apply(record.data, record.getAssociatedData());
        return record.getWriteData(true, true);
    }

});