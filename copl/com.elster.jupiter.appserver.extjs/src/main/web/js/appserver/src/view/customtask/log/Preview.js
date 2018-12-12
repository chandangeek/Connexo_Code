/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.customtask.log.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.ctk-log-preview',
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
                    fieldLabel: Uni.I18n.translate('general.name', 'APR', 'Name'),
                    name: 'name',
                    renderer: function (value) {
                        var url = me.router.getRoute('administration/taskmanagement/view').buildUrl({
                            taskId: me.taskId
                        });
                        return Dxp.privileges.DataExport.canView()
                            ? '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>'
                            : Ext.String.htmlEncode(value);
                    },
                    itemId: 'name-field'
                },
                {
                    fieldLabel: Uni.I18n.translate('log.runStartedOn', 'APR', 'Run started on'),
                    itemId: 'run-started-on'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.status', 'APR', 'Status'),
                    name: 'status'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.reason', 'APR', 'Reason'),
                    itemId: 'reason-field',
                    name: 'reason',
                    hidden: true
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('general.summary', 'APR', 'Summary'),
                    itemId: 'ctk-summary-container'
                }
            ]
        };
        me.callParent(arguments);
    },

    updateSummary: function (summary) {
        Ext.suspendLayouts();
        this.down('#ctk-summary-container').removeAll();
        var summaryLines = summary.split('\n');
        for (var i = 0; i < summaryLines.length; i++) {
            var summaryLine = summaryLines[i];
            this.down('#ctk-summary-container').add(
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
