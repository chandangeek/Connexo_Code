Ext.define('Uni.view.navigation.AppSwitcher', {
    extend: 'Ext.button.Button',
    alias: 'widget.navigationAppSwitcher',

    text: '',
    action: 'switch',
    glyph: 'xf0c9@icomoon',
    scale: 'small',
    cls: 'nav-appswitcher',
    disabled: true,

    menu: [
        {
            xtype: 'dataview',
            cls: 'nav-appswitcher-menu',
            componentCls: 'nav-appswitcher-menu-comp',
            plain: true,
            showSeparator: false,
            forceLayout: true,
            tpl: [
                '<tpl for=".">',
                '<div class="app-item">',
                '<a href="{url}">',
                '<img href="{icon}" />',
                '<h3>{name}</h3>',
                '</a>',
                '</div>',
                '</tpl>'
            ],
            itemSelector: 'div.app-item',
            store: 'appitems'
        }
    ]
});