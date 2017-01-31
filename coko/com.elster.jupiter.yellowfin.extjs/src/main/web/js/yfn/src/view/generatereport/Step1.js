/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Yfn.view.generatereport.Step1', {
    extend: 'Ext.panel.Panel',
    xtype: 'generatereport-wizard-step1',
    name: 'generateReportWizardStep1',
    ui: 'large',

    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.util.FormInfoMessage',
        'Ext.form.RadioGroup'

    ],

    title: Uni.I18n.translate('generatereport.wizard.step1.title', 'YFN', 'Step 1: Select report type'),

    items: [
        {
            itemId: 'step1-generatereport-errors',
            xtype: 'uni-form-error-message',
            hidden: true
        },
        {
            xtype: 'form',
            layout: 'column',
            itemId: 'step1-form'
        },
        {
            xtype: 'component',
            itemId: 'step1-generatereport-error-msg',
            hidden: true,
            html: Uni.I18n.translate('generatereport.noReportPrompts', 'YFN', 'Please select at least one report type.'),
            style: {
                'font': '13px/17px Lato',
                'color': '#eb5642',
                'margin-top': '20px'
            }
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});