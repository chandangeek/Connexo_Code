Ext.define('Itk.view.bulk.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    itemId: 'bulkNavigation',
    alias: 'widget.issue-bulk-navigation',
    width: 200,
    jumpForward: false,
    jumpBack: false,
    ui: 'medium',
    title: Uni.I18n.translate('general.title.bulkActions', 'ITK', 'Bulk action'),
    items: [
        {
            itemId: 'SelectIssues',
            text: Uni.I18n.translate('issues.selectIssues','ITK','Select issues')
        },
        {
            itemId: 'SelectAction',
            text: Uni.I18n.translate('issues.selectAction','ITK','Select action')
        },
        {
            itemId: 'actionDetails',
            text: Uni.I18n.translate('issues.actionDetails','ITK','Action details')
        },
        {   itemId: 'Confirmation',
            text: Uni.I18n.translate('issues.confirmation','ITK','Confirmation')
        },
        {
            itemId: 'Status',
            text: Uni.I18n.translate('issues.status','ITK','Status')
        }
    ]
});