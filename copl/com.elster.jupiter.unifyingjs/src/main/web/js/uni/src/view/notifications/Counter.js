Ext.define('Uni.view.notifications.Counter', {
    extend: 'Ext.button.Button',
    alias: 'widget.notificationsCounter',
    text: '15',
    action: 'preview',
    glyph: 'xe012@icomoon',
    scale: 'small',
    cls: 'notifications-counter',
    initComponent: function () {
        this.callParent(arguments);
    }
});