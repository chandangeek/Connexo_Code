/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.form.field.Spinner', {
    override: 'Ext.form.field.Spinner',

    getTriggerData: function(){
        var data = this.callParent();
        if (this.getHierarchyState().rtl) {
            data.childElCls = this._rtlCls;
        }
        return data;
    }
});