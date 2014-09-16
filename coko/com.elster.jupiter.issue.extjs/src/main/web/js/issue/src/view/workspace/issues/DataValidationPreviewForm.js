Ext.define('Isu.view.workspace.issues.DataValidationPreviewForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.form.field.FilterDisplay'
    ],
    alias: 'widget.datavalidation-issue-form',
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
                xtype: 'container',
                layout: 'column',
                defaults: {
                    xtype: 'container',
                    layout: 'form',
                    columnWidth: 0.5
                },
                items: [
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
                ]
            },
            {
                xtype: 'container',
                layout: 'form',
                margin: '20 0 0 0',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        fieldLabel: '&nbsp;',
                        value: '<b>' + Uni.I18n.translate('workspace.datavalidation.overview.title', 'ISU', 'Data validation') + '</b>'
                    },
                    {
                        itemId: '_validationRule',
                        fieldLabel: Uni.I18n.translate('general.title.validationRule', 'ISU', 'Validation rule'),
                        name: 'validationRule'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});