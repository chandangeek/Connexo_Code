/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.view.NoIssuesFoundPanel', {
    extend: 'Uni.view.notifications.NoItemsFoundPanel',
    alias: 'widget.no-issues-found-panel',
    title: Uni.I18n.translate('issues.empty.title', 'ITK', 'No issues found'),
    reasons: [
        Uni.I18n.translate('workspace.issues.empty.list.item1', 'ITK', 'No issue creation rules have been defined yet.'),
        Uni.I18n.translate('workspace.issues.empty.list.item2', 'ITK', "The current issue creation rules haven't generated any issues."),
        Uni.I18n.translate('workspace.issues.empty.list.item3', 'ITK', 'No issues comply to the filter.')
    ]
});