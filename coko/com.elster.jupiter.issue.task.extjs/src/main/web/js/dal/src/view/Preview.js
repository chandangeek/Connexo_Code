/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.view.Preview', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.form.field.FilterDisplay',
        'Itk.privileges.Issue',
        'Uni.store.Apps',
        'Itk.view.ActionMenu'
    ],
    alias: 'widget.issue-preview',
    layout: 'column',
    defaults: {
        xtype: 'container',
        layout: 'form',
        columnWidth: 0.5
    },
    frame: true,
    router: null,
    fieldxtype: 'displayfield',
    showTools: true,

    initComponent: function () {
        var me = this;

        if (me.showTools) {
            me.tools = [
                {
                    xtype: 'uni-button-action',
                    itemId: 'issue-preview-actions-button',
                    privileges: Itk.privileges.Issue.adminDevice,
                    menu: {
                        xtype: 'issues-action-menu',
                        itemId: 'issue-overview-action-menu',
                        router: me.router
                    },
                    listeners: {
                        click: function () {
                            this.showMenu();
                        }
                    }
                }
            ];
        }

        me.items = [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        xtype: 'displayfield',
                        itemId: 'issue-id',
                        fieldLabel: Uni.I18n.translate('general.title.issueId', 'ITK', 'ID'),
                        name: 'issueId'
                    },
                    {
                        xtype: me.fieldxtype,
                        itemId: 'issue-reason',
                        fieldLabel: Uni.I18n.translate('general.title.reason', 'ITK', 'Reason'),
                        name: 'reason',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : '-';
                        }
                    },
                    {
                        itemId: 'issue-usage-point',
                        fieldLabel: Uni.I18n.translate('general.title.Usage point', 'ITK', 'Usage point'),
                        name: 'usagePointMRID',
                        renderer: function (value) {
                            var appName = 'Insight';
                            if (value && Itk.privileges.Issue.canViewUsagePoint()) {
                                if (Uni.store.Apps.checkApp(appName)) {
                                    if (Itk.privileges.Issue.canViewUsagePointInInsight()) {
                                        var url = Ext.String.format('{0}/usagepoints/{1}', Uni.store.Apps.getAppUrl(appName), encodeURIComponent(value));
                                        return Ext.String.format('<a href="{0}">{1}</a>', url, Ext.String.htmlEncode(value));
                                    }
                                } else if (Itk.privileges.Issue.canViewUsagePointByApp()) {
                                    var url = me.router.getRoute('usagepoints/usagepoint').buildUrl({usagePointId: value});
                                    return Ext.String.format('<a href="{0}">{1}</a>', url, Ext.String.htmlEncode(value));
                                }
                                else {
                                    return '-';
                                }
                            } else {
                                return '-';
                            }
                        }
                    },
                    {
                        xtype: me.fieldxtype,
                        itemId: 'issue-device',
                        fieldLabel: Uni.I18n.translate('general.title.device', 'ITK', 'Device'),
                        name: 'device',
                        renderer: function (value) {
                            var result = '';

                            if (value && value.name && Itk.privileges.Issue.canViewDeviceCommunication()) {
                                var url = me.router.getRoute('devices/device').buildUrl({deviceId: value.name});
                                result = '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>';
                            } else if (value) {
                                result = value.name;
                            }

                            return result;
                        }
                    },
                    {
                        itemId: 'issue-location',
                        fieldLabel: Uni.I18n.translate('general.title.location', 'ITK', 'Location'),
                        name: 'location',
                        renderer: function (value) {
                            if (!Ext.isEmpty(value)) {
                                return Ext.String.htmlEncode(value).replace(/(?:\\r\\n|\\r|\\n)/g, '<br>');
                            } else {
                                return '-'
                            }
                        }
                    },
                    {
                        itemId: 'issue-logbook',
                        fieldLabel: Uni.I18n.translate('general.title.logbook', 'ITK', 'Logbook'),
                        name: 'logBook',
                        renderer: function (value, metaData, record) {
                            var url = '',
                                result = '-';

                            if (value && Itk.privileges.Issue.canViewLogbook()) {
                                url = me.router.getRoute('devices/device/logbooks/logbookdata').buildUrl({
                                    deviceId: me.getRecord('device').get('deviceName'),
                                    logbookId: value.id
                                });
                                result = '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>';
                            } else if (value) {
                                result = value.name;
                            }
                            return result;
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
                        xtype: me.fieldxtype,
                        itemId: 'issue-status',
                        fieldLabel: Uni.I18n.translate('general.title.status', 'ITK', 'Status'),
                        name: 'status',
                        afterSubTpl: '<span id="issue-status-field-sub-tpl" class="field-additional-info" style="color: #686868;"></span>',
                        renderer: function (value, field) {
                            return value ? value.name : '-';
                        }
                    },
                    {
                        xtype: 'displayfield',
                        itemId: 'issue-due-date',
                        fieldLabel: Uni.I18n.translate('general.title.dueDate', 'ITK', 'Due date'),
                        name: 'dueDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateShort(value) : '-';
                        }
                    },
                    {
                        xtype: 'displayfield',
                        itemId: 'issue-priority',
                        fieldLabel: Uni.I18n.translate('general.title.priority', 'ITK', 'Priority'),
                        name: 'priority'
                    },
                    {
                        xtype: me.fieldxtype,
                        itemId: 'issue-workgroup',
                        fieldLabel: Uni.I18n.translate('general.title.workgroup', 'ITK', 'Workgroup'),
                        name: 'workGroupAssignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'ITK', 'Unassigned');
                        }
                    },
                    {
                        xtype: me.fieldxtype,
                        itemId: 'issue-user',
                        fieldLabel: Uni.I18n.translate('general.title.user', 'ITK', 'User'),
                        name: 'userAssignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'ITK', 'Unassigned');
                        }
                    },
                    {
                        itemId: 'issue-creation-date',
                        fieldLabel: Uni.I18n.translate('general.title.creationDate', 'ITK', 'Creation date'),
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