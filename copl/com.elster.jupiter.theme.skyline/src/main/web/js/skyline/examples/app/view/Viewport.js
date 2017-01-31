/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('example.view.Viewport', {
    extend: 'Ext.container.Viewport',
    layout: 'border',

    defaults: {
        collapsible: false,
        split: true
    },

    items: [{
        region: 'west',
        collapsible: true,
        title: 'Navigation',
        ui: 'medium',
        width: 300,
        layout: 'fit',
        items: {
            xtype: 'form',
            title: 'Filter',
            ui: 'filter',
            layout: 'hbox',
            items: [{
                xtype: 'checkboxgroup',
                fieldLabel: 'Rule',
                // Arrange checkboxes into two columns, distributed vertically
                columns: 1,
                vertical: true,
                labelAlign: 'top',
                items: [
                    { boxLabel: 'Item 1', name: 'rb', inputValue: '1' },
                    { boxLabel: 'Item 2', name: 'rb', inputValue: '2', checked: true },
                    { boxLabel: 'Item 3', name: 'rb', inputValue: '3' },
                    { boxLabel: 'Item 4', name: 'rb', inputValue: '4' },
                    { boxLabel: 'Item 5', name: 'rb', inputValue: '5' },
                    { boxLabel: 'Item 6', name: 'rb', inputValue: '6' }
                ]
            }]
        }
    }, {
        region: 'center',
        xtype: 'panel',
        ui: 'large',
        title: 'Large Panel',
        layout: {
            type: 'vbox',
            align : 'stretch',
            pack  : 'start'
        },
        items: [
            {
                xtype: 'panel',
                title: 'Inner Panel Two',
                flex: 1
            },
            {
                xtype: 'grid',
                title: 'Simpsons',
                columns: [
                    { text: 'Name',  dataIndex: 'name' },
                    { text: 'Email', dataIndex: 'email', flex: 1 },
                    { text: 'Phone', dataIndex: 'phone' }
                ],
                store: {
                    fields:['name', 'email', 'phone'],
                    data:{'items':[
                        { 'name': 'Lisa',  "email":"lisa@simpsons.com",  "phone":"555-111-1224"  },
                        { 'name': 'Bart',  "email":"bart@simpsons.com",  "phone":"555-222-1234" },
                        { 'name': 'Homer', "email":"home@simpsons.com",  "phone":"555-222-1244"  },
                        { 'name': 'Marge', "email":"marge@simpsons.com", "phone":"555-222-1254"  }
                    ]},
                    proxy: {
                        type: 'memory',
                        reader: {
                            type: 'json',
                            root: 'items'
                        }
                    }
                },
                flex: 2
            },
            {
                xtype: 'panel',
                title: 'Details',
                frame: true,
                flex: 2,
                html: 'some content',

                tools:[
                    {
                        xtype: 'button',
                        glyph: 71,
                        text: 'Action',
                        menu: [{
                            text:'Do One'
                        },{
                            text:'Do Two'
                        },{
                            text:'Do Three'
                        }]
                    }
                ]
            }
        ]
    }]
});