Ext.define('Isu.view.workspace.issues.bulk.Navigation', {
    extend: 'Skyline.menu.NavigationMenu',
    alias: 'widget.bulk-navigation',
    componentCls: 'isu-bulk-navigation',
    width: 200,
    jumpForward: true,
    items: [
        {
            text: 'Select issues'
        },
        {
            text: 'Select action'
        },
        {
            text: 'Action details'
        },
        {
            text: 'Confirmation'
        },
        {
            text: 'Status'
        }
    ]

});