/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.dom.Element_anim', {
    override: 'Ext.dom.Element',

    rtlXAnchors: {
        l: 'r',
        r: 'l'
    },

    anchorAnimX: function(anchor) {
        if (Ext.rootHierarchyState.rtl) {
            anchor = this.rtlXAnchors[anchor];
        }
        this.callParent(arguments);
    }
});