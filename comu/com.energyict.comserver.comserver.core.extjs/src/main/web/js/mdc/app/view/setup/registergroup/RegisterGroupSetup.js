Ext.define('Mdc.view.setup.registergroup.RegisterGroupSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerGroupSetup',
    itemId: 'registerGroupSetup',

    requires: [
        'Mdc.view.setup.registergroup.RegisterGroupGrid',
        'Mdc.view.setup.registergroup.RegisterGroupPreview',
        'Ext.layout.container.Card',
        'Uni.view.container.EmptyGridContainer'
    ],

    hidden: true,

    content: [
        {
            xtype: 'container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            padding: '0 10 0 10',
            items: [
                {
                    xtype: 'component',
                    html: '<h1>' + Uni.I18n.translate('registerGroup.registerGroups', 'MDC', 'Register groups') + '</h1>',
                    //margins: '10 10 10 10',
                    itemId: 'registerGroupTitle'
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'registerGroupGridContainer'
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
                xtype: 'emptygridcontainer',
                itemId: 'registerGroupEmptyGrid',
                grid: {
                    xtype: 'registerGroupGrid'
                },
                emptyComponent: {
                    xtype: 'container',
                    layout: {
                        type: 'hbox',
                        align: 'left'
                    },
                    minHeight: 20,
                    items: [
                        {
                            xtype: 'image',
                            margin: '0 10 0 0',
                            src: "../mdc/resources/images/information.png",
                            height: 20,
                            width: 20
                        },
                        {
                            xtype: 'container',
                            items: [
                                {
                                    xtype: 'component',
                                    html: '<b>' + Uni.I18n.translate('registerGroup.empty.title', 'MDC', 'No register groups found') + '</b><br>' +
                                        Uni.I18n.translate('registerGroup.empty.detail', 'MDC', 'There are no register groups. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                        Uni.I18n.translate('registerGroup.empty.list.item1', 'MDC', 'No register groups have been defined yet.') + '</li></lv><br>' +
                                        Uni.I18n.translate('registerGroup.empty.steps', 'MDC', 'Possible steps:')
                                },
                                {
                                    xtype: 'button',
                                    margin: '10 0 0 0',
                                    text: Uni.I18n.translate('registerGroup.create', 'MDC', 'Create register group'),
                                    action: 'createRegisterGroup'
                                }
                            ]
                        }
                    ]
                }
            }
        );
    }
});


