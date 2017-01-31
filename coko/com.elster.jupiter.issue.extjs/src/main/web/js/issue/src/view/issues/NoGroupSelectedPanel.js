/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.NoGroupSelectedPanel', {
    extend: 'Uni.view.notifications.NoItemsFoundPanel',
    alias: 'widget.no-issues-group-selected-panel',
    title: Uni.I18n.translate('issues.group.empty.title', 'ISU', 'No group selected'),
    reasons: [
        Uni.I18n.translate('issues.group.empty.list.item1', 'ISU', 'No group has been selected yet.')
    ],
    stepItems: [
        {
            xtype: 'component',
            html: Uni.I18n.translate('issues.group.selectGroup', 'ISU', 'Select a group of issues.')
        }
    ]
});