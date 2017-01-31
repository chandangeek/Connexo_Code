/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.view.Step4', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.usagepoint-group-wizard-step4',
    xtype: 'usagepoint-group-wizard-step4',
    ui: 'large',
    bbar: [
        {
            xtype: 'progressbar',
            itemId: 'usagepoint-group-wizard-step4-progressbar',
            width: '50%'
        }
    ]
});