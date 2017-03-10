/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.view.NoUsagePointsFound', {
    extend: 'Uni.view.notifications.NoItemsFoundPanel',
    alias: 'widget.no-usagepoints-found-panel',
    xtype: 'no-usagepoints-found-panel',
    title: Uni.I18n.translate('usagepointsearch.empty.title', 'IMT', 'No usage points found'),
    reasons: [
        Uni.I18n.translate('usagepointsearch.empty.list.item1', 'IMT', 'There are no usage points in the system.'),
        Uni.I18n.translate('usagepointsearch.empty.list.item2', 'IMT', 'No usage points comply with the filter.')
    ],
    margin: '16 0 24 0'
});