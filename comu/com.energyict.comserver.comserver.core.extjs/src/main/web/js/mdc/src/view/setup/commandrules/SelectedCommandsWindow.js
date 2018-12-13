/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.commandrules.SelectedCommandsWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.AddCommandsToRuleSelectedCommands',
    title: Uni.I18n.translatePlural('general.nrOfCommandsSelected', 0, 'MDC', 'No commands selected', '{0} command selected', '{0} commands selected'),
    closable: true,
    requires: [
        'Mdc.store.SelectedCommands'
    ],
    width: 800,
    height: 425,
    autoShow: true,
    modal: true,
    layout: 'fit',
    closeAction: 'destroy',
    floating: true,
    items: [
        {
            xtype: 'grid',
            margin: '10 0 0 0',
            store: 'Mdc.store.SelectedCommands',
            columns: [
                {
                    header: Uni.I18n.translate('general.category', 'MDC', 'Category'),
                    dataIndex: 'category',
                    flex: 1
                },
                {
                    header: Uni.I18n.translate('general.command', 'MDC', 'Command'),
                    dataIndex: 'command',
                    flex: 1
                }
            ]
        }
    ]
});