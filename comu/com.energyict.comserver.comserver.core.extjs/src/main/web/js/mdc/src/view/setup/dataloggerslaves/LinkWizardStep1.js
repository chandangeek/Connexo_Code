/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.dataloggerslaves.LinkWizardStep1', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.dataloggerslave-link-wizard-step1',
    ui: 'large',

    requires: [
        'Uni.util.FormErrorMessage'
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'mdc-dataloggerslave-link-wizard-step1-errors',
                xtype: 'uni-form-error-message',
                width: 570,
                hidden: true
            },
            {
                xtype: 'container',
                itemId: 'mdc-dataloggerslave-link-wizard-step1-container',
                fieldLabel: '',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'radiogroup',
                        itemId: 'mdc-step1-radiogroup',
                        columns: 1,
                        fieldLabel: '',
                        vertical: true,
                        defaults: {
                            margin: '0 15 0 0'
                        },
                        items: [
                            {
                                itemId: 'mdc-existing-slave-option',
                                boxLabel: Uni.I18n.translate('linkwizard.step1.option.existing', 'MDC', 'Existing data logger slave'),
                                name: 'useExisting',
                                inputValue: true,
                                checked: true,
                                margin: '20 15 0 0'
                            },
                            {
                                itemId: 'mdc-new-slave-option',
                                boxLabel: Uni.I18n.translate('linkwizard.step1.option.new', 'MDC', 'New data logger slave'),
                                name: 'useExisting',
                                inputValue: false
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        items: [
                            {
                                xtype: 'combobox',
                                width: 386,
                                itemId: 'mdc-step1-slave-combo',
                                store: 'Mdc.store.AvailableDataLoggerSlaves',
                                forceSelection: true,
                                displayField: 'name',
                                valueField: 'name',
                                emptyText: Uni.I18n.translate('general.selectADataLoggerSlave', 'MDC', 'Start typing to select a data logger slave...'),
                                msgTarget: 'under',
                                margin: '20 0 0 0',
                                queryMode: 'remote',
                                queryParam: 'like',
                                queryDelay: 500,
                                queryCaching: false,
                                minChars: 1,
                                editable: true,
                                typeAhead: true
                            },
                            {
                                xtype: 'displayfield',
                                renderer: function() {
                                    return '';
                                }
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});