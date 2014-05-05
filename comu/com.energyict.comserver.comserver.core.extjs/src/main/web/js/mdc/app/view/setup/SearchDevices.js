Ext.define('Mdc.view.setup.SearchDevices', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.searchDevices',
    itemId: 'searchDevices',
    requires: [
    ],

    content: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'filter-form'//,
                    //html: 'Content!'
                }
            ]
        }
    ],

    side: [
        {
            xtype: 'label',
            text: Uni.I18n.translate('searchDevices.searchFo', 'MDC', 'Search for:')
        },
        {
            xtype: 'container',
            width: 342,
            layout: {
                type: 'vbox',
                align: 'stretch',
                pack: 'center',
                padding: 20
            },
            items: [
            {
                xtype: 'menu',
                floating: false,
                items: [
                    {
                        xtype: 'menuitem',
                        itemId: 'home',
                        text: 'Home'
                    },
                    {
                        xtype: 'menuitem',
                        itemId: 'about',
                        text: 'About Us'
                    },
                    {
                        xtype: 'menuitem',
                        itemId: 'contact',
                        text: 'Contact us'
                    }
                ]
            }]
//            items: [
//                {
//                    xtype: 'gridpanel',
//                    store: {
//                        fields:['name'],
//                        data:{'items':[
//                            { 'name': 'Lisa'},
//                            { 'name': 'Bart'},
//                            { 'name': 'Homer'},
//                            { 'name': 'Marge'}
//                        ]},
//                        proxy: {
//                            type: 'memory',
//                            reader: {
//                                type: 'json',
//                                root: 'items'
//                            }
//                        }
//                    },
//                    columns: [
//                        {
//                            dataIndex: 'name'
//                        }]
//                }
//            ]
        }
        ]

});
