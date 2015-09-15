Ext.define('Mdc.view.setup.deviceattributes.DeviceAttributesEditForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceAttributesEditForm',

    requires: [
        'Mdc.view.setup.device.form.DeviceDateField',
        'Mdc.view.setup.deviceattributes.form.DateFieldEdited'
    ],

    defaults: {
        labelWidth: 150,
        xtype: 'displayfield',
        hidden: true,
        width: 420,
        renderer: function (value) {
            if (Ext.isEmpty(value.displayValue)) {
                return '-'
            } else {
                return Ext.String.htmlEncode(value.displayValue)
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
                fieldLabel: Uni.I18n.translate('general.deviceType', 'MDC', 'Device type')
            },
            {
                name: 'deviceConfiguration',
                itemId: 'deviceConfigurationView',
                fieldLabel: Uni.I18n.translate('general.deviceConfiguration', 'MDC', 'Device configuration')
            },
            {
                itemId: 'usagePointEmptyStoreField',
                fieldLabel: Uni.I18n.translate('general.usagePoint', 'MDC', 'Usage point'),
                renderer: function () {
                    return '<span style="font-style:italic;color: grey;">' + Uni.I18n.translate('deviceGeneralInformation.usagePoint.notAvailable', 'MDC', 'No usage points available') + '</span>';
                }
            },
            {
                name: 'usagePoint',
                itemId: 'usagePointView',
                fieldLabel: Uni.I18n.translate('general.usagePoint', 'MDC', 'Usage point')
            },
            {
                name: 'usagePointEdit',
                xtype: 'combobox',
                store: 'Mdc.store.UsagePointsForDeviceAttributes',
                itemId: 'usagePointEdit',
                fieldLabel: Uni.I18n.translate('general.usagePoint', 'MDC', 'Usage point'),
                valueField: 'id',
                displayField: 'mRID',
                queryMode: 'remote',
                queryParam: 'like',
                queryCaching: false,
                minChars: 0,
                triggerAction: 'last',
                forceSelection: true,
                listeners: {
                    change: {
                        fn: function (combo, newValue) {
                            if (!newValue) {
                                combo.reset();
                            }
                        }
                    },
                    expand: {
                        fn: function (combo) {
                            var picker = combo.getPicker(),
                                fn = function (view) {
                                    var store = view.getStore(),
                                        el = view.getEl().down('.' + Ext.baseCSSPrefix + 'list-plain');

                                    if (store.getTotalCount() > store.getCount()) {
                                        el.appendChild({
                                            tag: 'li',
                                            html: Uni.I18n.translate('issues.limitNotification', 'MDC', 'Keep typing to narrow down'),
                                            cls: Ext.baseCSSPrefix + 'boundlist-item combo-limit-notification'
                                        });
                                    }
                                };

                            picker.on('refresh', fn);
                            picker.on('beforehide', function () {
                                picker.un('refresh', fn);
                            }, combo, {single: true});
                        }
                    }
                }
            },
            {

                name: 'shipmentDate',
                itemId: 'shipmentDateView',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.shipmentDate', 'MDC', 'Shipment date'),
                renderer: function (value) {
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
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.installationDate', 'MDC', 'Installation date'),
                renderer: function (value) {
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
                itemId: 'deactivationDateView',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.deactivationDate', 'MDC', 'Deactivation date'),
                renderer: function (value) {
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
                itemId: 'decommissioningDateView',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.decommissionDate', 'MDC', 'Decommissioning date'),
                renderer: function (value) {
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
                xtype: 'combobox',
                name: 'yearOfCertificationEdit',
                itemId: 'yearOfCertificationEdit',
                fieldLabel: Uni.I18n.translate('deviceAdd.yearOfCertification', 'MDC', 'Year of certification'),
                displayField: 'year',
                valueField: 'year',
                store: undefined,
                editable: false,
                listConfig: { maxHeight: 100 },
                listeners: {
                    beforerender: function (combo) {
                        var currentTime = new Date();
                        var year = currentTime.getFullYear();
                        var years = [];

                        for (y = 0; y <= 20; y++) {
                            years.push([year - y]);
                        }

                        var yearStore = new Ext.data.SimpleStore
                        ({
                            fields: ['year'],
                            data: years
                        });

                        combo.bindStore(yearStore);
                        combo.setValue(year);
                    }
                }
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