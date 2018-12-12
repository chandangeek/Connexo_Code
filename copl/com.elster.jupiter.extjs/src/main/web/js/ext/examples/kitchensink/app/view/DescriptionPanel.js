/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('KitchenSink.view.DescriptionPanel', {
    extend: 'Ext.panel.Panel',
    xtype: 'descriptionPanel',
    id: 'description-panel',
    title: 'Description',
    autoScroll: true,
    rtl: false,

    initComponent: function() {
        this.ui = (Ext.themeName === 'neptune') ? 'light' : 'default';
        this.callParent();
    }
});