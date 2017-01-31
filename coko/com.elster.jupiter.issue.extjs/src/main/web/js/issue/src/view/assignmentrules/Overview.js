/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.assignmentrules.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.assignmentrules.List',
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage'
    ],
    alias: 'widget.issue-assignment-rules-overview',

    content: {
        itemId: 'title',
        xtype: 'panel',
        ui: 'large',
        title: Uni.I18n.translate('issue.administration.assignment', 'ISU', 'Issue assignment rules'),
        items: {
            itemId: 'issues-rules-list',
            xtype: 'preview-container',
            grid: {
                xtype: 'issues-assignment-rules-list'
            },
            emptyComponent: {
                xtype: 'uni-form-empty-message',
                text: Uni.I18n.translate('issueAssignment.empty', 'ISU', 'No issue assignment rules have been defined yet.')
            },
            previewComponent: null
        }
    }
});