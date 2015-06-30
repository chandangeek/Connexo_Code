Ext.define('Idv.view.workspace.issues.bulk.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    itemId: 'bulkNavigation',
    alias: 'widget.bulk-navigation',
    componentCls: 'isu-bulk-navigation',
    width: 200,
    jumpForward: true,
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