/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Iws.view.WebServiceDetails', {
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
                xtype: 'fieldcontainer',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        itemId: 'webservice-issue-details-run-started',
                        fieldLabel: Uni.I18n.translate('general.webservice.runStarted', 'IWS', 'Run started on'),
                        name: 'occurrenceLink',
                        renderer: function (value) {
                            var result = '-';

                            if (value) {
                                if (value && Wss.privileges.Webservices.canView()) {
                                    url = me.router.getRoute('administration/webserviceendpoints/view').buildUrl({endpointId: value.ednpointId, occurenceId: value.occurenceId});
                                    result = '<a href="' + url + '">' + Ext.String.htmlEncode(Uni.DateTime.formatDateTimeLong(new Date(value.startTime))) + '</a>';
                                } else {
                                    result = Uni.DateTime.formatDateTimeLong(new Date(value.startTime));
                                }
                            }

                            return result;
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
