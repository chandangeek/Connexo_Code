/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('ChartsKitchenSink.view.CodePreview', {
    extend: 'Ext.panel.Panel',
    xtype: 'codePreview',
    autoScroll: true,
    cls: 'preview-container',
    bodyStyle: 'padding: 5px;',


    // The code must be read in LTR
    rtl: false,

    initComponent: function() {
        this.ui = (Ext.themeName === 'neptune') ? 'light' : 'default';
        this.callParent();
    }
});