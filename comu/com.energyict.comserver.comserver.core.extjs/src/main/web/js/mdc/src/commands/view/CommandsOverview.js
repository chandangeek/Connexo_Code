/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.commands.view.CommandsOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.commands-overview',

    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.commands.view.CommandsGrid',
        'Mdc.commands.view.CommandPreview',
        'Mdc.commands.view.CommandFilter',
        'Mdc.commands.store.Commands'
    ],

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('general.commands', 'MDC', 'Commands'),
                items: [
                    {
                        xtype: 'commands-overview-filter',
                        itemId: 'mdc-commands-overview-filter',
                        store: 'Mdc.commands.store.Commands'
                    },
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'commands-grid',
                            itemId: 'mdc-commands-grid'
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'mdc-commands-empty-grid',
                            title: Uni.I18n.translate('commands.noItems', 'MDC', 'No commands found'),
                            reasons: [
                                Uni.I18n.translate('commands.empty.list.reason1', 'MDC', 'No commands have been defined yet.'),
                                Uni.I18n.translate('commands.empty.list.reason2', 'MDC', 'No commands comply with the filter.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('general.addCommand', 'MDC', 'Add command'),
                                    itemId: 'mdc-empty-commands-grid-add-button',
                                    privileges: ['privilege.administrate.device', 'execute.device.message.level1','execute.device.message.level2',
                                        'execute.device.message.level3','execute.device.message.level4']
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'command-preview',
                            itemId: 'mdc-command-preview'
                        }
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});
