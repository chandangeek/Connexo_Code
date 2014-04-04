Ext.define('Mdc.view.setup.registertype.RegisterTypeSetup', {
    //extend: 'Ext.panel.Panel',
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerTypeSetup',
    autoScroll: true,
    itemId: 'registerTypeSetup',
    requires: [
        'Mdc.view.setup.registertype.RegisterTypeGrid',
        'Mdc.view.setup.registertype.RegisterTypePreview',
        'Mdc.view.setup.registertype.RegisterTypeFilter'
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

 /*   side: [
           {
               xtype: 'registerTypeFilter',
               name: 'filter'
           }
       ],
*/

    initComponent: function () {
        this.callParent(arguments);
        this.down('#registerTypeGridContainer').add(
            {
                xtype: 'registerTypeGrid'
            }
        );
    }
});


