/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.view.bulk.ReadingTypesSelectionGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    xtype: 'reading-types-selection-grid',
    store: 'Mtr.readingtypes.store.ReadingTypesBulk',

    allLabel: Uni.I18n.translate('readingtypesmanagment.bulk.allreadingtypestitle', 'MTR', 'All reading types'),
    allDescription: Uni.I18n.translate('readingtypesmanagment.bulk.allreadingtypesMsg', 'MTR', 'Select all reading types (related to filters on previous screen)'),

    selectedLabel: Uni.I18n.translate('readingtypesmanagment.bulk.selectedreadingtypes', 'MTR', 'Selected reading types'),
    selectedDescription: Uni.I18n.translate('readingtypesmanagment.bulk.selectedreadingtypesMsg', 'MTR', 'Select reading types in table below'),

    cancelHref: '#/administration/readingtypes',

    radioGroupName: 'reading-types-selection-grid-step1',

    columns: [
        {
            header: Uni.I18n.translate('readingtypesmanagment.readingtype', 'MTR', 'Reading type'),
            dataIndex: 'fullAliasName',
            flex: 3
        },
        {
            header: Uni.I18n.translate('readingtypesmanagment.status', 'MTR', 'Status'),
            dataIndex: 'active',
            renderer: function(value){
                return value
                    ? Uni.I18n.translate('readingtypesmanagment.active', 'MTR', 'Active')
                    : Uni.I18n.translate('readingtypesmanagment.inactive', 'MTR', 'Inactive');
            },
            flex: 1
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
        this.onChangeSelectionGroupType();
        this.getBottomToolbar().setVisible(false);
    }
});