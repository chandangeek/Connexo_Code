/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.device.DeviceAttributesForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceAttributesForm',
    router: null,
    fullInfo: false,
    dataLoggerSlave: undefined,

    requires: [
        'Mdc.view.setup.device.form.DeviceDateField',
        'Uni.Auth',
        'Uni.store.Apps'
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
                name: 'name',
                itemId: 'fld-device-name',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.name', 'MDC', 'Name'),
                renderer: function (value) {
                    if (me.fullInfo && value && value.available) {
                        this.show();
                        return Ext.isEmpty(value.displayValue) ? '-' : Ext.String.htmlEncode(value.displayValue);
                    } else {
                        this.hide();
                        return null;
                    }
                }
            },
            {
                name: 'mrid',
                itemId: 'fld-device-mrid',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.mrid', 'MDC', 'MRID'),
                renderer: function (value) {
                    if (me.fullInfo && value && value.available) {
                        this.show();
                        return Ext.isEmpty(value.displayValue) ? '-' : Ext.String.htmlEncode(value.displayValue);
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
                                return '<a href="' + me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view').buildUrl({
                                        deviceTypeId: value.deviceTypeId,
                                        deviceConfigurationId: value.attributeId
                                    }) + '">' + Ext.String.htmlEncode(value.displayValue) + '</a>'
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
                        return Ext.isEmpty(value.displayValue) ? '-' : Ext.String.htmlEncode(value.displayValue);
                    } else {
                        this.hide();
                        return null;
                    }

                }
            },
            {
                name: 'manufacturer',
                itemId: 'fld-device-manufacturer',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.manufacturer', 'MDC', 'Manufacturer'),
                renderer: function (value) {
                    if (me.fullInfo) {
                        this.show();
                        return Ext.isEmpty(value.displayValue) ? '-' : Ext.String.htmlEncode(value.displayValue);
                    } else {
                        this.hide();
                        return null;
                    }

                }
            },
            {
                name: 'modelNbr',
                itemId: 'fld-device-model-number',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.modelNumber', 'MDC', 'Model number'),
                renderer: function (value) {
                    if (me.fullInfo) {
                        this.show();
                        return Ext.isEmpty(value.displayValue) ? '-' : Ext.String.htmlEncode(value.displayValue);
                    } else {
                        this.hide();
                        return null;
                    }

                }
            },
            {
                name: 'modelVersion',
                itemId: 'fld-device-model-version',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.modelVersion', 'MDC', 'Model version'),
                renderer: function (value) {
                    if (me.fullInfo) {
                        this.show();
                        return Ext.isEmpty(value.displayValue) ? '-' : Ext.String.htmlEncode(value.displayValue);
                    } else {
                        this.hide();
                        return null;
                    }
                }
            },
            {
                itemId: 'fld-data-logger',
                fieldLabel: Uni.I18n.translate('general.dataLogger', 'MDC', 'Data logger'),
                hidden: Ext.isEmpty(me.dataLoggerSlave),
                renderer: function() {
                    var dataloggerName = Ext.isEmpty(me.dataLoggerSlave) ? undefined : me.dataLoggerSlave.get('dataloggerName');
                    if (Ext.isEmpty(dataloggerName)) {
                        return '-';
                    }
                    return Ext.String.format(
                        '<a href="{0}">{1}</a>',
                        '#/devices/' + encodeURIComponent(dataloggerName),
                        Ext.String.htmlEncode(dataloggerName)
                    );
                }
            },
            {
                name: 'yearOfCertification',
                itemId: 'fld-year-of-certification',
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
                name: 'usagePoint',
                itemId: 'fld-usage-point',
                fieldLabel: Uni.I18n.translate('general.usagePoint', 'MDC', 'Usage point'),
                usagePointLink: null,
                renderer: function (value) {
                    var appName = 'Insight',
                        url;
                    if (value && (value.available || me.fullInfo)) {
                        this.show();
                        if (Ext.isEmpty(value.displayValue)) {
                            return '-'
                        } else {
                            if (Uni.store.Apps.checkApp(appName)) {
                                if (Mdc.privileges.UsagePoint.canViewInInsight()) {
                                    url = Ext.String.format('{0}/usagepoints/{1}', Uni.store.Apps.getAppUrl(appName), encodeURIComponent(value.displayValue));
                                    return Ext.String.format('<a href="{0}">{1}</a>', url, Ext.String.htmlEncode(value.displayValue));
                                }
                            } else if (Mdc.privileges.UsagePoint.canView()) {
                                url = me.router.getRoute('usagepoints/usagepoint').buildUrl({usagePointId: value.displayValue});
                                return Ext.String.format('<a href="{0}">{1}</a>', url, Ext.String.htmlEncode(value.displayValue));
                            }
                            return Ext.String.htmlEncode(value.displayValue);
                        }
                    } else {
                        this.hide();
                        return null;
                    }
                }
            },
            {
                name: 'geoCoordinates',
                itemId: 'fld-device-coordinates',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.coordinates', 'MDC', 'Coordinates'),
                hidden: !me.fullInfo,
                renderer: function (value) {
                    if (!Ext.isEmpty(value) && !Ext.isEmpty(value.displayValue) && !Ext.isEmpty(value.displayValue.coordinatesDisplay)) {
                        return Ext.String.htmlEncode(value.displayValue.coordinatesDisplay);
                    } else {
                        return '-'
                    }
                }
            },
            {
                name: 'location',
                itemId: 'fld-device-location',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.location', 'MDC', 'Location'),
                renderer: function (value) {
                    if (!Ext.isEmpty(value) && !Ext.isEmpty(value.displayValue) && !Ext.isEmpty(value.displayValue.formattedLocationValue)) {
                        return Ext.String.htmlEncode(value.displayValue.formattedLocationValue).replace(/(?:\\r\\n|\\r|\\n)/g, '<br>');
                    } else {
                        return '-'
                    }
                }
            },
            {
                name: 'batch',
                itemId: 'fld-device-batch',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.batch', 'MDC', 'Batch'),
                renderer: function (value) {
                    if (!Ext.isEmpty(value) && !Ext.isEmpty(value.displayValue)) {
                        return Ext.String.htmlEncode(value.displayValue)
                    } else {
                        return '-'
                    }
                }
            },
            {
                name: 'multiplier',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.multiplier', 'MDC', 'Multiplier'),
                hidden: !me.fullInfo,
                renderer: function (value) {
                    if (!Ext.isEmpty(value) && !Ext.isEmpty(value.displayValue)) {
                        return value.displayValue
                    } else {
                        return '-'
                    }
                }
            },
            {
                name: 'shipmentDate',
                itemId: 'fld-device-shipment-date',
                fieldLabel: Uni.I18n.translate('deviceGeneralInformation.shipmentDate', 'MDC', 'Shipment date'),
                hidden: !me.fullInfo,
                renderer: function (value) {
                    if (value && (value.available || me.fullInfo)) {
                        if (Ext.isEmpty(value.displayValue)) {
                            return '-'
                        } else {
                            return Uni.DateTime.formatDateTimeShort(new Date(value.displayValue));
                        }
                    }
                    return '-'
                }
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
            }
        ];
        me.callParent(arguments);
    }
});