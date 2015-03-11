Ext.define('Mdc.view.setup.communicationtask.CommunicationTaskPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.communicationTaskPreview',
    itemId: 'communicationTaskPreview',
    requires: [
        'Mdc.view.setup.communicationtask.CommunicationTaskActionMenu'
    ],
    frame: true,

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceType'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'communication-task-action-menu'
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
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200
                    },
                    items: [
                        {
                            fieldLabel: Uni.I18n.translate('communicationtasks.task.name', 'MDC', 'Communication task'),
                            name: 'comTask',
                            renderer: function (value) {
                                return value.name;
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('communicationtasks.task.securityset', 'MDC', 'Security set'),
                            name: 'securityPropertySet',
                            renderer: function (value) {
                                return value.name;
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('communicationtasks.task.partialConnectionTask', 'MDC', 'Connection method'),
                            name: 'partialConnectionTask',
                            renderer: function (value) {
                                return value ? value.name : '';
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('communicationtasks.task.protocolDialectConfigurationProperties', 'MDC', 'Protocol dialect'),
                            name: 'protocolDialectConfigurationProperties',
                            renderer: function (value) {
                                return value.name;
                            }
                        }
                    ]
                },
                {
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200
                    },
                    items: [
                        {
                            fieldLabel: Uni.I18n.translate('communicationtasks.task.status', 'MDC', 'Status'),
                            name: 'suspended',
                            renderer: function (value) {
                                if (value === true) {
                                    return Uni.I18n.translate('communicationtasks.task.inactive', 'MDC', 'Inactive');
                                }
                                return Uni.I18n.translate('communicationtasks.task.active', 'MDC', 'Active');
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('communicationtasks.task.priority', 'MDC', 'Urgency'),
                            name: 'priority'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('communicationtasks.task.ignoreNextExecutionSpecsForInbound', 'MDC', 'Always execute for inbound'),
                            name: 'ignoreNextExecutionSpecsForInbound',
                            renderer: function (value) {
                                if (value === true) {
                                    return Uni.I18n.translate('general.no', 'MDC', 'No');
                                }
                                return Uni.I18n.translate('general.yes', 'MDC', 'Yes');
                            }
                        }
                    ]
                }
            ]
        }
    ]

});


