Ext.define('Mdc.view.setup.deviceloadprofilechannels.DataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceLoadProfileChannelDataGrid',
    itemId: 'deviceLoadProfileChannelDataGrid',
    store: 'Mdc.store.ChannelOfLoadProfileOfDeviceData',
    requires: [
        'Uni.grid.column.Action',
        'Mdc.view.setup.deviceloadprofilechannels.DataActionMenu',
        'Uni.grid.column.IntervalFlags',
        'Uni.grid.column.Edited'
    ],
    height: 395,
    plugins: [
        'bufferedrenderer',
        'showConditionalToolTip'
    ],

    //test

    channelRecord: null,

    initComponent: function () {
        var me = this,
            readingType = me.channelRecord.get('cimReadingType'),
            measurementType = me.channelRecord.get('unitOfMeasure_formatted'),
            accumulationBehavior;

        me.columns = [
            {
                header: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval'),
                dataIndex: 'interval_end',
                width: 200
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
                dataIndex: 'editedTime'
            },
            {
                xtype: 'interval-flags-column',
                dataIndex: 'intervalFlags',
                width: 150
            }
        ];

        me.callParent(arguments);
    }

});