Ext.define('ViewDataCollectionIssues.view.ViewDataCollectionIssuesItem', {
    extend: 'Ext.panel.Panel',
    xtype: 'view-data-collection-issues-item',
    emptyText: '',
    issueTable: {
        labelWidth: 200,
        labelMargin: '0 20 0 0',
        rowMargin: '15 0 15 0'
    },
    items: [{
        html: '<h2>No issue selected</h2><p>Select an issue to view its detail.</p>',
        border: false
    }],

    initComponent: function () {
        var self = this;

        self.callParent();
        self.addEvents('change');
        self.on('change', this.onChange, this);
    },

    onChange: function (panel, data) {
        var self = this;

        self.removeAll();
        self.add(self.getItems(data));
    },

    getItems: function (data) {
        var self = this;

        return {
            border: false,
            defaults: {
                border: false
            },
            items: [{
                xtype: 'toolbar',
                padding: 10,
                ui: 'footer',
                items: [{
                    xtype: 'container',
                    flex: 1,
                    html: data.reason + (data.device ? ' to ' + data.device.sNumber : '')
                },{
                    xtype: 'button',
                    text: 'Actions',
                    disabled: true
                }]
            },{
                layout:'hbox',
                defaults: {
                    border: false,
                    flex: 1
                },
                margin: self.issueTable.rowMargin,
                items: [{                    
                    layout:'hbox',
                    defaults: {
                        border: false,
                    },
                    items: [{
                        layout: {
                            type: 'hbox',
                            pack: 'end'
                        },
                        width: self.issueTable.labelWidth,
                        margin: self.issueTable.labelMargin,
                        items: [{
                            html: '<b>Customer:</b>',
                            border: false
                        }]
                    },{
                        html: data.customer ? data.customer : self.emptyText,
                        flex: 1
                    }]
                },{                    
                    layout:'hbox',
                    defaults: {
                        border: false,
                    },
                    items: [{
                        layout: {
                            type: 'hbox',
                            pack: 'end'
                        },
                        width: self.issueTable.labelWidth,
                        margin: self.issueTable.labelMargin,
                        items: [{
                            html: '<b>Status:</b>',
                            border: false
                        }]
                    },{
                        html: data.status ? data.status : self.emptyText,
                        flex: 1
                    }]
                }]
            },{
                layout:'hbox',
                defaults: {
                    border: false,
                    flex: 1
                },
                margin: self.issueTable.rowMargin,
                items: [{                    
                    layout:'hbox',
                    defaults: {
                        border: false,
                    },
                    items: [{
                        layout: {
                            type: 'hbox',
                            pack: 'end'
                        },
                        width: self.issueTable.labelWidth,
                        margin: self.issueTable.labelMargin,
                        items: [{
                            html: '<b>Location:</b>',
                            border: false
                        }]
                    },{
                        html: data.location ? data.location : self.emptyText,
                        flex: 1
                    }]
                },{                    
                    layout:'hbox',
                    defaults: {
                        border: false,
                    },
                    items: [{
                        layout: {
                            type: 'hbox',
                            pack: 'end'
                        },
                        width: self.issueTable.labelWidth,
                        margin: self.issueTable.labelMargin,
                        items: [{
                            html: '<b>Due date:</b>',
                            border: false
                        }]
                    },{
                        html: data.dueDate ? Ext.Date.format(new Date(data.dueDate), 'M d, Y h:m') : self.emptyText,
                        flex: 1
                    }]
                }]
            },{
                layout:'hbox',
                defaults: {
                    border: false,
                    flex: 1
                },
                margin: self.issueTable.rowMargin,
                items: [{                    
                    layout:'hbox',
                    defaults: {
                        border: false,
                    },
                    items: [{
                        layout: {
                            type: 'hbox',
                            pack: 'end'
                        },
                        width: self.issueTable.labelWidth,
                        margin: self.issueTable.labelMargin,
                        items: [{
                            html: '<b>Usage point:</b>',
                            border: false
                        }]
                    },{
                        html: data.usagePoint ? data.usagePoint : self.emptyText,
                        flex: 1
                    }]
                },{                    
                    layout:'hbox',
                    defaults: {
                        border: false,
                    },
                    items: [{
                        layout: {
                            type: 'hbox',
                            pack: 'end'
                        },
                        width: self.issueTable.labelWidth,
                        margin: self.issueTable.labelMargin,
                        items: [{
                            html: '<b>Assignee:</b>',
                            border: false
                        }]
                    },{
                        html: data.assignee ? data.assignee.title : 'None',
                        flex: 1
                    }]
                }]
            },{
                layout:'hbox',
                defaults: {
                    border: false,
                    flex: 1
                },
                margin: self.issueTable.rowMargin,
                items: [{                    
                    layout:'hbox',
                    defaults: {
                        border: false,
                    },
                    items: [{
                        layout: {
                            type: 'hbox',
                            pack: 'end'
                        },
                        width: self.issueTable.labelWidth,
                        margin: self.issueTable.labelMargin,
                        items: [{
                            html: '<b>Device:</b>',
                            border: false
                        }]
                    },{
                        html: data.device ? data.device : self.emptyText,
                        flex: 1
                    }]
                },{                    
                    layout:'hbox',
                    defaults: {
                        border: false,
                    },
                    items: [{
                        layout: {
                            type: 'hbox',
                            pack: 'end'
                        },
                        width: self.issueTable.labelWidth,
                        margin: self.issueTable.labelMargin,
                        items: [{
                            html: '<b>Creation date:</b>',
                            border: false
                        }]
                    },{
                        html: data.creationDate ? Ext.Date.format(new Date(data.creationDate), 'M d, Y h:m') : self.emptyText,
                        flex: 1
                    }]
                }]
            },{
                layout: 'hbox',
                defaults: {
                    border: false,
                },
                margin: self.issueTable.rowMargin,
                items: [{
                    layout: {
                        type: 'hbox',
                        pack: 'end'
                    },
                    width: self.issueTable.labelWidth,
                    margin: self.issueTable.labelMargin,
                    items: [{
                        html: '<b>Service category:</b>',
                        border: false
                    }]
                },{
                    html: data.serviceCategory ? data.serviceCategory : self.emptyText,
                    flex: 1
                }]
            },{
                layout: {
                    type: 'hbox',
                    pack: 'end'
                },
                items: [{
                    xtype: 'button',
                    text: 'View details',
                    margin: '0 20 20 0',
                    cls: Ext.baseCSSPrefix + 'btn-plain-toolbar-medium',
                    disabled: true
                }]
            }]
        }
    }
});