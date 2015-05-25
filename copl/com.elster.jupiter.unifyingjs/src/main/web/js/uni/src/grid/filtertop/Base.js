/**
 * @class Uni.grid.filtertop.Base
 */
Ext.define('Uni.grid.filtertop.Base', {
    mixins: {
        observable: 'Ext.util.Observable'
    },

    /**
     * @cfg {String} dataIndex
     * The {@link Ext.data.Store} dataIndex of the field this filter represents.
     * The dataIndex does not actually have to exist in the store.
     */
    dataIndex: null,

    /**
     * Template method to be implemented by all subclasses that is to
     * initialize the filter and install required menu items.
     * Defaults to Ext.emptyFn.
     */
    init: Ext.emptyFn,

    /**
     * Template method to be implemented by all subclasses that is to
     * get and return the value of the filter.
     * @return {Object} The 'serialized' form of this filter
     * @template
     */
    getFilterValue: function () {
        if (this.getValue) {
            return this.getValue();
        }
    },

    /**
     * Template method to be implemented by all subclasses that is to
     * set the value of the filter and fire the 'update' event.
     * @param {Object} data The value to set the filter
     * @template
     */
    setFilterValue: function (data) {
        if (this.setValue) {
            this.setValue(data);
        }
    },

    resetValue: function () {
        if (this.reset) {
            this.reset();
        }
    },

    /**
     * Template method to be implemented by all subclasses that is to
     * get and return serialized filter data for transmission to the server.
     */
    getParamValue: function () {
        if (this.getValue) {
            return this.getValue();
        }

        return undefined;
    },

    /**
     * Template method that is supposed to be overwritten when doing complex changes to the params.
     */
    applyParamValue: undefined,

    /**
     * @cfg {Boolean} active
     * Whether this filter item is active or not. Default true.
     */
    active: true,

    fireFilterUpdateEvent: function () {
        this.fireEvent('filterupdate');
    },

    /**
     * Template method to be implemented by all subclasses that is to
     * validates the provided Ext.data.Record against the filters configuration.
     * Defaults to always returning true.
     * @param {Ext.data.Record} record The record to validate
     * @return {Boolean} true if the record is valid within the bounds
     * of the filter, false otherwise.
     */
    validateRecord: function () {
        return true;
    },

    generateRandomName: function () {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
            var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }
});