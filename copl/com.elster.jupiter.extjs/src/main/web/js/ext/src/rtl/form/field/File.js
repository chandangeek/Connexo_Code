/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.form.field.File', {
    override: 'Ext.form.field.File',

    getButtonMarginProp: function() {
        return this.getHierarchyState().rtl ? 'margin-right:' : 'margin-left:';
    }
});