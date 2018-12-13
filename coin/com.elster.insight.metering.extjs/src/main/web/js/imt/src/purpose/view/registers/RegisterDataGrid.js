/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.registers.RegisterDataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.register-data-grid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Imt.purpose.view.registers.RegisterReadingActionMenu',
        'Imt.purpose.view.registers.MultipleRegisterReadingsActionMenu',
        'Imt.purpose.util.GridRenderer'
    ],
    store: 'Imt.purpose.store.RegisterReadings',

    initComponent: function () {
        var me = this,
            readingType =  me.output.get('readingType'),
            unit = readingType && readingType.names ? readingType.names.unitOfMeasure : readingType.unit;
        me.columns = [];

        if((me.output.get('deliverableType')==='numerical' || me.output.get('deliverableType')==='billing') && (me.output.get('isCummulative') || me.output.get('isBilling'))){
            me.columns.push( {
                header: Uni.I18n.translate('general.measurementPeriod', 'IMT', 'Measurement period'),
                flex: 2,
                dataIndex: 'interval',
                renderer:  Imt.purpose.util.GridRenderer.renderMeasurementPeriodColumn
            })
        } else if (!me.output.get('hasEvent')){
            me.columns.push({
                header: Uni.I18n.translate('general.measurementTime', 'IMT', 'Measurement time'),
                flex: 1,
                dataIndex: 'timeStamp',
                renderer: Imt.purpose.util.GridRenderer.renderMeasurementTimeColumn
            })
        }
        if(me.output.get('hasEvent')){
            me.columns.push(
                {
                    header: Uni.I18n.translate('device.registerData.eventTime', 'IMT', 'Event time'),
                    dataIndex: 'eventDate',
                    itemId: 'eventTime',
                    renderer: Imt.purpose.util.GridRenderer.renderEventTimeColumn,
                    flex: 1
                }
            );
        }
        me.columns.push({
                header: unit
                    ? Uni.I18n.translate('general.valueOf', 'IMT', 'Value ({0})', unit)
                    : Uni.I18n.translate('general.value.empty', 'IMT', 'Value'),
                renderer: function(value, metaData, record){
                    return Imt.purpose.util.GridRenderer.renderValueColumn(value, metaData, record);
                },
                align: 'right',
                width: 200,
                dataIndex: 'value'
            });
        if(me.output.get('isCummulative')){
            me.columns.push({
                header: unit
                    ? Uni.I18n.translate('device.registerData.deltaValueWithUnit', 'IMT', 'Delta value ({0})',unit)
                    : Uni.I18n.translate('device.registerData.deltaValue', 'IMT', 'Delta value'),
                dataIndex: 'deltaValue',
                align: 'right',
                minWidth: 150,
                flex: 1
            })
        }
        me.columns = me.columns.concat([
            {
                xtype: 'edited-column',
                header: '',
                dataIndex: 'modificationState',
                width: 30,
                emptyText: ' '
            },
            {
                header: Uni.I18n.translate('device.readingData.lastUpdate', 'IMT', 'Last update'),
                dataIndex: 'reportedDateTime',
                flex: 1,
                renderer: Imt.purpose.util.GridRenderer.renderLastUpdateColumn
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                itemId: 'register-data-grid-action-column',
                privileges: Imt.privileges.UsagePoint.admin,
                menu: {
                    xtype: 'purpose-register-readings-data-action-menu',
                    itemId: 'purpose-register-readings-data-action-menu',
                    router: me.router
                }
            }
        ]);
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('outputs.registers.pagingtoolbartop.displayMsgItems', 'IMT', '{0} - {1} of {2} readings'),
                displayMoreMsg: Uni.I18n.translate('outputs.registers.displayMsgMoreItems', 'IMT', '{0} - {1} of more than {2} readings'),
                emptyMsg: Uni.I18n.translate('outputs.registers.noItemsToDisplay', 'IMT', 'There are no readings to display'),
                items: [
                    {
                        xtype: 'button',
                        itemId: 'add-reading-button',
                        text: Uni.I18n.translate('general.addReading', 'IMT', 'Add reading'),
                        href: me.router.getRoute('usagepoints/view/purpose/output/addregisterdata').buildUrl(),
                        privileges: Imt.privileges.UsagePoint.admin
                    },
                    {
                        xtype: 'button',
                        itemId: 'register-readings-bulk-action-button',
                        text: Uni.I18n.translate('general.bulkAction', 'IMT', 'Bulk action'),
                        menu: {
                            xtype: 'register-readings-bulk-action-menu',
                            itemId: 'register-readings-bulk-action-menu'
                        }
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
    }
});