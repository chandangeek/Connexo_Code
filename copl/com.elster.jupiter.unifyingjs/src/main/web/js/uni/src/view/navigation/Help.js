Ext.define('Uni.view.navigation.Help', {
    extend: 'Ext.button.Button',
    alias: 'widget.navigationHelp',
    action: 'help',
    glyph: 'xe009@icomoon',
    scale: 'small',
    cls: 'nav-help',
    initComponent: function () {
        this.callParent(arguments);
    }
});