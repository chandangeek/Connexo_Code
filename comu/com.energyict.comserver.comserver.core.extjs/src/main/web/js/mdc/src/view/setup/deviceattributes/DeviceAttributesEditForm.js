Ext.define('Mdc.view.setup.deviceattributes.DeviceAttributesEditForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceAttributesEditForm',

    requires: [
        'Mdc.view.setup.device.form.DeviceDateField',
        'Mdc.view.setup.deviceattributes.form.DateFieldEdited',
        'Uni.form.field.Coordinates',
        'Uni.form.field.Location'
    ],

    defaults: {
        labelWidth: 150,
        xtype: 'displayfield',
        hidden: true,
        width: 450,
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
                name: 'mrid',
                itemId: 'mridEdit',
                xtype: 'textfield',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.mrid', 'MDC', 'MRID')
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
                xtype: 'location',
                name: 'locationEdit',
                itemId: 'locationEdit',
                width: 490,
                displayResetButton: true,
                findLocationsUrl: '/api/jsr/search/com.energyict.mdc.device.data.Device/locationsearchcriteria/location',
                locationDetailsUrl: '/api/ddr/devices/locations'
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
                name: 'batch',
                itemId: 'batchView',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.batch', 'MDC', 'Batch')
            },
            {
                name: 'batchEdit',
                xtype: 'textfield',
                itemId: 'batchEdit',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.batch', 'MDC', 'Batch')
            },
            {
                xtype: 'coordinates',
                name: 'geoCoordinatesEdit',
                itemId: 'geoCoordinatesEdit',
                width: 490,
                displayResetButton: true,
                fieldLabel: Uni.I18n.translate('general.coordinates', 'MDC', 'Coordinates')
            },
            {
                xtype: 'numberfield',
                name: 'multiplierEdit',
                minValue: 1,
                maxValue: 2147483647,
                itemId: 'multiplierEdit',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.multiplier', 'MDC', 'Multiplier')
            },
            {
                name: 'multiplier',
                itemId: 'multiplierView',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.multiplier', 'MDC', 'Multiplier')
            },
            {
                xtype: 'numberfield',
                name: 'multiplierEdit',
                minValue: 1,
                maxValue: 2147483647,
                itemId: 'multiplierEdit',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.multiplier', 'MDC', 'Multiplier')
            },
            {

                name: 'shipmentDate',
                itemId: 'shipmentDateView',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.shipmentDate', 'MDC', 'Shipment date'),
                renderer: function (value) {
                    if (Ext.isEmpty(value.displayValue)) {
                        return '-'
                    } else {
                        return Uni.DateTime.formatDateTimeShort(new Date(value.displayValue));
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
                        return Uni.DateTime.formatDateTimeShort(new Date(value.displayValue));
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
                        return Uni.DateTime.formatDateTimeShort(new Date(value.displayValue));
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
                        return Uni.DateTime.formatDateTimeShort(new Date(value.displayValue));
                    }
                }
            },
            {
                xtype: 'deviceDateFieldEdited',
                name: 'decommissioningDateEdit',
                itemId: 'decommissioningDateEdit',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.decommissionDate', 'MDC', 'Decommissioning date')
            }
        ];

        me.callParent(arguments);
    }
});