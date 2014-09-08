Ext.define('Dsh.view.widget.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.dsh-action-menu',
    plain: true,
    items: [
        {   itemId: 'assign',
            text: 'Item 1',
            action: 'assign'
        },
        {
            itemId: 'close',
            text: 'item 2',
            action: 'close'
        }
    ]
});