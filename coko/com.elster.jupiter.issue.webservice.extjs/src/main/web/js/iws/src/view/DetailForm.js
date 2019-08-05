/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Iws.view.DetailForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.webservice-isssue-detail-form',
    layout: 'column',
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
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        itemId: 'webservice-issue-id',
                        fieldLabel: Uni.I18n.translate('general.label.id', 'IWS', 'ID'),
                        name: 'issueId',
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                        itemId: 'webservice-issue-reason',
                        fieldLabel: Uni.I18n.translate('general.label.reason', 'IWS', 'Reason'),
                        name: 'reason',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : '-';
                        }
                    },
                    {
                        itemId: 'webservice-issue-type',
                        fieldLabel: Uni.I18n.translate('general.label.type', 'IWS', 'Type'),
                        name: 'issueType',
                        renderer: function (value) {
                            return value.name ? value.name : '-';
                        }
                    },
                    {
                        itemId: 'webservice-issue-endpoint',
                        fieldLabel: Uni.I18n.translate('general.label.endpoint', 'IWS', 'Web service endpoint'),
                        name: 'endpoint',
                        renderer: function (value) {
                            return value.name ? value.name : '-';
                        }
                    },
                    {
                        itemId: 'webservice-issue-ws',
                        fieldLabel: Uni.I18n.translate('general.label.webService', 'IWS', 'Web service'),
                        name: 'webservice',
                        renderer: function (value) {
                            return value.name ? value.name : '-';
                        }
                    }
                ]
            },
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        itemId: 'webservice-issue-status',
                        fieldLabel: Uni.I18n.translate('general.label.status', 'IWS', 'Status'),
                        name: 'status',
                        afterSubTpl: '<span id="issue-status-field-sub-tpl" class="field-additional-info" style="color: #686868;"></span>',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : '-';
                        }
                    },
                    {
                        itemId: 'webservice-issue-due-date',
                        fieldLabel: Uni.I18n.translate('general.label.dueDate', 'IWS', 'Due date'),
                        name: 'dueDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateLong(value) : '-';
                        }
                    },
                    {
                        itemId: 'webservice-issue-priority',
                        fieldLabel: Uni.I18n.translate('general.label.priority', 'IWS', 'Priority'),
                        name: 'priority',
                        renderer: function (value) {
                            return value ? Ext.String.htmlEncode(value) : '-';
                        }
                    },
                    {
                        itemId: 'webservice-issue-workgroup',
                        fieldLabel: Uni.I18n.translate('general.label.workgroup', 'IWS', 'Workgroup'),
                        name: 'workGroupAssignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'IDC', 'Unassigned');
                        }
                    },
                    {
                        itemId: 'webservice-issue-user',
                        fieldLabel: Uni.I18n.translate('general.label.user', 'IWS', 'User'),
                        name: 'userAssignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'IWS', 'Unassigned');
                        }
                    },
                    {
                        itemId: 'webservice-issue-creation-date',
                        fieldLabel: Uni.I18n.translate('general.label.creationDate', 'IWS', 'Creation date'),
                        name: 'creationDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(value) : '-';
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
