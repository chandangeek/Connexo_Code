/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isc.view.ServiceCallDetails', {
    extend: 'Ext.form.Panel',
    alias: 'widget.servicecall-details-form',
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
                fieldLabel: Uni.I18n.translate('general.slaveDetails', 'ISC', 'Slave details'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        itemId: 'servicecall-issue-details-link',
                        fieldLabel: Uni.I18n.translate('issue.workspace.servicecall', 'ISC', 'Service call'),
                        name: 'serviceCall',
                        renderer: function (value) {
                            var url,
                                result = '-';

                            if (value) {
                                if (value.name && Scs.privileges.ServiceCall.canView()) {
                                    url = me.router.getRoute('workspace/servicecalls/overview').buildUrl({serviceCallId: value.id});
                                    result = '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>';
                                } else {
                                    result = value.name;
                                }
                            }

                            return result;
                        }
                    },
                    {
                        itemId: 'servicecall-issue-details-parent',
                        fieldLabel: Uni.I18n.translate('general.servicecall.parent', 'ISC', 'Parent service call'),
                        name: 'parentServiceCall',
                        renderer: function (value) {
                            var url,
                                result = '-';

                            if (value) {
                                if (value.name && Scs.privileges.ServiceCall.canView()) {
                                    url = me.router.getRoute('workspace/servicecalls/overview').buildUrl({serviceCallId: value.id});
                                    result = '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>';
                                } else {
                                    result = value.name;
                                }
                            }

                            return result;
                        }
                    },
                    {
                        itemId: 'servicecall-issue-details-type',
                        fieldLabel: Uni.I18n.translate('general.servicecall.type', 'ISC', 'Service call type'),
                        name: 'serviceCallType',
                        renderer: function (value) {
                            return value ? value.name : '-';
                        }
                    },
                    {
                        itemId: 'servicecall-issue-details-status',
                        fieldLabel: Uni.I18n.translate('general.servicecall.status', 'ISC', 'Service call status'),
                        name: 'onState',
                        renderer: function (value) {
                            return value ? value.name : '-';
                        }
                    },
                    {
                        itemId: 'servicecall-issue-details-received-date',
                        fieldLabel: Uni.I18n.translate('general.servicecall.received.date', 'ISC', 'Received date'),
                        name: 'receivedTime',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-';
                        }
                    },
                    {
                        itemId: 'servicecall-issue-details-modification-date',
                        fieldLabel: Uni.I18n.translate('general.servicecall.modification.date', 'ISC', 'Modification date'),
                        name: 'lastModifyTime',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-';
                        }
                    }
                ]
            },
        ];

        me.callParent(arguments);
    }
});
