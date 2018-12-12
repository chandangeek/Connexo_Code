/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.controller.readingtypesgroup.processors.AlwaysVisibleComboProcessor', {
    extend: 'Mtr.controller.readingtypesgroup.processors.ComboProcessor',

    // This flag is set for those combos that need to set a value and disable
    // Currently is set when we're cloning from a reading type set
    selectAndDisable: false,

    isDisabled: function (){
        return this.controller.getBasicCommodity().getValue() === this.NOT_APPLICABLE;
    },

    setState: function (state) {
        this.getCombo().setDisabled(state);
    },

    getFilterParam: function (){
        return this.controller.getBasicCommodity().getValue();
    },

    /**
     * If cloning from ReadingTypeSet => load store, select clone value, set disabled
     * If cloning from AddRegister => load store, select clone, set enabled
     * If no cloning select NOT_APPLICABLE
     */
    process: function() {
        if (this.isDisabled()){
            this.restoreValue(true);
        } else {
            this.loadAndSelect(this.selectAndDisable);
        }
    },

    /**
     * @param disabled {boolean} 'true' means disable combobox
     */
    loadAndSelect: function (disabled) {
        this.getCombo().getStore().getProxy().setExtraParam('filter', this.getFilterParam());
        this.setComboValue(disabled);
    }
});
