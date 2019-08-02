/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Iws.view.ServiceCallDetails', {
    extend: 'Ext.form.Panel',
    alias: 'widget.webservice-details-form',
    defaults: {
        xtype: 'container',
        layout: 'form',
        columnWidth: 0.5
    },
    router: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                items: [
                    {
                        itemId: 'webservice-issue-details-run-started',
                        fieldLabel: Uni.I18n.translate('general.webservice.runStarted', 'IWS', 'Run started on'),
                        name: 'serviceCallType',
                        renderer: function (value) {
                            return value ? value.name : '-';
                        }
                    },
                    {
                        itemId: 'webservice-issue-details-status',
                        fieldLabel: Uni.I18n.translate('general.webservice.status', 'IWS', 'Status'),
                        name: 'status',
                        renderer: function (value) {
                            return value ? value.name : '-';
                        }
                    },
                ]
            }
        ];

        me.callParent(arguments);
    }
});
