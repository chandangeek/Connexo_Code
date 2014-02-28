Ext.define('Mdc.view.setup.registertype.RegisterTypeSetup', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.registerTypeSetup',
    autoScroll: true,
    itemId: 'registerTypeSetup',
    requires: [
        'Mdc.view.setup.registertype.RegisterTypeGrid',
        'Mdc.view.setup.registertype.RegisterTypePreview',
        'Uni.view.breadcrumb.Trail'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    cls: 'content-container',
//    border: 0,
//    region: 'center',

    items: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'breadcrumbTrail',
                    region: 'north',
                    padding: 6
                },
                {
                    xtype: 'component',
                    html: '<h1>' + Uni.I18n.translate('registerType.registerTypes','MDC','Register types') + '</h1>',
                    margins: '10 10 10 10',
                    itemId: 'registerTypeTitle'
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'registerTypeGridContainer'
                },
                {
                    xtype: 'component',
                    height: 25
                },
                {
                    xtype: 'registerTypePreview'
                }
            ]}
    ],


    initComponent: function () {
        this.callParent(arguments);
        this.down('#registerTypeGridContainer').add(
            {
                xtype: 'registerTypeGrid'
            }
        );
    }
});


