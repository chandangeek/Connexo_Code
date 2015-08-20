Ext.define('Mdc.view.setup.device.DeviceAttributesForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceAttributesForm',
    router: null,
    fullInfo: false,

    requires: [
        'Mdc.view.setup.device.form.DeviceDateField'
    ],

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    defaults: {
        labelWidth: 150,
        xtype: 'displayfield'
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                name: 'mrid',
                itemId: 'fld-device-mrid',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.mrid', 'MDC', 'MRID'),
                renderer: function (value) {
                    if (value && (value.available || me.fullInfo)) {
                        this.show();
                        if (Ext.isEmpty(value.displayValue)) {
                            return '-'
                        } else {
                            return Ext.htmlEncode(value.displayValue)
                        }
                    } else {
                        this.hide();
                        return null;
                    }

                }
            },
            {
                name: 'lifeCycleState',
                itemId: 'fld-device-state',
                fieldLabel: Uni.I18n.translate('general.state', 'MDC', 'State'),
                renderer: function (value) {
                    if (value && (value.available || me.fullInfo)) {
                        this.show();
                        if (Ext.isEmpty(value.displayValue)) {
                            return '-'
                        } else {
                            return Ext.String.htmlEncode(value.displayValue) + ' (<a href="' + me.router.getRoute('devices/device/history').buildUrl() + '">' + Uni.I18n.translate('deviceHistory.viewHistory', 'MDC', 'View history') + ')</a>';
                        }
                    } else {
                        this.hide();
                        return null;
                    }
                }
            },
            {
                name: 'serialNumber',
                itemId: 'fld-device-serial',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.serialNumber', 'MDC', 'Serial number'),
                renderer: function (value) {
                    if (value && (value.available || me.fullInfo)) {
                        this.show();
                        if (Ext.isEmpty(value.displayValue)) {
                            return '-'
                        } else {
                            return Ext.String.htmlEncode(value.displayValue);
                        }
                    } else {
                        this.hide();
                        return null;
                    }

                }
            },
            {
                name: 'deviceType',
                itemId: 'fld-device-type-name',
                fieldLabel: Uni.I18n.translate('general.deviceType', 'MDC', 'Device type'),
                renderer: function (value) {
                    if (value && (value.available || me.fullInfo)) {
                        this.show();
                        if (Ext.isEmpty(value.displayValue)) {
                            return '-'
                        } else {
                            if (Mdc.privileges.DeviceType.canView()) {
                                return '<a href="' + me.router.getRoute('administration/devicetypes/view').buildUrl({deviceTypeId: value.attributeId}) + '">' + Ext.String.htmlEncode(value.displayValue) + '</a>'
                            } else {
                                return value.displayValue
                            }
                        }
                    } else {
                        this.hide();
                        return null;
                    }

                }
            },
            {
                name: 'deviceConfigurationDisplay',
                fieldLabel: Uni.I18n.translate('general.deviceConfiguration', 'MDC', 'Device configuration'),
                itemId: 'fld-device-config-name',
                renderer: function (value) {
                    if (value && (value.available || me.fullInfo)) {
                        this.show();
                        if (Ext.isEmpty(value.displayValue)) {
                            return '-'
                        } else {
                            if (Mdc.privileges.DeviceType.canView()) {
                                return '<a href="' + me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view').buildUrl({deviceTypeId: value.deviceTypeId, deviceConfigurationId: value.attributeId}) + '">' + Ext.String.htmlEncode(value.displayValue) + '</a>'
                            } else {
                                return value.displayValue
                            }
                        }
                    } else {
                        this.hide();
                        return null;
                    }

                }
            },
            {
                name: 'usagePoint',
                itemId: 'fld-usage-point',
                fieldLabel: Uni.I18n.translate('general.usagePoint', 'MDC', 'Usage point'),
                renderer: function (value) {
                    if (value && (value.available || me.fullInfo)) {
                        this.show();
                        if (Ext.isEmpty(value.displayValue)) {
                            return '-'
                        } else {
                            return '<a href="' + me.router.getRoute('usagepoints/usagepoint').buildUrl({usagePointMRID: value.displayValue}) + '">' + Ext.String.htmlEncode(value.displayValue) + '</a>'
                        }
                    } else {
                        this.hide();
                        return null;
                    }

                }
            },
            {
                xtype: 'deviceFormDateField',
                name: 'shipmentDate',
                itemId: 'fld-device-shipment-date',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.shipmentDate', 'MDC', 'Shipment date'),
                fullInfo: me.fullInfo
            },
            {
                xtype: 'deviceFormDateField',
                name: 'installationDate',
                itemId: 'fld-device-installation-date',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.installationDate', 'MDC', 'Installation date'),
                fullInfo: me.fullInfo
            },
            {
                xtype: 'deviceFormDateField',
                name: 'deactivationDate',
                itemId: 'fld-device-deactivation-date',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.deactivationDate', 'MDC', 'Deactivation date'),
                fullInfo: me.fullInfo
            },
            {
                xtype: 'deviceFormDateField',
                name: 'decommissioningDate',
                itemId: 'fld-device-decommission-date',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.decommissionDate', 'MDC', 'Decommissioning date'),
                fullInfo: me.fullInfo
            },
            {
                name: 'yearOfCertification',
                itemId: 'fld-year-of-certification',
                hidden: !me.fullInfo,
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.yearOfCertification', 'MDC', 'Year of certification'),
                renderer: function (value) {
                    if (!Ext.isEmpty(value) && !Ext.isEmpty(value.displayValue)) {
                        return value.displayValue
                    } else {
                        return '-'
                    }
                }
            },
            {
                name: 'batch',
                itemId: 'fld-device-batch',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.batch', 'MDC', 'Batch'),
                hidden: !me.fullInfo,
                renderer: function (value) {
                    if (!Ext.isEmpty(value) && !Ext.isEmpty(value.displayValue)) {
                        return value.displayValue
                    } else {
                        return '-'
                    }
                }
            }
        ];

        me.callParent(arguments);
    }
});