Ext.define('Imt.purpose.view.RegisterDataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.register-data-grid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    store: 'Imt.purpose.store.RegisterReadings',
    output: null,

    initComponent: function () {
        var me = this,
            readingType =  me.output.get('readingType'),
            unit = readingType && readingType.names ? readingType.names.unitOfMeasure : readingType.unit;
        me.columns = [
            {
                header: Uni.I18n.translate('general.measurementTime', 'IMT', 'Measurement time'),
                flex: 1,
                dataIndex: 'timeStamp',
                renderer: function (value) {
                    return value
                        ? Uni.DateTime.formatDateTimeShort(new Date(value))
                        : '-'
                }
            },
            {
                header: Uni.I18n.translate('general.value', 'IMT', 'Value') + ' (' + unit + ')',
                dataIndex: 'value',
                align: 'right',
                flex: 1
            },
            {
                flex: 1,
                header: ' ',
                renderer: function (v, metaData, record) {
                    var status = record.get('validationResult') ? record.get('validationResult').split('.')[1] : '',
                        icon = '';

                    if (status === 'notValidated') {
                        icon = '<span class="icon-flag6" style="margin-left:-15px;" data-qtip="'
                            + Uni.I18n.translate('reading.validationResult.notvalidated', 'IMT', 'Not validated') + '"></span>';
                    } else if (status === 'suspect') {
                        icon = '<span class="icon-flag5" style="margin-left:-15px; color:red;" data-qtip="'
                            + Uni.I18n.translate('reading.validationResult.suspect', 'IMT', 'Suspect') + '"></span>';
                    }
                    return icon;
                }
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('outputs.registers.pagingtoolbartop.displayMsgItems', 'IMT', '{0} - {1} of {2} items'),
                displayMoreMsg: Uni.I18n.translate('outputs.registers.displayMsgMoreItems', 'IMT', '{0} - {1} of more than {2} items'),
                emptyMsg: Uni.I18n.translate('outputs.registers.noItemsToDisplay', 'IMT', 'There are no items to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                deferLoading: true,
                itemsPerPageMsg: Uni.I18n.translate('outputs.registers.pagingtoolbarbottom.itemsPerPage', 'IMT', 'Readings per page')
            }

        ];
        me.callParent(arguments);
    }
});