/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.form.Labelable', {
    override: 'Ext.form.Labelable',

    getLabelStyleMarginProp: function () {
        return this.getHierarchyState().rtl ? 'margin-left:' : 'margin-right:';
    }
});
