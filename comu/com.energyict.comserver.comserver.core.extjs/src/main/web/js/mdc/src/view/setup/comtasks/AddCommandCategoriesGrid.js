/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.AddCommandCategoriesGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.addCommandCategoriesGrid',
    checkAllButtonPresent: true,
    //plugins: {
    //    ptype: 'bufferedrenderer'
    //},

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('general.nrOfCommandCategories.selected', count, 'MDC',
            'No command categories selected', '{0} command category selected', '{0} command categories selected')
    },

    bottomToolbarHidden: true,

    columns: [
        {
            text: Uni.I18n.translate('commands.category.name', 'MDC', 'Command category'),
            dataIndex: 'name',
            flex: 1
        }
    ]

});