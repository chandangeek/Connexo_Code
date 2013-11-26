Ext.define('Uni.view.navigation.AppSwitcher', {
    extend: 'Ext.button.Button',
    alias: 'widget.navigationAppSwitcher',
    action: 'switch',
    glyph: 'xf0c9@icomoon',
    scale: 'medium',
    cls: 'nav-switcher',

    initComponent: function () {
        this.callParent(arguments);
    }
});