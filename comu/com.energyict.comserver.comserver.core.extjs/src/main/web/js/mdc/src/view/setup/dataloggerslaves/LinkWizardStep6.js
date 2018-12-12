/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.dataloggerslaves.LinkWizardStep6', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.dataloggerslave-link-wizard-step6',
    ui: 'large',

    bbar: [
        {
            xtype: 'progressbar',
            itemId: 'mdc-dataloggerslave-link-wizard-step6-progressbar',
            width: '50%'
        }
    ]

});