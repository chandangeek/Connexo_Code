/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.log.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.dxp-log-preview',
    itemId: 'des-log-preview',
    router: null,
    logLevelsStore : Ext.data.StoreManager.lookup('LogLevelsStore'),
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
                    fieldLabel: Uni.I18n.translate('general.exportTask', 'DES', 'Export task'),
                    name: 'name',
                    renderer: function (value) {
                        var url = me.router.getRoute('administration/dataexporttasks/dataexporttask').buildUrl({
                            taskId: me.taskId
                        });
                        return Dxp.privileges.DataExport.canView()
                            ? '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>'
                            : Ext.String.htmlEncode(value);
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
                    fieldLabel: Uni.I18n.translate('general.logLevel', 'DES', 'Log level'),
                    name: 'logLevel',
                    renderer: function (value) {
                        var displayValue = value;
                        if (value) {
                            var record = me.logLevelsStore.findRecord("id", value);
                            if(record != null) {
                                displayValue = record && record.get("displayValue");
                            }
                        }
                        return displayValue;
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.reason', 'DES', 'Reason'),
                    itemId: 'reason-field',
                    name: 'reason',
                    hidden: true
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('general.dataSelectorSummary', 'DES', 'Data selector summary'),
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
