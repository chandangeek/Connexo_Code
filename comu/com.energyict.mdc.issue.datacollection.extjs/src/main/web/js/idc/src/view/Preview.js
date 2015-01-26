Ext.define('Idc.view.Preview', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.form.field.FilterDisplay',
        'Isu.view.issues.ActionMenu'
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
    bbar: {
        layout: {
            type: 'vbox',
            align: 'right'
        },
        items: {
            text: Uni.I18n.translate('general.title.viewDetails', 'ISU', 'View details'),
            itemId: 'issue-view-details-link',
            ui: 'link',
            href: location.href
        }
    },

    initComponent: function () {
        var me = this;

        me.tools = [
            {
                xtype: 'button',
                itemId: 'data-collection-issues-preview-actions-button',
                text: Uni.I18n.translate('general.actions', 'ISU', 'Actions'),
                hidden:  !Uni.Auth.hasAnyPrivilege(['privilege.comment.issue','privilege.close.issue','privilege.assign.issue','privilege.action.issue',
                                                'privilege.view.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication']),
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
                        fieldLabel: Uni.I18n.translate('general.title.reason', 'ISU', 'Reason'),
                        name: 'reason',
                        renderer: function (value) {
                            return value.name ? value.name : '';
                        }
                    },
                    {
                        itemId: 'data-collection-issue-preview-usage-point',
                        fieldLabel: Uni.I18n.translate('general.title.usagePoint', 'ISU', 'Usage point'),
                        name: 'usage_point'
                    },
                    {
                        xtype: 'filter-display',
                        itemId: 'data-collection-issue-preview-device',
                        fieldLabel: Uni.I18n.translate('general.title.device', 'ISU', 'Device'),
                        name: 'device',
                        renderer: function (value) {
                            var url = '',
                                result = '';

                            if (value) {
                                if (value.serialNumber && Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceData','privilege.view.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication'])) {
                                    url = me.router.getRoute('devices/device').buildUrl({mRID: value.serialNumber});
                                    result = '<a href="' + url + '">' + value.name + ' ' + value.serialNumber + '</a>';
                                } else {
                                    result = value.name + ' ' + value.serialNumber;
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
                        fieldLabel: Uni.I18n.translate('general.title.status', 'ISU', 'Status'),
                        name: 'status',
                        renderer: function (value) {
                            return value.name ? value.name : '';
                        }
                    },
                    {
                        itemId: 'data-collection-issue-preview-due-date',
                        fieldLabel: Uni.I18n.translate('general.title.dueDate', 'ISU', 'Due date'),
                        name: 'dueDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateLong(value) : '';
                        }
                    },
                    {
                        xtype: 'filter-display',
                        itemId: 'data-collection-issue-preview-assignee',
                        fieldLabel: Uni.I18n.translate('general.title.assignee', 'ISU', 'Assignee'),
                        name: 'assignee',
                        renderer: function (value) {
                            return value.name ? value.name : Uni.I18n.translate('general.none', 'ISU', 'None');
                        }
                    },
                    {
                        itemId: 'data-collection-issue-preview-creation-date',
                        fieldLabel: Uni.I18n.translate('general.title.creationDate', 'ISU', 'Creation date'),
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