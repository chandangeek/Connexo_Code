Ext.define('Isu.view.issues.bulk.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    itemId: 'bulkNavigation',
    alias: 'widget.bulk-navigation',
    componentCls: 'isu-bulk-navigation',
    width: 200,
    jumpForward: false,
    jumpBack: false,
    items: [
        {
            itemId: 'SelectIssues',
            text: 'Select issues'
        },
        {
            itemId: 'SelectAction',
            text: 'Select action'
        },
        {
            itemId: 'actionDetails',
            text: 'Action details'
        },
        {   itemId: 'Confirmation',
            text: 'Confirmation'
        },
        {
            itemId: 'Status',
            text: 'Status'
        }
    ]
});