/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.customattributesets.view.SelectionGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.cas-selection-grid',
    checkAllButtonPresent: true,

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('customattributesets.multiselect.selected', count, 'IMT',
            'No custom attribute sets selected', '{0} custom attribute set selected', '{0} custom attribute sets selected'
        );
    },

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                dataIndex: 'name',
                flex: 2
            },
            {
                header: Uni.I18n.translate('customattributesets.viewlevels', 'IMT', 'View levels'),
                dataIndex: 'viewPrivilegesString',
                flex: 1
            },
            {
                header: Uni.I18n.translate('customattributesets.editlevels', 'IMT', 'Edit levels'),
                dataIndex: 'editPrivilegesString',
                flex: 1
            },
            {
                header: Uni.I18n.translate('customattributesets.timeSliced', 'IMT', 'Time-sliced'),
                dataIndex: 'isVersioned',
                renderer: function (value) {
                    return value ?
                        Uni.I18n.translate('general.yes', 'IMT', 'Yes') :
                        Uni.I18n.translate('general.no', 'IMT', 'No');
                }
            }
        ];

        me.callParent(arguments);
    }
});