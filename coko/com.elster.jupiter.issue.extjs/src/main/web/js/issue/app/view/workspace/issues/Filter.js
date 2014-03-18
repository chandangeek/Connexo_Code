Ext.define('Isu.view.workspace.issues.Filter', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Ext.form.Label'
    ],
    alias: "widget.issues-filter",
    store: 'Isu.store.Issues',
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
            height: 45,
            layout: {
                type: 'hbox',
                align: 'middle'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<b>Filters</b>',
                    width: 50
                },
                {
                    xtype: 'component',
                    html: 'None',
                    name: 'empty-text'
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
                    action: 'clearfilter',
                    text: 'Clear all',
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
            header: false,
            border: false,
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    border: false,
                    layout: {
                        type: 'hbox',
                        align: 'middle'
                    },
                    height: 45,
                    items: [
                        {
                            xtype: 'component',
                            html: '<b>Group</b>',
                            width: 50
                        },
                        {
                            xtype: 'combobox',
                            name: 'groupnames',
                            editable: false,
                            emptyText: 'None',
                            labelSeparator: '',
                            queryMode: 'local',
                            displayField: 'display',
                            valueField: 'Value',
                            labelAlign: 'left',
                            height: 20,
                            onFocus: function () {
                                var me = this;

                                if (!me.isExpanded) {
                                    me.expand()
                                }
                                me.getPicker().focus();
                            }
                        }
                    ]
                },
                {
                    xtype: 'gridpanel',
                    name: 'groupgrid',
                    margin: '10 0 0 0',
                    hidden: true,
                    store: 'Isu.store.IssuesGroups',
                    border: 1,
                    columns: [
                        {
                            text: 'Reason',
                            dataIndex: 'reason',
                            flex: 5
                        },
                        {
                            text: 'Issues',
                            dataIndex: 'number',
                            flex: 1
                        }
                    ],
                    tbar: {
                        xtype: 'panel',
                        name: 'groupitemsshown',
                        hidden: true,
                        border: false
                    },
                    bbar: {
                        xtype: 'pagingtoolbarbottom',
                        store: 'Isu.store.IssuesGroups',
                        dock: 'bottom'
                    }
                }
            ]
        },
        {
            xtype: 'label',
            html: '<hr>'
        },
        {
            xtype: 'panel',
            style: 'font: normal 16px Calibri',
            name: 'issuesforlabel',
            defaults: {
                style: 'font: normal 16px Calibri',
                border: 0
            },
            margin: '10 0 10 0',
            border: 0,
            hidden: true
        },
        {
            xtype: 'label',
            name: 'forissuesline',
            html: '<hr>',
            hidden: true
        },
        {
            xtype: 'panel',
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
                    name: 'sortitemspanel',
                    flex: 1,
                    layout: {
                        type: 'hbox',
                        align: 'middle'
                    },
                    defaults: {
                        margin: '0 5 0 0'
                    },
                    items: [
                        {
                            xtype: 'button',
                            name: 'addsortbtn',
                            text: '+ Add sort',
                            menu: {
                                shadow: false,
                                border: false,
                                plain: true,
                                name: 'addsortitemmenu'
                            }
                        }
                    ]
                },
                {
                    xtype: 'button',
                    name: 'clearsortbtn',
                    text: 'Clear all'
                }
            ]
        }
    ]
});