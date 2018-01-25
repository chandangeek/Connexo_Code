/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.controller.readingtypesgroup.processors.AggregateProcessor', {
    extend: 'Mtr.controller.readingtypesgroup.processors.AlwaysVisibleComboProcessor',

    getCombo: function (){
        return this.controller.getBasicAggregate();
    },

    /**
     * Load store and select value when clone we have a clone value and
     * it's not disabled. Currently the combo is disabled if commodity is not set
     */
    process: function() {
        var disabled = this.isDisabled();
        if (this.cloneValue && !disabled){
            this.setComboValue(false);
        } else {
            this.restoreValue(disabled);
        }
    }
});
