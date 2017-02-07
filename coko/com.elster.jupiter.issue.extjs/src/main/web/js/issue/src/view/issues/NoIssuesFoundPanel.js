/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.NoIssuesFoundPanel', {
    extend: 'Uni.view.notifications.NoItemsFoundPanel',
    alias: 'widget.no-issues-found-panel',
    title: Uni.I18n.translate('issues.empty.title', 'ISU', 'No issues found'),
    reasons: [
        Uni.I18n.translate('workspace.issues.empty.list.item1', 'ISU', 'No issues have been defined yet.'),
        Uni.I18n.translate('workspace.issues.empty.list.item2', 'ISU', 'No issues comply with the filter.')
    ]
});