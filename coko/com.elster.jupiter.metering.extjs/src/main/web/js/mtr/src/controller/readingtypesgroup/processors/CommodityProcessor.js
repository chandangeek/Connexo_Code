/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.controller.readingtypesgroup.processors.CommodityProcessor', {
    extend: 'Mtr.controller.readingtypesgroup.processors.ComboProcessor',
    selectAndDisable: false,

    process: function () {
        if (this.cloneValue) {
            this.setComboValue(this.selectAndDisable);
        }
    },

    setComboValue: function (disabled){
        var me = this,
            combo = me.getCombo();

        combo.getStore().load({
            callback: function (records, operation, success) {
                if (success) {
                    // Commodity combo has only one value for both primary and secondary electricity
                    if (me.cloneValue === me.SECONDARY_ELECTRICITY){
                        me.cloneValue = me.PRIMARY_ELECTRICITY;
                    }
                    var record = combo.findRecordByValue(me.cloneValue);
                    if (record){
                        combo.select(record);
                    }
                    me.cloneValue = me.NOT_APPLICABLE;
                    combo.setDisabled(disabled);
                }
            }
        });
    },

    getCombo: function () {
        return this.controller.getBasicCommodity();
    }
});
