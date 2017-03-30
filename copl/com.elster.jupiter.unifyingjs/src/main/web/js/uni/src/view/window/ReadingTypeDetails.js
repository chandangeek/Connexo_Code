/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.window.ReadingTypeDetails', {
    extend: 'Ext.window.Window',
    xtype: 'readingTypeDetails',
    closable: true,
    width: 800,
    height: 550,
    constrain: true,
    autoShow: true,
    modal: true,
    layout: 'fit',
    closeAction: 'destroy',
    floating: true,
    items: {}
});