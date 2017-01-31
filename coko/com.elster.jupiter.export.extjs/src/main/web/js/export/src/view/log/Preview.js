/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.log.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.dxp-log-preview',
    itemId: 'des-log-preview',
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
                    fieldLabel: Uni.I18n.translate('general.name', 'DES', 'Name'),
                    name: 'name',
                    renderer: function (value) {
                        var url = me.router.getRoute('administration/dataexporttasks/dataexporttask').buildUrl();
                        return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('log.runStartedOn', 'DES', 'Run started on'),
                    itemId: 'run-started-on'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.status', 'DES', 'Status'),
                    name: 'status'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.reason', 'DES', 'Reason'),
                    itemId: 'reason-field',
                    name: 'reason',
                    hidden: true
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('general.summary', 'DES', 'Summary'),
                    itemId: 'des-summary-container'
                }
            ]
        };
        me.callParent(arguments);
    },

    updateSummary: function (summary) {
        Ext.suspendLayouts();
        this.down('#des-summary-container').removeAll();
        var summaryLines = summary.split('\n');
        for (var i = 0; i < summaryLines.length; i++) {
            var summaryLine = summaryLines[i];
            this.down('#des-summary-container').add(
                {
                    xtype: 'displayfield',
                    fieldLabel: undefined,
                    value: summaryLine,
                    padding: '0 0 0 0',
                    margin: '0 0 0 0'
                }
            );
        }
        Ext.resumeLayouts(true);
    }

});
