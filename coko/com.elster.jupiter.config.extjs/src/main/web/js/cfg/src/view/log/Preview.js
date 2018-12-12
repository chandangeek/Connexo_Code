/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.log.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.cfg-log-preview',
    router: null,
    initComponent: function () {
        var me = this;
        me.items = {
            xtype: 'form',
            itemId: 'log-preview-form',
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200,
                labelAlign: 'right'
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.name', 'CFG', 'Name'),
                    name: 'name',
                    renderer: function (value) {
                        var url = me.router.getRoute(me.detailLogRoute).buildUrl({taskId: me.taskId});
                        return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('validationTasks.log.runStartedOn', 'CFG', 'Run started on'),
                    itemId: 'run-started-on'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.status', 'CFG', 'Status'),
                    name: 'status'
                }
            ]
        };
        me.callParent(arguments);
    }
});
