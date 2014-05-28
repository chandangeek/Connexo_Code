Ext.define('Mdc.view.setup.communicationtask.CommunicationTaskPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.communicationTaskPreview',
    itemId: 'communicationTaskPreview',
    frame: true,
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            iconCls: 'x-uni-action-iconA',
            menu: {
                xtype: 'menu',
                plain: true,
                border: false,
                shadow: false,
                items: [
                    {
                        text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                        action: 'editcommunicationtask'
                    },
                    {
                        text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
                        action: 'activatecommunicationtask'
                    },
                    {
                        text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                        action: 'removecommunicationtask'
                    }
                ]
            }
        }
    ],
    items: [
        {
            xtype: 'form',
            itemId: 'communicationTaskPreviewForm',
            layout: 'column',
            defaults: {
                xtype: 'container',
                layout: 'form',
                columnWidth: 0.5
            },
            items: [
                {
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: Uni.I18n.translate('communicationtasks.task.name', 'MDC', 'Communication task'),
                            name: 'comTask',
                            renderer: function (value) {
                                return value.name;
                            }
                        },
                        {
                            xtype: 'displayfield',
                            fieldLabel: Uni.I18n.translate('communicationtasks.task.securityset', 'MDC', 'Security set'),
                            name: 'securityPropertySet',
                            renderer: function (value) {
                                return value.name;
                            }
                        },
                        {
                            xtype: 'displayfield',
                            fieldLabel: Uni.I18n.translate('communicationtasks.task.partialConnectionTask', 'MDC', 'Connection method'),
                            name: 'partialConnectionTask',
                            renderer: function (value) {
                                return value.name;
                            }
                        },
                        {
                            xtype: 'displayfield',
                            fieldLabel: Uni.I18n.translate('communicationtasks.task.protocolDialectConfigurationProperties', 'MDC', 'Protocol dialect'),
                            name: 'protocolDialectConfigurationProperties',
                            renderer: function (value) {
                                return value.name;
                            }
                        }
                    ]
                },
                {
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: Uni.I18n.translate('communicationtasks.task.priority', 'MDC', 'Urgency'),
                            name: 'priority'
                        },
                        {
                            xtype: 'displayfield',
                            fieldLabel: Uni.I18n.translate('communicationtasks.task.ignoreNextExecutionSpecsForInbound', 'MDC', 'Always execute for inbound'),
                            name: 'ignoreNextExecutionSpecsForInbound',
                            renderer: function (value) {
                                if (value === true) {
                                    return Uni.I18n.translate('general.no', 'MDC', 'No');
                                }
                                return Uni.I18n.translate('general.yes', 'MDC', 'Yes');
                            }
                        },
                        {
                            xtype: 'displayfield',
                            fieldLabel: Uni.I18n.translate('communicationtasks.task.isactive', 'MDC', 'Is active'),
                            name: 'suspended',
                            renderer: function (value) {
                                if (value === true) {
                                    return Uni.I18n.translate('general.no', 'MDC', 'No');
                                }
                                return Uni.I18n.translate('general.yes', 'MDC', 'Yes');
                            }
                        },
                        {
                            xtype: 'displayfield',
                            fieldLabel: Uni.I18n.translate('communicationtasks.task.nextExecutionSpecs', 'MDC', 'Schedule'),
                            name: 'nextExecutionSpecsName'
                        }
                    ]
                }
            ]
        }
    ]

});


