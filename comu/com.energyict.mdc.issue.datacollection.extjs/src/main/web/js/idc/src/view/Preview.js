Ext.define('Idc.view.Preview', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.form.field.FilterDisplay',
        'Isu.view.issues.ActionMenu',
        'Isu.privileges.Issue',
        'Mdc.privileges.Device'
    ],
    alias: 'widget.data-collection-issues-preview',
    layout: 'column',
    defaults: {
        xtype: 'container',
        layout: 'form',
        columnWidth: 0.5
    },
    frame: true,
    router: null,

    initComponent: function () {
        var me = this;

        me.tools = [
            {
                xtype: 'button',
                itemId: 'data-collection-issues-preview-actions-button',
                text: Uni.I18n.translate('general.actions', 'IDC', 'Actions'),
                privileges: Ext.Array.merge(Isu.privileges.Issue.adminDevice, Mdc.privileges.Device.viewDeviceCommunication),
                iconCls: 'x-uni-action-iconD',
                menu: {
                    xtype: 'issues-action-menu',
                    itemId: 'issues-overview-action-menu',
                    router: me.router
                },
                listeners: {
                    click: function () {
                        this.showMenu();
                    }
                }
            }
        ];

        me.items = [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        xtype: 'filter-display',
                        itemId: 'data-collection-issue-preview-reason',
                        fieldLabel: Uni.I18n.translate('general.title.reason', 'IDC', 'Reason'),
                        name: 'reason',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : '';
                        }
                    },
                    {
                        itemId: 'data-collection-issue-preview-usage-point',
                        fieldLabel: Uni.I18n.translate('general.title.usagePoint', 'IDC', 'Usage point'),
                        name: 'usage_point'
                    },
                    {
                        xtype: 'filter-display',
                        itemId: 'data-collection-issue-preview-device',
                        fieldLabel: Uni.I18n.translate('general.title.device', 'IDC', 'Device'),
                        name: 'device',
                        renderer: function (value) {
                            var url = '',
                                result = '';

                            if (value) {
                                if (value.serialNumber && Mdc.privileges.Device.canViewDeviceCommunication()) {
                                    url = me.router.getRoute('devices/device').buildUrl({mRID: value.serialNumber});
                                    result = '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + ' ' + Ext.String.htmlEncode(value.serialNumber) + '</a>';
                                } else {
                                    result = value.name + ' ' + Ext.String.htmlEncode(value.serialNumber);
                                }
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
                        xtype: 'filter-display',
                        itemId: 'data-collection-issue-preview-status',
                        fieldLabel: Uni.I18n.translate('general.title.status', 'IDC', 'Status'),
                        name: 'status',
                        renderer: function (value) {
                            return value.name ? value.name : '';
                        }
                    },
                    {
                        itemId: 'data-collection-issue-preview-due-date',
                        fieldLabel: Uni.I18n.translate('general.title.dueDate', 'IDC', 'Due date'),
                        name: 'dueDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateShort(value) : '';
                        }
                    },
                    {
                        xtype: 'filter-display',
                        itemId: 'data-collection-issue-preview-assignee',
                        fieldLabel: Uni.I18n.translate('general.title.assignee', 'IDC', 'Assignee'),
                        name: 'assignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.none', 'IDC', 'None');
                        }
                    },
                    {
                        itemId: 'data-collection-issue-preview-creation-date',
                        fieldLabel: Uni.I18n.translate('general.title.creationDate', 'IDC', 'Creation date'),
                        name: 'creationDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});