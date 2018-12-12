/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Yfn.view.generatereport.Step2', {
    extend: 'Ext.panel.Panel',
    xtype: 'generatereport-wizard-step2',
    name: 'generateReportWizardStep2',
    ui: 'large',

    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    requires: [
        'Uni.util.FormErrorMessage'
    ],


    title: Uni.I18n.translate('generatereport.wizard.step2.title', 'YFN', 'Step 2: Select report filter'),

    items: [
        {
            itemId: 'step2-generatereport-errors',
            xtype: 'uni-form-error-message',
            hidden: true,
            maxWidth: 540
        },
        {
            xtype: 'form',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            flex:1,
            itemId: 'filters-form',
            items:[
                {
                    xtype: 'displayfield',
                    itemId:'report-description',
                    fieldLabel: '',
                    labelCls:'x-panel-header-text-container-medium',
                    labelAlign: 'top',
                    value: ''
                },
                {
                    xtype: 'container',
                    itemId:'report-mandatory-filters-title',
                    layout: {
                        type: 'hbox',
                        align: 'left'
                    },
                    items: [
                        {
                            xtype: 'label',
                            itemId: 'report-mandatory-filters-label',
                            text: Uni.I18n.translate('generatereport.wizard.mandatoryFilters', 'YFN', 'Mandatory filters')
                        },
                        {
                            xtype: 'button',
                            tooltip: Uni.I18n.translate('generatereport.wizard.mandatoryFiltersTooltip',
                                'YFN',
                                'Reports allow you to look up a lot of data. A mandatory filter is a minimum required selection to help narrow down the results, so to avoid performance issues when generating the report.'),
                            iconCls: 'uni-icon-info-small',
                            ui: 'blank',
                            shadow: false,
                            margin: '5 0 0 5',
                            width: 16,
                            tabIndex: -1
                        }

                    ]
                },
                {
                    xtype: 'container',
                    itemId:'report-mandatory-filters',
                    layout: 'column',
                    hidden:true
                },
                {
                    xtype: 'container',
                    itemId:'report-optional-filters-title',
                    layout: {
                        type: 'hbox',
                        align: 'left'
                    },
                    items: [
                        {
                            xtype: 'label',
                            itemId: 'report-mandatory-filters-label',
                            text: Uni.I18n.translate('generatereport.wizard.optionalFilters', 'YFN', 'In report filters')
                        },
                        {
                            xtype: 'button',
                            tooltip:  Uni.I18n.translate('generatereport.wizard.optionalFiltersTooltip',
                                'YFN',
                                'Filtering on the data that is in the report data set'),
                            iconCls: 'uni-icon-info-small',
                            ui: 'blank',
                            shadow: false,
                            margin: '5 0 0 5',
                            width: 16,
                            tabIndex: -1
                        }
                    ]
                },
                {
                    xtype: 'container',
                    itemId:'report-optional-filters',
                    layout: 'column',
                    hidden:true
                }
            ]

        }
    ]


});
