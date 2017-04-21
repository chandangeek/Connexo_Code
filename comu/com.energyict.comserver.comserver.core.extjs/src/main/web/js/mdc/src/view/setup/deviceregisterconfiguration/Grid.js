/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterconfiguration.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceRegisterConfigurationGrid',
    itemId: 'deviceRegisterConfigurationGrid',
    device: null,
    router: null,
    store: 'Mdc.store.RegisterConfigsOfDevice',
    scroll: false,
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.RegisterConfigsOfDevice',
        'Mdc.view.setup.deviceregisterconfiguration.ActionMenu',
        'Uni.util.Common'
    ],
    viewConfig: {
        style: { overflow: 'auto', overflowX: 'hidden' },
        enableTextSelection: true

    },
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('deviceregisterconfiguration.register', 'MDC', 'Register'),
                dataIndex: 'readingType',
                renderer: function (value, metaData, record) {
                    var mRID = encodeURIComponent(me.device.get('name'));
                    return '<a href="#/devices/' + mRID + '/registers/' + record.get('id') + '/data">' + Ext.String.htmlEncode(value.fullAliasName) + '</a>';
                },
                flex: 2
            },
            {
                header: Uni.I18n.translate('general.dataUntil', 'MDC', 'Data until'),
                dataIndex: 'timeStamp',
                renderer: function(value){
                    if(!Ext.isEmpty(value)) {
                        return Uni.DateTime.formatDateTimeShort(new Date(value));
                    }
                    return '-';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('deviceregisterconfiguration.latestValue', 'MDC', 'Latest value'),
                dataIndex: 'value',
                width: 450,
                flex: 1
            }
        ];

        if ((!Ext.isEmpty(me.device.get('isDataLogger')) && me.device.get('isDataLogger')) ||
            (!Ext.isEmpty(me.device.get('isMultiElementDevice')) && me.device.get('isMultiElementDevice'))){
            me.columns.push(
                {
                    dataIndex: 'dataloggerSlaveName',
                    flex: 1,
                    header: Mdc.util.LinkPurpose.forDevice(me.device).channelGridSlaveColumn,
                    renderer: function(value) {
                        if (Ext.isEmpty(value)) {
                            return '-';
                        }
                        var href = me.router.getRoute('devices/device/registers').buildUrl({deviceId: encodeURIComponent(value)});
                        return '<a href="' + href + '">' + Ext.String.htmlEncode(value) + '</a>'
                    }
                }
            );
        }

        me.columns.push(
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                menu: {
                    xtype: 'deviceRegisterConfigurationActionMenu',
                    itemId: 'registerActionMenu'
                }
            }
        );
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceregisterconfiguration.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} registers'),
                displayMoreMsg: Uni.I18n.translate('deviceregisterconfiguration.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} registers'),
                emptyMsg: Uni.I18n.translate('deviceregisterconfiguration.pagingtoolbartop.emptyMsg', 'MDC', 'There are no registers'),
                usesExactCount: true
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                params: [
                    {deviceId: Uni.util.Common.decodeURIArguments(me.device.get("name"))}
                ],
                itemsPerPageMsg: Uni.I18n.translate('deviceregisterconfiguration.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Registers per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});

