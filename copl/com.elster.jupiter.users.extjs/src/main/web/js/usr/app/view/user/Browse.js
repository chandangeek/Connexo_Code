Ext.define('Usr.view.user.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.userBrowse',
    overflowY: 'auto',
    requires: [
        'Usr.view.user.List',
        'Usr.view.user.Details',
        'Ext.panel.Panel'
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
                    text: Uni.I18n.translate('user.name', 'USM', 'User name'),
                    height: 20
                },
                {
                    xtype:'textfield',
                    name: 'username'
                },
                {
                    xtype: 'label',
                    text: Uni.I18n.translate('user.domain', 'USM', 'Domain'),
                    height: 20
                },
                {
                    xtype:'combobox',
                    name: 'userdomain'
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
            xtype: 'panel',
            ui: 'large',
            title: '<h1>' + Uni.I18n.translate('user.title', 'USM', 'Users') + '</h1>',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'userList'
                },
                {
                    xtype: 'userDetails'
                }
            ]
        }
    ]
});