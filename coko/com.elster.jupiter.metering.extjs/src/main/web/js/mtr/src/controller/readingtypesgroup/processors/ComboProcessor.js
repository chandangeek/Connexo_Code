/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
/**
 * Abstract class. Classes extending this will handle the processing for
 * reading type attribute comboboxes
 */
Ext.define('Mtr.controller.readingtypesgroup.processors.ComboProcessor', {

    config: {
        cloneValue: this.NOT_APPLICABLE,
        controller: null
    },

    NOT_APPLICABLE: 0,
    SECONDARY_ELECTRICITY: 1,
    PRIMARY_ELECTRICITY: 2,

    constructor: function(config) {
        this.initConfig(config);
    },


    /**
     * Load combo store.
     * If we are cloning , we try to match and select the clone value in the store
     * Otherwise we default to NOT_APPLICABLE
     * After the selection is done we call setDisabled/setVisible on the combo. It is
     * important that setDisabled to be called after the value was selectet. Otherwise,
     * when extracting the combo record from the page form, the value is not the one we
     * expected.
     * @param state {boolean} visible/hidden/enabled/disabled
     */
    setComboValue: function (state) {
        var me = this,
            combo = me.getCombo();

        combo.getStore().load({
            callback: function (records, operation, success) {
                if (success) {
                    var record = combo.findRecordByValue(me.cloneValue);
                    if (record) {
                        combo.select(record);
                    } else {
                        combo.select(me.NOT_APPLICABLE);
                    }
                    me.setState(state);
                    me.cloneValue = me.NOT_APPLICABLE;
                }
            }
        });
    },

    /**
     * Restores the combo value to NOT_APPLICABLE is the store is loaded
     * and enables/disables the combo based on the input param
     * @param state `true` to disable the combobox
     */
    restoreValue: function (state) {
        var me = this,
            combo = me.getCombo(),
            storeSize = combo.getStore().data.length;
        if (storeSize > 0) {
            combo.reset();
        }
        me.setState(state);
    },

    /**
     * This method is called when the commodity is changed, which is the main
     * trigger for all other comboboxes on the reading type page. This will
     * handle the visible/disable logic and will load/set a value in the combo
     */
    process: function(){
        throw "Method not implemented";
    },

    /**
     * Each processor will return the combo that it handles
     */
    getCombo: function() {
        throw "Method not implemented";
    },

    setState: function () {
        throw "Method not implemented";
    }
});
