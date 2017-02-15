/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.view.Step3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.usagepoint-group-wizard-step3',
    xtype: 'usagepoint-group-wizard-step3',
    ui: 'large',
    bbar: [
        {
            xtype: 'progressbar',
            itemId: 'usagepoint-group-wizard-step3-progressbar',
            width: '50%'
        }
    ]
});