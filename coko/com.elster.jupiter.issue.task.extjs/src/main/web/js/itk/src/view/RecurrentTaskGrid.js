/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.view.RecurrentTaskGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.task-details-grid',
    store: null,
    ui: 'medium',
    maxHeight: 408,
    columns: [
        {
            text: Uni.I18n.translate('general.recurrent.taskId', 'ITK', 'Id'),
            dataIndex: 'id',
            flex: 1
        },
        {
            text: Uni.I18n.translate('general.recurrent.triggerTime', 'ITK', 'Name'),
            dataIndex: 'name',
            flex: 3
        },
        {
            text: Uni.I18n.translate('general.startDate', 'ITK', 'Queue'),
            dataIndex: 'queue',
            flex: 2
        },
        {
            text: Uni.I18n.translate('general.endDate', 'ITK', 'Display type'),
            dataIndex: 'displayType',
            flex: 2
        }
    ]
});
