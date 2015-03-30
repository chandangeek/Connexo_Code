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
        object.set(data);
        object.getProxy().getReader().readAssociated(object, data);
    }
});