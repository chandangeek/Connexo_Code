/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicegroup.Step4', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.device-group-wizard-step4',
    ui: 'large',

    bbar: [
        {
            xtype: 'progressbar',
            itemId: 'device-group-wizard-step4-progressbar',
            width: '50%'
        }
    ]
});