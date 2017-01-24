Ext.define('Mdc.view.setup.deviceregisterdata.MainGrid', {
    extend: 'Ext.grid.Panel',

    requires: [
        'Uni.grid.column.Edited',
        'Uni.grid.column.Action',
        'Uni.grid.column.Obis',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.view.setup.deviceregisterdata.ActionMenu'
    ],

    deviceId: null,
    registerId: null,

    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('device.registerData.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} readings'),
                displayMoreMsg: Uni.I18n.translate('device.registerData.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} readings'),
                emptyMsg: Uni.I18n.translate('device.registerData.pagingtoolbartop.emptyMsg', 'MDC', 'There are no readings to display'),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.addReading', 'MDC', 'Add reading'),
                        privileges: Mdc.privileges.Device.administrateDeviceData,
                        href: '#/devices/' + encodeURIComponent(me.deviceId) + '/registers/' + me.registerId + '/data/add',
                        dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                deferLoading: true,
                params: [
                    {deviceId: me.deviceId},
                    {registerId: me.registerId}
                ],
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('device.registerData.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Readings per page')
            }
        ];

        me.callParent(arguments);
    },

    renderMeasurementTime: function (value, metaData, record) {
        if (Ext.isEmpty(value)) {
            return '-';
        }
        var date = new Date(value),
            showDeviceQualityIcon = false,
            tooltipContent = '',
            icon = '';

        if (!Ext.isEmpty(record.get('readingQualities'))) {
            Ext.Array.forEach(record.get('readingQualities'), function (readingQualityObject) {
                if (readingQualityObject.cimCode.startsWith('1.')) {
                    showDeviceQualityIcon |= true;
                    tooltipContent += readingQualityObject.indexName + '<br>';
                }
            });
            if (tooltipContent.length > 0) {
                tooltipContent += '<br>';
                tooltipContent += Uni.I18n.translate('general.deviceQuality.tooltip.moreMessage', 'MDC', 'View reading quality details for more information.');
            }
            if (showDeviceQualityIcon) {
                icon = '<span class="icon-price-tags" style="margin-left:10px; position:absolute;" data-qtitle="'
                    + Uni.I18n.translate('general.deviceQuality', 'MDC', 'Device quality') + '" data-qtip="'
                    + tooltipContent + '"></span>';
            }
        }
        return Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}', [Uni.DateTime.formatDateShort(date), Uni.DateTime.formatTimeShort(date)]) + icon;
    }
});