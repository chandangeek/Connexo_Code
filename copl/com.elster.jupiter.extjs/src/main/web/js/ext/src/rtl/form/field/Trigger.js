/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.form.field.Trigger', {
    override: 'Ext.form.field.Trigger',

    beforeRender: function(){
        if (this.getHierarchyState().rtl) {
            this.extraTriggerCls = this._rtlCls;
        }
        this.callParent(arguments);
    }
});