Ext.define('Uni.grid.filtertop.NoUi', {
    mixins: {
        observable: 'Ext.util.Observable'
    },


    dataIndex: null,
    init: Ext.emptyFn,
    getFilterValue: function () {
        if (this.getValue) {
            return this.getValue();
        }
    },

    setInitialValue: function(value) {
        this.initialValue = value;
    },

    setFilterValue: function (data) {
        this.value = data;
    },

    resetValue: function () {
        this.value = this.hasOwnProperty('initialValue') ? this.initialValue : null;
    },

    /**
     * Template method to be implemented by all subclasses that is to
     * get and return serialized filter data for transmission to the server.
     */
    getParamValue: function () {
        if(this.value){
            return this.value;
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

    validateRecord: function () {
        return true;
    },

    generateRandomName: function () {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
            var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    },

    constructor: function (config) {
        this.mixins.observable.constructor.call(this, config);
    }

    //initComponent: function () {
    //    var me = this;
    //    me.callParent(arguments);
    //}
});