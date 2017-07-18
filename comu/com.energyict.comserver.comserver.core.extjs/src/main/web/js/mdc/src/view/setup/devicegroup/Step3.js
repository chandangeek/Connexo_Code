/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicegroup.Step3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.device-group-wizard-step3',
    ui: 'large',

    bbar: [
        {
            xtype: 'progressbar',
            itemId: 'device-group-wizard-step3-progressbar',
            width: '50%'
        }
    ]
});