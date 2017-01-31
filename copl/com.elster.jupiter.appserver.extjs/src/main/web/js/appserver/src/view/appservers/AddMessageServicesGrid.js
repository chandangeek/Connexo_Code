/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.AddMessageServicesGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.add-message-services-grid',
    requires: [
        'Apr.store.ActiveService'
    ],

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('general.nrOfMessageServices.selected', count, 'APR',
            'No message services selected', '{0} message service selected', '{0} message services selected'
        );
    },

    bottomToolbarHidden: true,
    checkAllButtonPresent: true,


    columns: [
        {
            header: Uni.I18n.translate('general.messageService', 'APR', 'Message service'),
            dataIndex: 'messageService',
            getSortParam: Ext.emptyFn,
            flex: 1
        }
    ]
});


