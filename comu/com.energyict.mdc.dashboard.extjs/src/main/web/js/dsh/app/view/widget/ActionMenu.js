Ext.define('Dsh.view.widget.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.dsh-action-menu',
    plain: true,
    items: [
        {   itemId: 'assign',
            text: 'Assign',
            action: 'assign'
        },
        {
            itemId: 'close',
            text: 'Close',
            action: 'close'
        }
    ]
});