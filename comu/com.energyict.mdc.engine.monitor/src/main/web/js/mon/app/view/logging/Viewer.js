/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.view.logging.Viewer', {
    extend: 'Ext.panel.Panel',
    xtype: 'logViewer',
    border: true,
    cls: ['logviewer', 'logrecord-date', 'logmessage', 'logmessage-error'],
    tpl: '{data}',
    tplWriteMode: 'overwrite',
    autoScroll: true,
    bodyStyle: 'padding: 5px;'
});
