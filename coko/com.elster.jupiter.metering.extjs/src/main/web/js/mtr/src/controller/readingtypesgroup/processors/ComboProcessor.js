/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
/**
 * Abstract class. Classes extending this will handle the processing for
 * reading type attribute comboboxes
 */
Ext.define('Mtr.controller.readingtypesgroup.processors.ComboProcessor', {

    config: {
        cloneValue: null,
        controller: null
    },

    NOT_APPLICABLE: 0,
    SECONDARY_ELECTRICITY: 1,
    PRIMARY_ELECTRICITY: 2,

    constructor: function(config) {
        this.initConfig(config);
    },

    /**
     * This will set the value in the combo when it's loaded/enabled/visible
     */
    setComboValue: function (){
        var me = this,
            combo = me.getCombo(),
            record = null;

        combo.getStore().load({
            callback: function (records, operation, success) {
                if (success) {
                    // Check if we have a preload/clone value and it matches the store
                    if (me.cloneValue){
                        record = combo.findRecordByValue(me.cloneValue);
                        me.cloneValue = null;
                    }
                    if (record){
                        combo.select(record);
                    } else {
                        combo.select(me.NOT_APPLICABLE);
                    }
                }
            }
        });
    },

    /**
     * If combo is not visible/enabled and the store is loaded, we reset the value
     */
    resetComboValue: function() {
        var me = this,
            storeSize = me.getCombo().getStore().data.length;
        if (storeSize > 0) {
            this.getCombo().select(this.NOT_APPLICABLE);
        }
    },


    /**
     * This method is called when the commodity is changed, which is the main
     * trigger for all other comboboxes on the reading type page. This will
     * handle the visible/disable logic and will load/set a value in the combo
     */
    process: function(){
        throw "Method not implemented"
    },

    /**
     * Each processor will return the combo that it handles
     */
    getCombo: function() {
        throw "Method not implemented"
    }
});
