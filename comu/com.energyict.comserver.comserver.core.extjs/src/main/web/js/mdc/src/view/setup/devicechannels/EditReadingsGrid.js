Ext.define('Mdc.view.setup.devicechannels.EditReadingsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-loadprofile-channel-edit-readings-grid',
    store: 'Mdc.store.ChannelOfLoadProfileOfDeviceData',
    requires: [
        'Uni.grid.column.Action',
        'Uni.grid.column.IntervalFlags',
        'Uni.grid.column.Edited',
        'Uni.view.toolbar.PagingTop',
        'Uni.grid.column.ValidationFlag'
    ],
    height: 395,
    plugins: [
        'bufferedrenderer',
        'showConditionalToolTip',
        {
            ptype: 'cellediting',
            clicksToEdit: 1,
            pluginId: 'cellplugin'
        }
    ],

    channel: null,

    initComponent: function () {
        var me = this,
            calculatedReadingType = me.channel.get('calculatedReadingType'),
            measurementType = me.channel.get('unitOfMeasure');

        me.columns = [
            {
                header: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval'),
                dataIndex: 'interval_end',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                },
                width: 200
            },
            {
                xtype: 'validation-flag-column',
                header: Uni.I18n.translate('deviceloadprofiles.channels.value', 'MDC', 'Value') + ' (' + measurementType + ')',
                dataIndex: 'value',
                flex: 1,
                align: 'right',
                renderer: function (value, metaData, record) {
                    if (record.get('validationResult')) {
                        var result = record.get('validationResult'),
                            status = result.split('.')[1],
                            cls = 'icon-validation-cell ';
                        if (status === 'suspect') {
                            cls += 'icon-validation-red'
                        }
                        if (status === 'notValidated') {
                            cls += 'icon-validation-black'
                        }
                        metaData.tdCls = cls;
                    }
                    if(!Ext.isEmpty(value)) {
                        var val = Uni.Number.formatNumber(value, -1);
                        return !Ext.isEmpty(val)? val : '';
                    }
                },
                editor: {
                    xtype: 'textfield',
                    stripCharsRe: /[^0-9\.]/,
                    selectOnFocus: true,
                    validateOnChange: true,
                    fieldStyle: 'text-align: right'
                }
            },
            {
                xtype: 'edited-column',
                header: '',
                dataIndex: 'modificationState',
                width: 30
            },
            {
                header: Uni.I18n.translate('deviceloadprofiles.channels.bulkValue', 'MDC', 'Bulk value') + ' (' + measurementType + ')',
                dataIndex: 'collectedValue',
                flex: 1,
                align: 'right',
                hidden: Ext.isEmpty(calculatedReadingType),
                renderer: function (v) {
                    if(!Ext.isEmpty(v)) {
                        var value = Uni.Number.formatNumber(v, -1);
                        return !Ext.isEmpty(value)? value : '';
                    }
                }
            },
            {
                xtype: 'interval-flags-column',
                dataIndex: 'intervalFlags',
                width: 150
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'menu',
                    itemId: 'device-loadprofile-channel-edit-readings-action-menu',
                    plain: true,
                    border: false,
                    shadow: false,
                    defaultAlign: 'tr-br?',
                    items: [
                        {
                            itemId: 'edit-value',
                            text: Uni.I18n.translate('devicechannels.editReadings.editValue', 'MDC', 'Edit value'),
                            action: 'editValue'
                        },
                        {
                            itemId: 'remove-reading',
                            text: Uni.I18n.translate('devicechannels.editReadings.removeReading', 'MDC', 'Remove reading'),
                            action: 'removeReading'
                        }
                    ]
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                isFullTotalCount: true,
                displayMsg: Uni.I18n.translate('devicechannels.editReadingsGrid.pagingtoolbartop.displayMsg', 'MDC', '{2} readings'),
                emptyMsg: Uni.I18n.translate('devicechannels.editReadingsGrid.pagingtoolbartop.emptyMsg', 'MDC', 'There are no readings to display')
            },
            {
                xtype: 'toolbar',
                dock: 'bottom',
                items: [
                    {
                        itemId: 'device-loadprofile-channel-edit-readings-save',
                        text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                        ui: 'action'
                    },
                    {
                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                        ui: 'link',
                        href: me.router.getRoute('devices/device/channels/channeltableData').buildUrl(me.router.arguments, me.router.queryParams)
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});