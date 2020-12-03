/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicetopology.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceTopologyGrid',
    itemId: 'deviceTopologyGrid',
    store: 'Mdc.store.DeviceTopology',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.DeviceTopology'
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('deviceCommunicationTopology.name', 'MDC', 'Name'),
                dataIndex: 'name',
                renderer: function (value, meta, record) {
                    var href = me.router.getRoute('devices/device').buildUrl({deviceId: encodeURIComponent(record.get('name'))});
                    return '<a href="' + href + '">' + Ext.String.htmlEncode(value) + '</a>'
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('deviceCommunicationTopology.serialNumber', 'MDC', 'Serial number'),
                dataIndex: 'serialNumber',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.nodeAddress', 'MDC', 'Node address'),
                dataIndex: 'nodeAddress',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return record.getG3NodePLCInfo() && !Ext.isEmpty(record.getG3NodePLCInfo().get('nodeAddress')) ? record.getG3NodePLCInfo().get('nodeAddress') : '-';
                }
            },
            {
                header: Uni.I18n.translate('deviceCommunicationTopology.parent', 'MDC', 'Parent'),
                dataIndex: 'parentName',
                renderer: function (value, meta, record) {
                    if (Ext.isEmpty(record.getG3NodePLCInfo().get('parentName'))) {
                        return '-';
                    } else {
                        var href = me.router.getRoute('devices/device').buildUrl({deviceId: encodeURIComponent(record.getG3NodePLCInfo().get('parentName'))});
                        return '<a href="' + href + '">' + Ext.String.htmlEncode(record.getG3NodePLCInfo().get('parentName')) + '</a>'
                    }
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('deviceCommunicationTopology.parentSerialNumber', 'MDC', 'Parent Serial number'),
                dataIndex: 'parentSerialNumber',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return record.getG3NodePLCInfo() && !Ext.isEmpty(record.getG3NodePLCInfo().get('parentSerialNumber')) ? record.getG3NodePLCInfo().get('parentSerialNumber') : '-';
                }
            },
            {
                header: Uni.I18n.translate('general.associationState', 'MDC', 'Association state'),
                dataIndex: 'state',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return record.getG3NodePLCInfo() && !Ext.isEmpty(record.getG3NodePLCInfo().get('state')) ? record.getG3NodePLCInfo().get('state') : '-';
                }
            },
            {
                header: Uni.I18n.translate('general.modulation', 'MDC', 'Modulation'),
                dataIndex: 'modulation',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return record.getG3NodePLCInfo() && !Ext.isEmpty(record.getG3NodePLCInfo().get('modulation')) ? record.getG3NodePLCInfo().get('modulation') : '-';
                }
            },
            {
                header: Uni.I18n.translate('general.phaseInfo', 'MDC', 'Phase info'),
                dataIndex: 'phaseInfo',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return record.getG3NodePLCInfo() && !Ext.isEmpty(record.getG3NodePLCInfo().get('phaseInfo')) ? record.getG3NodePLCInfo().get('phaseInfo') : '-';
                }
            },
            {
                header: Uni.I18n.translate('general.linkQualityIndicator', 'MDC', 'Link quality'),
                dataIndex: 'linkQualityIndicator',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return record.getG3NodePLCInfo() && !Ext.isEmpty(record.getG3NodePLCInfo().get('linkQualityIndicator')) ? record.getG3NodePLCInfo().get('linkQualityIndicator') : '-';
                }
            },
            {
                header: Uni.I18n.translate('general.linkedOn', 'MDC', 'Linked on'),
                dataIndex: 'linkingTimeStamp',
                flex: 1,
                renderer: function (value) {
                    return Ext.isEmpty(value) ? '-' : Uni.DateTime.formatDateTimeShort(new Date(value));
                }
            }
            /* {
                 header: Uni.I18n.translate('general.linkCost', 'MDC', 'Link cost'),
                 dataIndex: 'linkCost',
                 flex: 1,
                 renderer: function (value, metaData, record) {
                     return record.getG3NodePLCInfo() && !Ext.isEmpty(record.getG3NodePLCInfo().get('linkCost')) ? record.getG3NodePLCInfo().get('linkCost') : '-';
                 }
             },
             {
                 header: Uni.I18n.translate('general.roundTrip', 'MDC', 'Round trip'),
                 dataIndex: 'roundTrip',
                 flex: 1,
                 renderer: function (value, metaData, record) {
                     return record.getG3NodePLCInfo() && !Ext.isEmpty(record.getG3NodePLCInfo().get('roundTrip')) ? record.getG3NodePLCInfo().get('roundTrip') : '-';
                 }
             },*/

        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                deferLoading: true,
                dock: 'top',
                displayMsg: Uni.I18n.translate('devices.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} devices'),
                displayMoreMsg: Uni.I18n.translate('devices.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} devices'),
                emptyMsg: Uni.I18n.translate('devices.pagingtoolbartop.emptyMsg', 'MDC', 'There are no devices to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                deferLoading: true,
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('devices.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Devices per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
        me.maxHeight = 560;
    }
});