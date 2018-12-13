/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.store.ClearStatus', {
    extend: 'Ext.data.Store',
    model: 'Dal.model.ClearStatus',
    autoLoad: false,

    data: [
        {id: 'yes', name: Uni.I18n.translate('alarm.filter.cleared.yes', 'DAL', 'Yes')},
        {id: 'no', name: Uni.I18n.translate('alarm.filter.cleared.no', 'DAL', 'No')}
    ]
});
