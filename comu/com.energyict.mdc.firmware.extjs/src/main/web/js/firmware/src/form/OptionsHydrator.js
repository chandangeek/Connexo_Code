/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.form.OptionsHydrator', {
    extend: 'Fwc.form.Hydrator',
    /**
     * Extracts data from the provided object
     * @param object Ext.data.Model
     * @returns {Object}
     */
    extract: function () {
        var data = this.callParent(arguments);
        data.allowedOptions = data.allowedOptions.map(function (item) {
            return item.id;
        });
        data.isAllowed = +data.isAllowed;
        return data;
    },

    /**
     * Hydrates data to the provided object
     *
     * @param data
     * @param object
     */
    hydrate: function (data, object) {
        if (!data.allowedOptions) {
            data.allowedOptions = [];
        }
        if (!Ext.isArray(data.allowedOptions)) {
            data.allowedOptions = [data.allowedOptions]
        }
        data.allowedOptions = data.allowedOptions.map(function (item) {
            return {id: item};
        });
        delete data.supportedOptions;
        delete data.id;

        this.callParent(arguments);
    }
});