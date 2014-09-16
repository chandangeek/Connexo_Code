Ext.define('Isu.view.workspace.issues.DataCollectionPreviewForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.form.field.FilterDisplay'
    ],
    alias: 'widget.datacollection-issue-form',
    layout: 'column',
    defaults: {
        xtype: 'container',
        layout: 'form',
        columnWidth: 0.5
    },
    ui: 'medium',
    showFilters: false,
    router: null,
    initComponent: function () {
        var me = this,
            displayFieldType;

        if (me.showFilters) {
            displayFieldType = 'filter-display';
        } else {
            displayFieldType = 'displayfield';
        }

        me.items = [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        xtype: displayFieldType,
                        itemId: 'reason',
                        fieldLabel: Uni.I18n.translate('general.title.reason', 'ISU', 'Reason'),
                        name: 'reason',
                        renderer: function (value) {
                            return value.name ? value.name : '';
                        }
                    },
                    {
                        itemId: '_customer',
                        fieldLabel: Uni.I18n.translate('general.title.customer', 'ISU', 'Customer'),
                        name: 'customer'
                    },
                    {
                        itemId: '_location',
                        fieldLabel: Uni.I18n.translate('general.title.location', 'ISU', 'Location'),
                        name: 'service_location'
                    },
                    {
                        itemId: '_usagepoint',
                        fieldLabel: Uni.I18n.translate('general.title.usagePoint', 'ISU', 'Usage point'),
                        name: 'usage_point'
                    },
                    {
                        xtype: displayFieldType,
                        itemId: 'device',
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
                        xtype: displayFieldType,
                        itemId: 'status',
                        fieldLabel: Uni.I18n.translate('general.title.status', 'ISU', 'Status'),
                        name: 'status',
                        renderer: function (value) {
                            return value.name ? value.name : '';
                        }
                    },
                    {
                        itemId: '_dueDate',
                        fieldLabel: Uni.I18n.translate('general.title.dueDate', 'ISU', 'Due date'),
                        name: 'dueDate',
                        renderer: Ext.util.Format.dateRenderer('M d, Y')
                    },
                    {
                        xtype: displayFieldType,
                        itemId: 'assignee',
                        fieldLabel: Uni.I18n.translate('general.title.assignee', 'ISU', 'Assignee'),
                        name: 'assignee',
                        renderer: function (value) {
                            return value.name ? value.name : Uni.I18n.translate('general.none', 'ISU', 'None');
                        }
                    },
                    {
                        itemId: '_creationDate',
                        fieldLabel: Uni.I18n.translate('general.title.creationDate', 'ISU', 'Creation date'),
                        name: 'creationDate',
                        renderer: Ext.util.Format.dateRenderer('M d, Y H:i')
                    },
                    {
                        itemId: '_serviceCat',
                        fieldLabel: Uni.I18n.translate('general.title.serviceCategory', 'ISU', 'Service category'),
                        name: 'service_category'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});