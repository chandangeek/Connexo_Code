/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.ruledeviceconfiguration.RuleDeviceConfigurationAdd', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.rule-device-configuration-add',
    ruleSetId: null,
    store: undefined,
    currentRoute: undefined,
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.view.container.PreviewContainer',
        'Mdc.view.setup.ruledeviceconfiguration.RuleDeviceConfigurationAddGrid'
    ],
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('validation.deviceConfiguration.addMultiple', 'MDC', 'Add device configurations'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'rule-device-configuration-add-grid',
                            itemId: 'rule-device-configuration-add-grid-table',
                            ruleSetId: me.ruleSetId,
                            store: me.store,
                            currentRoute: me.currentRoute
                        },
                        emptyComponent: {
                            xtype: 'container',
                            items: [
                                {
                                    xtype: 'no-items-found-panel',
                                    title: Uni.I18n.translate('validation.empty.deviceConfiguration.title', 'MDC', 'No device configurations found'),
                                    reasons: [
                                        Uni.I18n.translate('deviceConfiguration.empty.list.item1', 'MDC', 'No device configurations have been added yet.'),
                                        Uni.I18n.translate('deviceConfiguration.empty.list.item2', 'MDC', 'There are no device configurations that have reading types that match the rules in the validation rule set.'),
                                        Uni.I18n.translate('deviceConfiguration.empty.list.item3', 'MDC', 'Matching device configurations exist, but you do not have permission to view them.'),
                                        Uni.I18n.translate('deviceConfiguration.empty.list.item4', 'MDC', 'All matching device configurations have already been added to the validation rule set.')
                                    ]
                                },
                                {
                                    xtype:'button',
                                    ui: 'link',
                                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                    href: me.currentRoute
                                }
                            ]
                        },
                        onLoad: function () {
                            this.getLayout().setActiveItem(this.grid.store.getCount() === 0 ? 0 : 1);
                        }
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});