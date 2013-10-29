Ext.define('Uni.view.navigation.Switcher', {
    extend: 'Ext.button.Button',
    alias: 'widget.navigationSwitcher',
    action: 'switch',
    glyph: 'xf0c9@icomoon',
    scale: 'medium',
    cls: 'nav-switcher',
    initComponent: function () {
        this.callParent(arguments);
    }
});