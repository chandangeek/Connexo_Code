Ext.define('Mdc.view.setup.searchitems.ContentFilter', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Ext.form.Label',
        'Mdc.view.setup.searchitems.SortMenu'
    ],
    alias: "widget.search-content-filter",
    //store: 'Lbt.store.Logbook',
    border: true,
    header: false,
    collapsible: false,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    items: [
        {
            xtype: 'container',
            name: 'criteriacontainer',
            height: 45,
            layout: {
                type: 'hbox',
                align: 'middle'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<b>Criteria</b>',
                    width: 50
                },
                {
                    xtype: 'container',
                    name: 'filter',
                    header: false,
                    border: false,
                    margin: '10 0 10 0',
                    layout: {
                        type: 'hbox',
                        align: 'stretch',
                        defaultMargins: '0 5'
                    },
                    flex: 1
                },
                {
                    xtype: 'button',
                    name: 'clearitemsfilterbtn',
                    action: 'clearitemsfilter',
                    text: Uni.I18n.translate('searchItems.clearAll', 'MDC', 'Clear all'),
                    disabled: true
                }
            ]
        },
        {
            xtype: 'label',
            html: '<hr>'
        },
        {
            xtype: 'panel',
            name: 'sortpanel',
            header: false,
            border: false,
            flex: 1,
            height: 45,
            layout: {
                type: 'hbox',
                align: 'middle'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<b>Sort</b>',
                    width: 50
                },
                {
                    xtype: 'panel',
                    border: false,
                    name: 'sortitemsbtns',
                    defaults: {
                        margin: '0 5 0 0'
                    }
                },
                {
                    xtype: 'panel',
                    border: false,
                    name: 'sortitemspanel',
                    defaults: {
                        margin: '0 5 0 0'
                    },
                    flex: 1,
                    items: [
                        {
                            xtype: 'button',
                            name: 'addsortitemsbtn',
                            text: Uni.I18n.translate('searchItems.addSort', 'MDC', '+ Add sort'),
                            menu: {
                                xtype: 'items-sort-menu'
                            }
                        }
                    ]
                },
                {
                    xtype: 'button',
                    name: 'clearitemssortbtn',
                    text: Uni.I18n.translate('searchItems.clearAll', 'MDC', 'Clear all'),
                    disabled: true
                }
            ]
        }
    ]
});