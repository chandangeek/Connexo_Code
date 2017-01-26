Ext.define('Dal.view.bulk.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    itemId: 'bulkNavigation',
    alias: 'widget.alarm-bulk-navigation',
    width: 200,
    jumpForward: false,
    jumpBack: false,
    ui: 'medium',
    title: Uni.I18n.translate('general.title.bulkActions', 'DAL', 'Bulk action'),
    items: [
        {
            itemId: 'SelectIssues',
            text: Uni.I18n.translate('alarms.selectAlarms','DAL','Select alarms')
        },
        {
            itemId: 'SelectAction',
            text: Uni.I18n.translate('alarms.selectAction','DAL','Select action')
        },
        {
            itemId: 'actionDetails',
            text: Uni.I18n.translate('alarms.actionDetails','DAL','Action details')
        },
        {   itemId: 'Confirmation',
            text: Uni.I18n.translate('alarms.confirmation','DAL','Confirmation')
        },
        {
            itemId: 'Status',
            text: Uni.I18n.translate('alarms.status','DAL','Status')
        }
    ]
});