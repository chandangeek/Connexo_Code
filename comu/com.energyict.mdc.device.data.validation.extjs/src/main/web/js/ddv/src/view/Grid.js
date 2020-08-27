/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ddv.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.ddv-quality-grid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    store: 'Ddv.store.DataQuality',
    router: null,
    hasHtmlInColumnHeaders: true,
    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'DDV', 'Name'),
                exportText: Uni.I18n.translate('general.name', 'DDV', 'Name'),
                dataIndex: 'deviceName',
                flex: 3,
                renderer: function (value) {
                    var url = me.router.getRoute('devices/device').buildUrl({
                        deviceId: encodeURIComponent(value)
                    });
                    return Mdc.privileges.Device.canViewDeviceCommunication() ? '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>' : value;
                }
            },
            {
                header: Uni.I18n.translate('general.serialNumber', 'DDV', 'Serial number'),
                exportText: Uni.I18n.translate('general.serialNumber', 'DDV', 'Serial number'),
                dataIndex: 'deviceSerialNumber',
                flex: 3
            },
            {
                header: Uni.I18n.translate('general.deviceType', 'DDV', 'Device type'),
                exportText: Uni.I18n.translate('general.deviceType', 'DDV', 'Device type'),
                dataIndex: 'deviceType',
                renderer: function (value) {
                    var url = me.router.getRoute('administration/devicetypes/view').buildUrl({
                        deviceTypeId: value.id
                    });
                    return Mdc.privileges.DeviceType.canView() ? '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>' : Ext.String.htmlEncode(value.name);
                },
                flex: 3
            },
            {
                header: Uni.I18n.translate('general.configuration', 'DDV', 'Configuration'),
                exportText: Uni.I18n.translate('general.configuration', 'DDV', 'Configuration'),
                dataIndex: 'deviceConfig',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view').buildUrl({
                        deviceTypeId: record.get('deviceType').id,
                        deviceConfigurationId: value.id
                    });
                    return Mdc.privileges.DeviceType.canView() ? '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>' : Ext.String.htmlEncode(value.name);
                },
                flex: 3
            },
            {
                header: '<span class="white-circle-grid-header icon-flag5" style="color:red;" data-qtip="' + Uni.I18n.translate('general.suspects', 'DDV', 'Suspects') + '"></span>',
                exportText: Uni.I18n.translate('general.suspects', 'DDV', 'Suspects'),
                dataIndex: 'amountOfSuspects',
                align: 'right',
                flex: 1
            },
            {
                header: '<span class="white-circle-grid-header icon-checkmark" style="color:#686868" data-qtip="' + Uni.I18n.translate('general.confirmed', 'DDV', 'Confirmed') + '"></span>',
                exportText: Uni.I18n.translate('general.confirmed', 'DDV', 'Confirmed'),
                dataIndex: 'amountOfConfirmed',
                align: 'right',
                flex: 1
            },
            {
                header: '<span class="white-circle-grid-header icon-flag5" style="color:#33CC33;" data-qtip="' + Uni.I18n.translate('general.estimates', 'DDV', 'Estimates') + '"></span>',
                exportText: Uni.I18n.translate('general.estimates', 'DDV', 'Estimates'),
                dataIndex: 'amountOfEstimates',
                align: 'right',
                flex: 1
            },
            {
                header: '<span class="white-circle-grid-header icon-flag5" style="color:#dedc49;" data-qtip="' + Uni.I18n.translate('general.informatives', 'DDV', 'Informatives') + '"></span>',
                exportText: Uni.I18n.translate('general.informatives', 'DDV', 'Informatives'),
                dataIndex: 'amountOfInformatives',
                align: 'right',
                flex: 1
            },
            {
                header: '<span class="white-circle-grid-header icon-pencil4" style="color:#686868" data-qtip="' + Uni.I18n.translate('general.edited', 'DDV', 'Edited') + '"></span>',
                exportText: Uni.I18n.translate('general.edited', 'DDV', 'Edited'),
                dataIndex: 'amountOfTotalEdited',
                align: 'right',
                flex: 1
            }
        ];

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                needCustomExporter: true,
                displayMsg: Uni.I18n.translate('dataQuality.paging.displayMsg', 'DDV', '{0} - {1} of {2} devices'),
                displayMoreMsg: Uni.I18n.translate('dataQuality.paging.displayMoreMsg', 'DDV', '{0} - {1} of more than {2} devices'),
                emptyMsg: Uni.I18n.translate('dataQuality.paging.emptyMsg', 'DDV', 'There are no devices to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                deferLoading: true,
                dock: 'bottom',
                needExtendedData: true,
                itemsPerPageMsg: Uni.I18n.translate('dataQuality.paging.devicesPerPage', 'DDV', 'Devices per page')
            }
        ];

        this.callParent(arguments);
    }
});
