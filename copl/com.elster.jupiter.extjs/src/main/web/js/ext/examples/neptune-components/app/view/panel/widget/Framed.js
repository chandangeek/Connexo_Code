/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Neptune.view.panel.widget.Framed', {
    extend: 'Ext.panel.Panel',
    xtype: 'framedPanel',

    title: 'Framed Panel',
    frame: true,
    html: NeptuneAppData.dummyText
});