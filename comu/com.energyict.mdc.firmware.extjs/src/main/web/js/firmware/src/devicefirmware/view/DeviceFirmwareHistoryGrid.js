/*
 *
 *  * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 *
 */
Ext.define('Fwc.devicefirmware.view.DeviceFirmwareHistoryGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-history-grid',
    store: 'Fwc.devicefirmware.store.DeviceFirmwareHistoryStore',
    requires: [
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.toolbar.PagingTop',
        'Fwc.devicefirmware.store.DeviceFirmwareHistoryStore'
    ],
    router: null,

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                dataIndex: 'firmwareVersion',
                header: Uni.I18n.translate('general.version', 'FWC', 'Version'),
                itemId: 'deviceFirmwareHistory',
                fixed: true,
                flex: 3,
                renderer: function (value) {
                    return value ? value : '-'
                }
            },
            {
                dataIndex: 'imageIdentifier',
                header: Uni.I18n.translate('general.imageIdentifier', 'FWC', 'Image identifier'),
                itemId: 'imageIdentifierHistory',
                fixed: true,
                flex: 3,
                renderer: function (value) {
                    return value ? value : '-'
                }
            },
            {
                header: Uni.I18n.translate('device.firmware.history.PlannedDate', 'FWC', 'Planned upload date'),
                dataIndex: 'plannedDate',
                fixed: true,
                flex: 3,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-'
                }

            },
            {
                header: Uni.I18n.translate('device.firmware.history.uploadDate', 'FWC', 'Uploaded date'),
                dataIndex: 'uploadDate',
                fixed: true,
                flex: 3,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-'
                }

            },
            {
                header: Uni.I18n.translate('device.firmware.history.ActivationDate', 'FWC', 'Activation date'),
                dataIndex: 'activationDate',
                fixed: true,
                flex: 3,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-'
                }

            },
            {
                dataIndex: 'result',
                header: Uni.I18n.translate('device.firmware.history.Result', 'FWC', 'Result'),
                fixed: true,
                flex: 3

            },
            {
                dataIndex: 'triggeredBy',
                header: Uni.I18n.translate('device.firmware.history.TriggeredBy', 'FWC', 'Triggered by'),
                fixed: true,
                flex: 3
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                width: 120,
                privileges: !!Isu.privileges.Issue.adminDevice,
                renderer: function (value) {

                },
                menu: {
                    xtype: 'uni-actions-menu',
                    itemId: 'history-grid-action-menu',
                    router: me.router,
                    items: [
                        {
                            itemId: 'view-log',
                            text: Uni.I18n.translate('general.viewLog', 'FWC', 'View log'),
                            //privileges: Usr.privileges.Users.admin,
                            action: 'viewLog',
                            section: this.SECTION_VIEW
                        },
                    ]
                }
            }
        ];
        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('device.firmware.history.pagingtoolbartop.displayMsg', 'FWC', '{0} - {1} of {2} firmware upgrade attempts'),
                displayMoreMsg: Uni.I18n.translate('device.firmware.history.pagingtoolbartop.displayMoreMsg', 'FWC', '{0} - {1} of more than {2} firmware attempts')
            },
            {
                itemId: 'pagingtoolbarbottom',
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('device.firmware.history.pagingtoolbarbottom.usagesPerPage', 'FWC', 'Firmware upgrade attempts')
            }
        ];
        me.callParent(arguments);
    }
});
