/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.controller.readingtypesgroup.processors.CommodityProcessor', {
    extend: 'Mtr.controller.readingtypesgroup.processors.ComboProcessor',
    disabledForLoad: false,
    process: function(){
        var me = this,
            combo = me.getCombo();

        combo.setDisabled(me.disabledForLoad);

        if (this.cloneValue){
            this.setComboValue();
        }
    },

    setComboValue: function (){
        var me = this,
            combo = me.getCombo(),
            record = null;

        combo.getStore().load({
            callback: function (records, operation, success) {
                if (success) {
                    // Commodity combo has only one value for both primary and secondary electricity
                    if (me.cloneValue === me.SECONDARY_ELECTRICITY){
                        me.cloneValue = me.PRIMARY_ELECTRICITY;
                    }
                    record = combo.findRecordByValue(me.cloneValue);
                    if (record) {
                        combo.select(record);
                        me.cloneValue = null;
                    }
                }
            }
        });
    },

    getCombo: function () {
        return this.controller.getBasicCommodity();
    }
});
