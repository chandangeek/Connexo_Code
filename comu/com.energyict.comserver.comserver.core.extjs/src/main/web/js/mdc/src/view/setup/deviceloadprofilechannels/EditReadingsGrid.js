Ext.define('Mdc.view.setup.deviceloadprofilechannels.EditReadingsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-loadprofile-channel-edit-readings-grid',
    store: 'Mdc.store.ChannelOfLoadProfileOfDeviceData',
    requires: [
        'Uni.grid.column.Action',
        'Uni.grid.column.IntervalFlags',
        'Uni.grid.column.Edited',
        'Uni.view.toolbar.PagingTop'
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
            readingType = me.channel.get('cimReadingType'),
            measurementType = me.channel.get('unitOfMeasure_formatted'),
            accumulationBehavior;

        me.columns = [
            {
                xtype: 'edited-column',
                header: Uni.I18n.translate('general.edited', 'MDC', 'Edited'),
                dataIndex: 'editedTime'
            },
            {
                header: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval'),
                dataIndex: 'interval_end',
                width: 200
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
                header: Uni.I18n.translate('deviceloadprofiles.channels.value', 'MDC', 'Value'),
                dataIndex: 'value',
                flex: 1,
                align: 'right',
                renderer: function (value) {
                    return !Ext.isEmpty(value) ? value + ' ' + measurementType : '';
                },
                editor: {
                    xtype: 'textfield',
                    stripCharsRe: /[^0-9\.]/,
                    selectOnFocus: true,
                    validateOnChange: true,
                    fieldStyle: 'text-align: right'
                }
            }
        ];

        //Getting 4th magic number of a reading type to understand if it holds cumulative values or not
        if (readingType) {
            accumulationBehavior = readingType.split('.')[3];
        }

        // 1 means cumulative
        if (accumulationBehavior && accumulationBehavior == 1) {
            me.columns.push(
                {
                    header: Uni.I18n.translate('deviceloadprofiles.channels.cumulativeValue', 'MDC', 'Cumulative value'),
                    dataIndex: 'delta',
                    flex: 1,
                    align: 'right',
                    renderer: function (value, metaData, record) {
                        return !Ext.isEmpty(value) ? value + ' ' + measurementType : '';
                    }
                }
            );
        }

        me.columns.push(
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
                            text: Uni.I18n.translate('deviceloadprofilechannels.editReadings.editValue', 'MDC', 'Edit value'),
                            action: 'editValue'
                        },
                        {
                            itemId: 'remove-reading',
                            text: Uni.I18n.translate('deviceloadprofilechannels.editReadings.removeReading', 'MDC', 'Remove reading'),
                            action: 'removeReading'
                        }
                    ]
                }
            }
        );

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                isFullTotalCount: true,
                displayMsg: Uni.I18n.translate('deviceloadprofilechannels.editReadingsGrid.pagingtoolbartop.displayMsg', 'MDC', '{2} readings'),
                emptyMsg: Uni.I18n.translate('deviceloadprofilechannels.editReadingsGrid.pagingtoolbartop.emptyMsg', 'MDC', 'There are no readings to display')
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
                        href: me.router.getRoute('devices/device/loadprofiles/loadprofile/channels/channel/data').buildUrl()
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});