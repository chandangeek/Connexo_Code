Ext.define('Mtr.view.workspace.issues.Filter', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Ext.form.Label'
    ],
    alias: "widget.issues-filter",
    store: 'Mtr.store.Issues',
    border: true,
    header: false,
    collapsible: false,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    items: [
        {
            xtype: 'panel',
            header: false,
            border: false,
            layout: {
                type: 'hbox',
                align: 'left'
            },
            items: [
                {
                    xtype: 'label',
                    style: 'font: normal 16px Calibri',
                    html: 'Filters',
                    margin: '10 0 10 0'
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
            margin: '10 0 10 0',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    border: false,
                    items: {
                        xtype: 'combobox',
                        name: 'groupnames',
                        editable: false,
                        labelStyle: 'font: normal 16px Calibri',
                        emptyText: 'None',
                        fieldLabel: 'Group',
                        labelSeparator: '',
                        queryMode: 'local',
                        displayField: 'display',
                        valueField: 'Value',
                        labelWidth: 60,
                        height: 20,
                        onFocus: function () {
                            var me = this;

                            if (!me.isExpanded) {
                                me.expand()
                            }
                            me.getPicker().focus();
                        }
                    }
                },
                {
                    xtype: 'gridpanel',
                    name: 'groupgrid',
                    margin: '10 2 2 2',
                    hidden: true,
                    store: 'Mtr.store.IssuesGroups',
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
                        store: 'Mtr.store.IssuesGroups',
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
            margin: '2 0 0 0',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'label',
                    style: 'font: normal 16px Calibri',
                    html: 'Sort',
                    margin: '10 0 10 0'
                },
                {
                    xtype: 'panel',
                    border: false,
                    name: 'sortitemspanel',
                    flex: 1,
                    layout: {
                        type: 'hbox',
                        align: 'left'
                    },
                    items: [
                        {
                            xtype: 'button',
                            name: 'addsortbtn',
                            text: '+ Add sort',
                            margin: '10 5 0 10',
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
                    xtype: 'panel',
                    border: false,
                    margin: '3 10 10 10',
                    items: {
                        xtype: 'button',
                        name: 'clearsortbtn',
                        margin: '10 10 10 10',
                        text: 'Clear all'
                    }
                }
            ]
        }
    ]
});