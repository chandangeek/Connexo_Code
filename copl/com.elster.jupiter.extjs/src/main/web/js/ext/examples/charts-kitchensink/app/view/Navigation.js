/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('ChartsKitchenSink.view.Navigation', {
    extend      : 'Ext.tree.Panel',
    xtype       : 'navigation',

    title       : 'Examples',
    rootVisible : false,
    lines       : false,
    useArrows   : true,

    initComponent : function() {
        var me = this;

        if (!me.columns) {
            if (me.initialConfig.hideHeaders === undefined) {
                me.hideHeaders = true;
            }
            me.addCls(me.autoWidthCls);
            me.columns = [{
                xtype     : 'treecolumn',
                text      : 'Name',
                width     : Ext.isIE6 ? '100%' : 10000, // IE6 needs width:100%
                dataIndex : me.displayField,
                scope     : me,
                renderer  : function(value) {
                    var searchString = this.searchField.getValue();

                    if (searchString.length > 0) {
                        return this.strMarkRedPlus(searchString, value);
                    }

                    return value;
                }
            }];
        }

        var store = Ext.create('Ext.data.TreeStore', {
            proxy : {
                type : 'ajax',
                url  : 'resources/data/navigation.json'
            }
        });

        Ext.apply(me, {
            store       : store,
            dockedItems : [
                {
                    xtype           : 'triggerfield',
                    dock            : 'top',
                    enableKeyEvents : true,
                    trigger1Cls     : 'x-form-clear-trigger',
                    trigger2Cls     : 'x-form-search-trigger',

                    onTrigger1Click : function() {
                        this.setValue();
                        me.store.clearFilter();
                    },

                    onTrigger2Click : function() {
                        console.log('filtering ... ' + this.getValue());
                        me.filterStore(this.getValue());
                    },

                    listeners : {
                        keyup : {
                            fn : function(field, event, eOpts) {
                                var value = field.getValue();

                                field.triggerEl.elements[0].setVisible(value.length > 0);

                                this.filterStore(value);
                            },
                            buffer : 100
                        },

                        render : function(field) {
                            this.searchField = field;

                            field.triggerEl.elements[0].hide();
                        },

                        scope  : me
                    }
                }
            ]
        });

        me.callParent(arguments);
    },

    filterStore : function(value) {
        var me           = this,
            store        = me.store,
            searchString = value.toLowerCase();

        if (searchString.length < 1) {
            store.clearFilter();
        } else {
            var v = new RegExp(searchString, 'i');

            store.clearFilter();

            store.filter({
                filterFn: function(node) {
                    var children = node.childNodes,
                        len      = children && children.length,
                        visible  = node.isLeaf() ? v.test(node.get('text')) : false,
                        i;

                    for (i = 0; i < len; i++) {
                        visible = children[i].get('visible');
                        if (visible) {
                            break;
                        }
                    }

                    return visible;
                },
                id: 'titleFilter'
            });
        }
    },

    strMarkRedPlus : function (search, subject) {
        return subject.replace(
            new RegExp( '('+search+')', "gi" ),
            "<span style='color: red;'><b>$1</b></span>"
        );
    }
});