Ext.define('Uni.view.navigation.Logo', {
    extend: 'Ext.button.Button',
    alias: 'widget.navigationLogo',
    text: 'Jupiter Meter Operations',
    action: 'home',
    glyph: 'xe002@icomoon',
    scale: 'medium',
    iconAlign: 'left',
    href: '#',
    hrefTarget: '_self',
    cls: 'nav-logo',

    initComponent: function () {
        this.callParent(arguments);
    }
});