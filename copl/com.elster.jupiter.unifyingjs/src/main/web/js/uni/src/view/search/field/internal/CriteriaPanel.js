/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.internal.CriteriaPanel', {
    extend: 'Ext.panel.Panel',
    xtype: 'uni-search-internal-criteriapanel',
    config: {
        service: null,
        sticky: false
    },
    flex: 1,
    defaults: {
        margin: '0 10 10 0'
    },
    layout: 'column',

    initComponent: function() {
        var me = this,
            service = me.getService(),
            listeners = service.on({
                reset:  me.onReset,
                add:    me.onCriteriaAdd,
                remove: me.onCriteriaRemove,
                scope:  me,
                destroyable: true
            });


        me.callParent(arguments);
        me.on('destroy', function () {
            listeners.destroy();
        });
    },

    onReset: function() {
        var me = this;

        Ext.suspendLayouts();
        me.removeAll();
        if (!me.sticky) {
            me.dockedItems.each(function(item){
                me.removeDocked(item);
            });
        }

        me.setVisible(false);
        Ext.resumeLayouts(true);
    },

    onCriteriaAdd: function(filters, property) {
        var me = this,
            button = me.service.createCriteriaButton(property);

        if (property.get('sticky') === me.sticky) {
            Ext.suspendLayouts();
            if (property.get('group')) {
                var group = property.get('group');
                var dock = me.down('panel[group="'+ group.id+'"]');
                if (!dock) {
                    dock = me.addDocked({
                        xtype: 'panel',
                        group: group.id,
                        dock: 'bottom',
                        layout: 'column',
                        header: {
                            style: {
                                background: 'transparent',
                                padding: '0 0 5 0'
                            }
                        },
                        defaults: me.defaults,
                        title: group.displayValue
                    })[0];
                }

                dock.add(button);
            } else {
                me.add(button);
            }

            me.setVisible(me.items.length + me.dockedItems.length);
            Ext.resumeLayouts(true);
        }
    },

    onCriteriaRemove: function(filters, property) {
        var me = this,
            filter = me.down('[dataIndex =' + property.getId() + ']');

        if (property.get('sticky') === this.sticky) {
            filter.destroy();

            if (property.get('group')) {
                var group = property.get('group'),
                    panel = this.down('panel[group="'+ group.id+'"]');

                if (panel && !panel.items.length) {
                    this.removeDocked(panel);
                }
            }
        }

        this.setVisible(this.items.length + this.dockedItems.length);
    }
});