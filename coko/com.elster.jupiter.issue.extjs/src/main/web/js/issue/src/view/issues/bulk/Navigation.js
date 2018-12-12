/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.bulk.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    itemId: 'bulkNavigation',
    alias: 'widget.bulk-navigation',
    width: 200,
    jumpForward: false,
    jumpBack: false,
    ui: 'medium',
    title: Uni.I18n.translate('general.title.bulkActions', 'ISU', 'Bulk action'),
    items: [
        {
            itemId: 'SelectIssues',
            text: Uni.I18n.translate('issues.selectIssues','ISU','Select issues')
        },
        {
            itemId: 'SelectAction',
            text: Uni.I18n.translate('issue.selectAction','ISU','Select action')
        },
        {
            itemId: 'actionDetails',
            text: Uni.I18n.translate('issue.actionDetails','ISU','Action details')
        },
        {   itemId: 'Confirmation',
            text: Uni.I18n.translate('issue.confirmation','ISU','Confirmation')
        },
        {
            itemId: 'Status',
            text: Uni.I18n.translate('issue.status','ISU','Status')
        }
    ]
});