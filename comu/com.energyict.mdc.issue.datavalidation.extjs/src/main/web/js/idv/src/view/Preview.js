Ext.define('Idv.view.Preview', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.form.field.FilterDisplay',
        'Isu.view.issues.ActionMenu',
        'Isu.privileges.Issue',
        'Mdc.privileges.Device'
    ],
    alias: 'widget.data-validation-issues-preview',
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
                itemId: 'data-validation-issues-preview-actions-button',
                text: Uni.I18n.translate('general.actions', 'ISU', 'Actions'),
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
                    //{
                    //    xtype: 'filter-display',
                    //    itemId: 'data-validation-issue-preview-reason',
                    //    fieldLabel: Uni.I18n.translate('general.title.reason', 'ISU', 'Reason'),
                    //    name: 'reason',
                    //    renderer: function (value) {
                    //        return value.name ? value.name : '';
                    //    }
                    //},
                    {
                        itemId: 'data-validation-issue-preview-usage-point',
                        fieldLabel: Uni.I18n.translate('general.title.usagePoint', 'ISU', 'Usage point'),
                        name: 'usage_point'
                    },
                    {
                        xtype: 'filter-display',
                        itemId: 'data-validation-issue-preview-device',
                        fieldLabel: Uni.I18n.translate('general.title.device', 'ISU', 'Device'),
                        name: 'device',
                        renderer: function (value) {
                            var url = '',
                                result = '';

                            if (value) {
                                if (value.serialNumber && Mdc.privileges.Device.canViewDeviceCommunication()) {
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
                        itemId: 'data-validation-issue-preview-status',
                        fieldLabel: Uni.I18n.translate('general.title.status', 'ISU', 'Status'),
                        name: 'status',
                        renderer: function (value) {
                            return value.name ? value.name : '';
                        }
                    },
                    {
                        itemId: 'data-validation-issue-preview-due-date',
                        fieldLabel: Uni.I18n.translate('general.title.dueDate', 'ISU', 'Due date'),
                        name: 'dueDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateShort(value) : '';
                        }
                    },
                    {
                        itemId: 'data-validation-issue-preview-modification-date',
                        fieldLabel: Uni.I18n.translate('general.title.modificationDate', 'ISU', 'Modification date'),
                        name: 'modTime',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateShort(value) : '';
                        }
                    },
                    {
                        xtype: 'filter-display',
                        itemId: 'data-validation-issue-preview-assignee',
                        fieldLabel: Uni.I18n.translate('general.title.assignee', 'ISU', 'Assignee'),
                        name: 'assignee',
                        renderer: function (value) {
                            return value.name ? value.name : Uni.I18n.translate('general.none', 'ISU', 'None');
                        }
                    },
                    {
                        itemId: 'data-validation-issue-preview-creation-date',
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