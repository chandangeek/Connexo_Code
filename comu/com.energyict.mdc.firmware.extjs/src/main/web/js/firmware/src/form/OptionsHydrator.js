Ext.define('Fwc.form.OptionsHydrator', {
    extend: 'Fwc.form.Hydrator',
    /**
     * Extracts data from the provided object
     * @param object Ext.data.Model
     * @returns {Object}
     */
    extract: function () {
        var data = this.callParent(arguments);

        return {
            supportedOptions: !!data.supportedOptions.length,
            allowedOptions: data.allowedOptions.map(function (item) {
                return item.id;
            })
        };
    },

    /**
     * Hydrates data to the provided object
     *
     * @param data
     * @param object
     */
    hydrate: function (data, object) {
        if (data.allowedOptions) {
            data.allowedOptions = data.allowedOptions.map(function (item) {
                return {id: item};
            });
        }
        delete data.supportedOptions;
        delete data.id;

        this.callParent(arguments);
    }
});