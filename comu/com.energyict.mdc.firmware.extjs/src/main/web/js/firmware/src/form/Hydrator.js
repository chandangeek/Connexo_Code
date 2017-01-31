/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.form.Hydrator', {

    /**
     * Extracts data from the provided object
     * @param object Ext.data.Model
     * @returns {Object}
     */
    extract: function (object) {
        return object.getData(true);
    },

    /**
     * Hydrates data to the provided object
     *
     * @param data
     * @param object
     */
    hydrate: function (data, object) {
        object.set(_.pick(data, object.fields.keys));

        //clean up on stores
        object.associations.each(function (item) {
            if (item.type === 'hasMany' && object[item.storeName]) {
                object[item.storeName].removeAll();
            }
        });
        object.getProxy().getReader().readAssociated(object, data);
    }
});