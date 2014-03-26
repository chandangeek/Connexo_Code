Ext.define('Usr.view.group.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.groupBrowse',
    cls: 'content-wrapper',
    overflowY: 'auto',
    requires: [
        'Usr.view.group.List',
        'Usr.view.group.Details'
    ],

    /*side: [
        {
            xtype: 'container',
            cls: 'content-container',
            //style: 'background-color:lightgray',
            padding: '10 10 0 10',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            defaults: {
                anchor: '100%',
                margins: '0 0 10 0'
            },

            items: [
                {
                    xtype: 'component',
                    html: '<h3>' + Uni.I18n.translate('general.filter', 'USM', 'Filter') + '</h3>',
                    height: 30
                },
                {
                    xtype: 'label',
                    text: Uni.I18n.translate('group.name', 'USM', 'Role name'),
                    height: 20
                },
                {
                    xtype:'textfield',
                    name: 'groupname'
                },
                {
                    xtype: 'container',
                    layout: {
                        type: 'hbox',
                        align: 'center'
                    },
                    items: [
                        {
                            xtype: 'button',
                            action: 'applyFilter',
                            text: Uni.I18n.translate('general.apply', 'USM', 'Apply'),
                            margin: '10 0 0 10 '
                        },
                        {
                            xtype: 'button',
                            action: 'clearFilter',
                            text: Uni.I18n.translate('general.clear', 'USM', 'Clear all'),
                            margin: '10 0 0 10 '
                        }
                    ]
                }
            ]
        }
    ],*/

    content: [
        {
            xtype: 'container',
            cls: 'content-container',
            padding: '10 10 0 10',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            defaults: {
                //anchor: '100%',
                margins: '0 0 10 0'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h1>' + Uni.I18n.translate('group.title', 'USM', 'Roles') + '</h1>'
                },
                {
                    xtype: 'groupList'
                },
                {
                    xtype: 'groupDetails'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});