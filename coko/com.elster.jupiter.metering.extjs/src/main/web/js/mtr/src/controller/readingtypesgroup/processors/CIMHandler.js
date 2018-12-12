/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
/**
 * This class handles the MRID processing from the URL.
 *
 * When Add Reading Type is called from the Add Register Type page, a
 * MRID value can be present in the URL. If true, we'll preload all reading
 * type comboboxes with the corresponding value from the URL MRID.
 *
 */
Ext.define('Mtr.controller.readingtypesgroup.processors.CIMHandler', {

    cimValues: null,

    process: function () {
        var me = this,
            queryValues = Uni.util.QueryString.getQueryStringValues(false);

        if (queryValues.mRID) {
            me.cimValues = queryValues.mRID.split(".").map(function (item) {
                return parseInt(item, 10);
            });
        }
    },

    /**
     * Return value at index or NOT_APPLICABLE(0)
     * @param index combo index
     * @returns {number}
     */
    getValue: function (index) {
        return (this.cimValues && this.cimValues.length > (index - 1)) ?  this.cimValues[index - 1] : 0;
    }
});

