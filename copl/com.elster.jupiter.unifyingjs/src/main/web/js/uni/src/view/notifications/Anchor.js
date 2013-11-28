Ext.define('Uni.view.notifications.Anchor', {
    extend: 'Ext.button.Button',
    alias: 'widget.notificationsAnchor',

    text: '',
    action: 'preview',
    glyph: 'xe012@icomoon',
    scale: 'small',
    cls: 'notifications-anchor',

    initComponent: function () {
        this.menu = [
            {
                xtype: 'dataview',
                tpl: [
                    '<tpl for=".">',
                    '<div class="notification-item">',
                    '<p>{message}</p>',
                    '</div>',
                    '</tpl>'
                ],
                itemSelector: 'div.notification-item',
                store: Uni.store.Notifications
            }
        ];

        this.callParent(arguments);
    }
});