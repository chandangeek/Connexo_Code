/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ddv.view.validations.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.ddv-validations-grid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    store: 'Ddv.store.Validations',
    router: null,
    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('validations.name', 'DDV', 'Name'),
                dataIndex: 'name',
                flex: 1,
                renderer: function (value) {
                    var href = me.router.getRoute('devices/device').buildUrl({deviceId: encodeURIComponent(value)});
                    return '<a href="' + href + '">' + Ext.String.htmlEncode(value) + '</a>';
                }
            },
            {
                header: Uni.I18n.translate('validations.serialNumber', 'DDV', 'Serial number'),
                dataIndex: 'serialNumber',
                flex: 1
            },
            {
                header: Uni.I18n.translate('validations.deviceType', 'DDV', 'Device type'),
                dataIndex: 'deviceType',
                flex: 1
            },
            {
                header: Uni.I18n.translate('validations.devicedeviceConfigConfig', 'DDV', 'Configuration'),
                dataIndex: 'deviceConfig',
                flex: 1
            },
            {
                header: Uni.I18n.translate('validations.amountOfSuspects', 'DDV', 'Amount of suspects'),
                dataIndex: 'amountOfSuspects',
                flex: 1
            }
        ];

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('validations.pagingtoolbartop.displayMsg', 'DDV', '{0} - {1} of {2} devices with suspects'),
                displayMoreMsg: Uni.I18n.translate('validations.pagingtoolbartop.displayMoreMsg', 'DDV', '{0} - {1} of more than {2} devices with suspects'),
                emptyMsg: Uni.I18n.translate('validations.pagingtoolbartop.emptyMsg', 'DDV', 'There are no data devices with suspects to display'),
                items: []
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                deferLoading: true,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('validations.pagingtoolbarbottom.deviceWithSuspects', 'DDV', 'Devices with suspects per page'),
                deferLoading: true
            }
        ];

        this.callParent(arguments);
    }
});
