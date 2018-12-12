/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.form.field.Checkbox', {
    override: 'Ext.form.field.Checkbox',

    getSubTplData: function(){
        var data = this.callParent();
        if (this.getHierarchyState().rtl) {
            data.childElCls = this._rtlCls;
        }
        return data;
    }
});