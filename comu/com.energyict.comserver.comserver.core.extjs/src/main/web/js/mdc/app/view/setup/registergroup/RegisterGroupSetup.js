Ext.define('Mdc.view.setup.registergroup.RegisterGroupSetup', {
    //extend: 'Ext.panel.Panel',
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerGroupSetup',
    itemId: 'registerGroupSetup',
    requires: [
        'Mdc.view.setup.registergroup.RegisterGroupGrid',
        'Mdc.view.setup.registergroup.RegisterGroupPreview'
        //   'Uni.view.breadcrumb.Trail'
    ],
    /* layout: {
     type: 'vbox',
     align: 'stretch'
     },*/
    //   cls: 'content-container',
//    border: 0,
//    region: 'center',

    content: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                /* {
                 xtype: 'breadcrumbTrail',
                 region: 'north',
                 padding: 6
                 },*/
                {
                    xtype: 'component',
                    html: '<h1>' + Uni.I18n.translate('registerGroup.registerGroups','MDC','Register groups') + '</h1>',
                    margins: '10 10 10 10',
                    itemId: 'registerGroupTitle'
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'registerGroupGridContainer'
                },
                {
                    xtype: 'component',
                    height: 25
                },
                {
                    xtype: 'registerGroupPreview'
                }
            ]}
    ],

    /*   side: [
     {
     xtype: 'registerGroupFilter',
     name: 'filter'
     }
     ],
     */

    initComponent: function () {
        this.callParent(arguments);
        this.down('#registerGroupGridContainer').add(
            {
                xtype: 'registerGroupGrid'
            }
        );
    }
});


