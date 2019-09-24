/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.AddPrivilegesGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.addPrivilegesGrid',
    checkAllButtonPresent: true,

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('general.nrOfPrivileges.selected', count, 'MDC',
            'No privileges selected', '{0} privilege selected', '{0} privileges selected')
    },

    bottomToolbarHidden: true,

    columns: [
        {
            text: Uni.I18n.translate('general.privilege', 'MDC', 'Privilege'),
            dataIndex: 'name',
            flex: 1
        }
    ]

});