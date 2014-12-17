Ext.define('Mdc.view.setup.devicechannels.DataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceLoadProfileChannelDataGrid',
    itemId: 'deviceLoadProfileChannelDataGrid',
    store: 'Mdc.store.ChannelOfLoadProfileOfDeviceData',
    requires: [
        'Uni.grid.column.Action',
        'Mdc.view.setup.devicechannels.DataActionMenu',
        'Uni.grid.column.IntervalFlags',
        'Uni.grid.column.Edited',
        'Uni.view.toolbar.PagingTop'
    ],
    height: 395,
    plugins: [
        'bufferedrenderer',
        'showConditionalToolTip'
    ],
    viewConfig: {
        loadMask: false
    },

    channelRecord: null,
    router: null,

    initComponent: function () {
        var me = this,
            calculatedReadingType = me.channelRecord.get('calculatedReadingType'),
            measurementType = me.channelRecord.get('unitOfMeasure_formatted');

        me.columns = [
            {
                header: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval'),
                dataIndex: 'interval_end',
                width: 200
            },
            {
                header: Uni.I18n.translate('deviceloadprofiles.channels.value', 'MDC', 'Value') + ' (' + measurementType + ')',
                dataIndex: 'value',
                flex: 1,
                align: 'right',
                renderer: function (value) {
                    return !Ext.isEmpty(value) ? value : '';
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
                header: '',
                xtype: 'templatecolumn',
                width: 30,
                align: 'right',
                tpl: new Ext.XTemplate(
                    '{[this.checkValidationResult(values.validationResult)]}',
                    {
                        checkValidationResult: function (validationResult) {
                            var result = '';

                            switch (validationResult) {
                                case 'validationStatus.notValidated':
                                    result = '<span class="validation-column-align"><span class="icon-validation icon-validation-black"></span>';
                                    break;
                                case 'validationStatus.ok':
                                    result = '<span class="validation-column-align"><span class="icon-validation"></span>';
                                    break;
                                case 'validationStatus.suspect':
                                    result = '<span class="validation-column-align"><span class="icon-validation icon-validation-red"></span>';
                                    break;
                            }

                            return result;
                        }
                    }
                )
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
                renderer: function (value) {
                    return !Ext.isEmpty(value) ? value : '';
                }
            },
            {
                xtype: 'interval-flags-column',
                dataIndex: 'intervalFlags',
                align: 'right',
                width: 150
            }
        ];

        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                isFullTotalCount: true,
                displayMsg: '{2} reading(s)',
                items: [
                    '->',
                    {
                        xtype: 'button',
                        itemId: 'device-load-profile-channel-data-edit-readings-button',
                        text: Uni.I18n.translate('deviceloadprofilechannels.data.editReadings', 'MDC', 'Edit readings'),
                        hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceData'),
                        href: typeof me.router.getRoute('devices/device/channels/channel/tableData/editreadings') !== 'undefined'
                            ? me.router.getRoute('devices/device/channels/channel/tableData/editreadings').buildUrl(me.router.arguments, me.router.queryParams) : null
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }

});