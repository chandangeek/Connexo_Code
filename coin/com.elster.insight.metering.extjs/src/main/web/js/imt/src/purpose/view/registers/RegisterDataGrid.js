Ext.define('Imt.purpose.view.registers.RegisterDataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.register-data-grid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Imt.purpose.view.registers.RegisterReadingActionMenu'
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
                renderer: function (value, metaData, record) {
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
                            tooltipContent += Uni.I18n.translate('general.deviceQuality.tooltip.moreMessage', 'IMT', 'View reading quality details for more information.');
                        }
                        if (showDeviceQualityIcon) {
                            icon = '<span class="icon-price-tags" style="margin-left:10px; position:absolute;" data-qtitle="'
                                + Uni.I18n.translate('general.deviceQuality', 'IMT', 'Device quality') + '" data-qtip="'
                                + tooltipContent + '"></span>';
                        }
                    }
                    return Uni.I18n.translate('general.dateAtTime', 'IMT', '{0} at {1}', [Uni.DateTime.formatDateShort(date), Uni.DateTime.formatTimeShort(date)]) + icon;
                }
            },
            {
                header: unit
                    ? Uni.I18n.translate('general.valueOf', 'IMT', 'Value ({0})', unit)
                    : Uni.I18n.translate('general.value.empty', 'IMT', 'Value'),
                renderer: me.formatColumn,
                align: 'right',
                width: 200,
                dataIndex: 'value'
            },
            {
                xtype: 'edited-column',
                header: '',
                dataIndex: 'modificationState',
                width: 30,
                emptyText: ' '
            },
            {
                xtype: 'uni-actioncolumn',
                itemId: 'register-data-grid-action-column',
                menu: {
                    xtype: 'purpose-register-readings-data-action-menu',
                    itemId: 'purpose-register-readings-data-action-menu',
                    router: me.router
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
                emptyMsg: Uni.I18n.translate('outputs.registers.noItemsToDisplay', 'IMT', 'There are no items to display'),
                items: [
                    {
                        xtype: 'button',
                        itemId: 'add-reading-button',
                        text: Uni.I18n.translate('general.addReading', 'IMT', 'Add reading'),
                        href: me.router.getRoute('usagepoints/view/purpose/output/addregisterdata').buildUrl(),
                        // disabled: true
                    }
                ]
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
    },

    formatColumn: function (v, metaData, record) {
        var status = record.get('validationResult') ? record.get('validationResult').split('.')[1] : '',
            value = Ext.isEmpty(v) ? '-' : v,
            icon = '';

        if (status === 'notValidated') {
            icon = '<span class="icon-flag6" style="margin-left:10px; position:absolute;" data-qtip="'
                + Uni.I18n.translate('reading.validationResult.notvalidated', 'IMT', 'Not validated') + '"></span>';
        } else if (record.get('confirmedNotSaved')) {
            metaData.tdCls = 'x-grid-dirty-cell';
        } else if (status === 'suspect') {
            icon = '<span class="icon-flag5" style="margin-left:10px; color:red; position:absolute;" data-qtip="'
                + Uni.I18n.translate('reading.validationResult.suspect', 'IMT', 'Suspect') + '"></span>';
        }

        if (record.get('isConfirmed') && !record.isModified('value')) {
            icon = '<span class="icon-checkmark" style="margin-left:10px; position:absolute;"></span>';
        }
        return value + icon;
    }
});