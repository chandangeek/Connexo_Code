Ext.define('Mdc.view.setup.device.DeviceGeneralInformationPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceGeneralInformationPanel',
    overflowY: 'auto',
    itemId: 'devicegeneralinformationpanel',
    title: Uni.I18n.translate('deviceGeneralInformation.generalInformationTitle', 'MDC', 'General information'),
    ui: 'tile',
    mRID: null,
    router: null,
    items: [
        {
            xtype: 'form',
            itemId: 'deviceGeneralInformationForm',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            defaults: {
                labelWidth: 150,
                xtype: 'displayfield'
            },
            items: [
                {
                    name: 'mRID',
                    itemId: 'fld-device-mrid',
                    fieldLabel: Uni.I18n.translate('deviceGeneralInformation.mrid', 'MDC', 'MRID')
                },
                {
                    name: 'state',
                    itemId: 'fld-device-state',
                    fieldLabel: Uni.I18n.translate('general.state', 'MDC', 'State'),
                    renderer: function (value) {
                        if (value) {
                            this.show();
                            return Ext.String.htmlEncode(value.name) + ' (<a href="' + this.up('#devicegeneralinformationpanel').router.getRoute('devices/device/history').buildUrl() + '">' + Uni.I18n.translate('deviceHistory.viewHistory', 'MDC', 'View history') + ')</a>';
                        } else {
                            this.hide();
                        }
                    }
                },
                {
                    name: 'serialNumber',
                    itemId: 'fld-device-serial',
                    fieldLabel: Uni.I18n.translate('deviceGeneralInformation.serialNumber', 'MDC', 'Serial number')
                },
                {
                    labelAlign: 'right',
                    xtype: 'fieldcontainer',
                    itemId: 'fld-device-type-name',
                    fieldLabel: Uni.I18n.translate('deviceGeneralInformation.deviceType', 'MDC', 'Device type'),
                    margin: '0 0 13 0',
                    layout: {
                        type: 'vbox'
                    },
                    items: [
                        {
                            xtype: 'component',
                            name: 'deviceTypeName',
                            cls: 'x-form-display-field',
                            autoEl: {
                                tag: Mdc.privileges.DeviceType.canView()
                                    ? 'a' : 'div',
                                href: '#',
                                html: Uni.I18n.translate('deviceGeneralInformation.deviceType', 'MDC', 'Device type')
                            },
                            itemId: 'deviceGeneralInformationDeviceTypeLink'
                        }
                    ]
                },
                {
                    labelAlign: 'right',
                    xtype: 'fieldcontainer',
                    itemId: 'fld-device-config-name',
                    margin: '0 0 13 0',
                    fieldLabel: Uni.I18n.translate('deviceGeneralInformation.deviceConfiguration', 'MDC', 'Device configuration'),
                    layout: {
                        type: 'vbox'
                    },
                    items: [
                        {
                            xtype: 'component',
                            name: 'deviceConfigurationName',
                            cls: 'x-form-display-field',
                            autoEl: {
                                tag: Mdc.privileges.DeviceType.canView()
                                    ? 'a' : 'div',
                                href: '#',
                                html: Uni.I18n.translate('deviceGeneralInformation.deviceConfiguration', 'MDC', 'Device configuration')
                            },
                            itemId: 'deviceGeneralInformationDeviceConfigurationLink'
                        }
                    ]
                },
//                {
//                    name: 'yearOfCertification',
//                    itemId: 'fld-year-of-certification',
//                    fieldLabel: Uni.I18n.translate('deviceGeneralInformation.yearOfCertification', 'MDC', 'Year of certification')
//                },
                {
                    name: 'usagePoint',
                    itemId: 'fld-usage-point',
                    fieldLabel: Uni.I18n.translate('deviceGeneralInformation.usagePoint', 'MDC', 'Usage point')
                },
//                {
//                    name: 'serviceCategory',
//                    itemId: 'fld-service-category',
//                    fieldLabel: Uni.I18n.translate('deviceGeneralInformation.serviceCategory', 'MDC', 'Service category')
//                },
//                {
//                    name: 'batch',
//                    itemId: 'fld-device-batch',
//                    fieldLabel: Uni.I18n.translate('deviceGeneralInformation.batch', 'MDC', 'Batch')
//                },
                {
                    name: 'shipment_date',
                    itemId: 'fld-device-shipment-date',
                    fieldLabel: Uni.I18n.translate('deviceGeneralInformation.shipmentDate', 'MDC', 'Shipment date'),
                    renderer: function (value) {
                        console.log(value);
                        if (value.matchCurrentState) {
                            this.show();
                            if (value.timestamp) {
                                return Uni.DateTime.formatDateLong(new Date(value.timestamp));
                            } else {
                                return '-';
                            }
                        } else {
                            this.hide();
                            return null;
                        }

                    }
                },
                {
                    name: 'installation_date',
                    itemId: 'fld-device-installation-date',
                    fieldLabel: Uni.I18n.translate('deviceGeneralInformation.installationDate', 'MDC', 'Installation date'),
                    renderer: function (value) {
                        if (value.matchCurrentState) {
                            this.show();
                            if (value.timestamp) {
                                return Uni.DateTime.formatDateLong(new Date(value.timestamp));
                            } else {
                                return '-';
                            }
                        } else {
                            this.hide();
                            return null;
                        }

                    }
                },
                {
                    name: 'decommission_date',
                    itemId: 'fld-device-decommission-date',
                    fieldLabel: Uni.I18n.translate('deviceGeneralInformation.decommissionDate', 'MDC', 'Decommissioning date'),
                    renderer: function (value) {
                        if (value.matchCurrentState) {
                            this.show();
                            if (value.timestamp) {
                                return Uni.DateTime.formatDateLong(new Date(value.timestamp));
                            } else {
                                return '-';
                            }
                        } else {
                            this.hide();
                            return null;
                        }

                    }
                },
                {
                    name: 'deactivation_date',
                    itemId: 'fld-device-deactivation-date',
                    fieldLabel: Uni.I18n.translate('deviceGeneralInformation.deactivationDate', 'MDC', 'Deactivation date'),
                    renderer: function (value) {
                        if (value.matchCurrentState) {
                            this.show();
                            if (value.timestamp) {
                                return Uni.DateTime.formatDateLong(new Date(value.timestamp));
                            } else {
                                return '-';
                            }
                        } else {
                            this.hide();
                            return null;
                        }

                    }
                }
            ]
        }
    ]
})
;

