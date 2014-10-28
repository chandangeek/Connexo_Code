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
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'ISU', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'issues-action-menu',
                itemId: 'issues-overview-action-menu'
            },
            listeners: {
                click: function () {
                    this.showMenu();
                }
            }
        }
    ],
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
                        itemId: 'data-collection-issue-preview-customer',
                        fieldLabel: Uni.I18n.translate('general.title.customer', 'ISU', 'Customer'),
                        name: 'customer'
                    },
                    {
                        itemId: 'data-collection-issue-preview-service-location',
                        fieldLabel: Uni.I18n.translate('general.title.location', 'ISU', 'Location'),
                        name: 'service_location'
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
                                if (value.serialNumber) {
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
                        renderer: Ext.util.Format.dateRenderer('M d, Y')
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
                        renderer: Ext.util.Format.dateRenderer('M d, Y H:i')
                    },
                    {
                        itemId: 'data-collection-issue-preview-service-category',
                        fieldLabel: Uni.I18n.translate('general.title.serviceCategory', 'ISU', 'Service category'),
                        name: 'service_category'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});