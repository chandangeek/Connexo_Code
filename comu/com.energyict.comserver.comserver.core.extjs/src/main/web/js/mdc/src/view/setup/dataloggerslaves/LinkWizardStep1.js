/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.dataloggerslaves.LinkWizardStep1', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.dataloggerslave-link-wizard-step1',
    ui: 'large',

    requires: [
        'Uni.util.FormErrorMessage',
        'Mdc.view.setup.dataloggerslaves.DataLoggerSlavesLinkWizardStep1',
        'Mdc.view.setup.dataloggerslaves.MultiElementSlavesLinkWizardStep1'
    ],
    purpose: undefined,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'mdc-dataloggerslave-link-wizard-step1-errors',
                xtype: 'uni-form-error-message',
                width: 570,
                hidden: true
            }];

        if (me.purpose.value === Mdc.util.LinkPurpose.LINK_MULTI_ELEMENT_SLAVE) {
            me.items.push({
                xtype: 'multi-element-slave-link-wizard-step1',
                itemId: 'mdc-multi-element-slave-link-wizard-step1'
            });
        }else{
            me.items.push({
                xtype: 'datalogger-slave-link-wizard-step1',
                itemId: 'mdc-datalogger-element-slave-link-wizard-step1'
            });
        }

        me.callParent(arguments);
    }
});