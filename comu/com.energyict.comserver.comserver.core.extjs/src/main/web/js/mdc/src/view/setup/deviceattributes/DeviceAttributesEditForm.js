Ext.define('Mdc.view.setup.deviceattributes.DeviceAttributesEditForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceAttributesEditForm',

    requires: [
        'Mdc.view.setup.device.form.DeviceDateField',
        'Mdc.view.setup.deviceattributes.form.DateFieldEdited'
    ],

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    defaults: {
        labelWidth: 150,
        xtype: 'displayfield',
        hidden: true,
        renderer: function (value) {
            if (Ext.isEmpty(value.displayValue)) {
                return '-'
            } else {
                return value.displayValue
            }
        }
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                name: 'mrid',
                itemId: 'mridView',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.mrid', 'MDC', 'MRID')
            },
            {
                name: 'lifeCycleState',
                itemId: 'lifeCycleStateView',
                fieldLabel: Uni.I18n.translate('general.state', 'MDC', 'State')
            },
            {
                name: 'serialNumber',
                itemId: 'serialNumberView',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.serialNumber', 'MDC', 'Serial number')
            },
            {
                name: 'serialNumberEdit',
                itemId: 'serialNumberEdit',
                xtype: 'textfield',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.serialNumber', 'MDC', 'Serial number')
            },
            {
                name: 'deviceType',
                itemId: 'deviceTypeView',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.deviceType', 'MDC', 'Device type')
            },
            {
                name: 'deviceConfiguration',
                itemId: 'deviceConfigurationView',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.deviceConfiguration', 'MDC', 'Device configuration')
            },
            {
                itemId: 'usagePointEmptyStoreField',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.usagePoint', 'MDC', 'Usage point'),
                renderer: function() {
                    return '<span style="font-style:italic;color: grey;">' + Uni.I18n.translate('deviceGeneralInformation.usagePoint.notAvailable', 'MDC', 'No usage points available') + '</span>';
                }
            },
            {
                name: 'usagePoint',
                itemId: 'usagePointView',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.usagePoint', 'MDC', 'Usage point')
            },
            {
                name: 'usagePointEdit',
                xtype: 'combobox',
                store: 'Mdc.store.UsagePointsForDeviceAttributes',
                itemId: 'usagePointEdit',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.usagePoint', 'MDC', 'Usage point'),
                valueField: 'id',
                displayField: 'mRID'
            },
            {

                name: 'shipmentDate',
                required: true,
                itemId: 'shipmentDateView',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.shipmentDate', 'MDC', 'Shipment date'),
                renderer: function(value)  {
                    if (Ext.isEmpty(value.displayValue)) {
                        return '-'
                    } else {
                        return Uni.DateTime.formatDateLong(new Date(value.displayValue));
                    }
                }
            },
            {
                xtype: 'deviceDateFieldEdited',
                name: 'shipmentDateEdit',
                itemId: 'shipmentDateEdit',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.shipmentDate', 'MDC', 'Shipment date')
            },
            {
                name: 'installationDate',
                itemId: 'installationDateView',
                required: true,
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.installationDate', 'MDC', 'Installation date'),
                renderer: function(value)  {
                    if (Ext.isEmpty(value.displayValue)) {
                        return '-'
                    } else {
                        return Uni.DateTime.formatDateLong(new Date(value.displayValue));
                    }
                }
            },
            {
                xtype: 'deviceDateFieldEdited',
                itemId: 'installationDateEdit',
                name: 'installationDateEdit',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.installationDate', 'MDC', 'Installation date')
            },
            {
                name: 'deactivationDate',
                required: true,
                itemId: 'deactivationDateView',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.deactivationDate', 'MDC', 'Deactivation date'),
                renderer: function(value)  {
                    if (Ext.isEmpty(value.displayValue)) {
                        return '-'
                    } else {
                        return Uni.DateTime.formatDateLong(new Date(value.displayValue));
                    }
                }
            },
            {
                xtype: 'deviceDateFieldEdited',
                itemId: 'deactivationDateEdit',
                name: 'deactivationDate',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.deactivationDate', 'MDC', 'Deactivation date')
            },
            {

                name: 'decommissioningDate',
                required: true,
                itemId: 'decommissioningDateView',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.decommissionDate', 'MDC', 'Decommissioning date'),
                renderer: function(value)  {
                    if (Ext.isEmpty(value.displayValue)) {
                        return '-'
                    } else {
                        return Uni.DateTime.formatDateLong(new Date(value.displayValue));
                    }
                }
            },
            {
                xtype: 'deviceDateFieldEdited',
                name: 'decommissioningDateEdit',
                itemId: 'decommissioningDateEdit',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.decommissionDate', 'MDC', 'Decommissioning date')
            },
            {
                name: 'yearOfCertification',
                itemId: 'yearOfCertificationView',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.yearOfCertification', 'MDC', 'Year of certification')
            },
            {
                name: 'yearOfCertificationEdit',
                itemId: 'yearOfCertificationEdit',
                xtype: 'combobox',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.yearOfCertification', 'MDC', 'Year of certification'),
                editable: false,
                queryMode: 'local',
                displayField: 'year',
                valueField: 'year',
                store: Ext.create('Ext.data.Store', {
                    fields: ['year'],
                    data : [
                        {"year":"2010"},
                        {"year":"2011"},
                        {"year":"2012"},
                        {"year":"2013"},
                        {"year":"2014"},
                        {"year":"2015"},
                        {"year":"2016"},
                        {"year":"2017"},
                        {"year":"2018"},
                        {"year":"2019"},
                        {"year":"2020"},
                        {"year":"2021"},
                        {"year":"2022"},
                        {"year":"2023"},
                        {"year":"2024"},
                        {"year":"2025"},
                        {"year":"2026"},
                        {"year":"2027"},
                        {"year":"2028"},
                        {"year":"2029"},
                        {"year":"2030"}
                    ]
                })
            },
            {
                name: 'batch',
                itemId: 'batchView',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.batch', 'MDC', 'Batch')
            },
            {
                name: 'batchEdit',
                xtype: 'textfield',
                itemId: 'batchEdit',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.batch', 'MDC', 'Batch')
            }
        ];

        me.callParent(arguments);
    }
});