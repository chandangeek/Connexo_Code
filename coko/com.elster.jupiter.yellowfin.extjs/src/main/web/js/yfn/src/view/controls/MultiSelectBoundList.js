/**
 * Created by Lucian on 12/12/2014.
 */
Ext.define('Yfn.view.controls.MultiSelectBoundList', {
    extend: 'Ext.view.BoundList',
    alias: 'widget.multi-select-boundlist',
    alternateClassName: 'Yfn.MultiSelectBoundList',
    requires: ['Ext.view.BoundList', 'Yfn.view.controls.MultiSelectBoundListLayout'],
    childEls: [
        'listEl', 'footerEl'
    ],

    componentLayout: 'multiselectboundlist',


    listeners: {
        'selectionchange': function() {
            if (this.selModel.selected.length > 0) {
                Ext.getCmp(this.id + '-clear-all-button').enable();
            } else {
                Ext.getCmp(this.id + '-clear-all-button').setDisabled(true);
            }
        }
    },

    getRefItems: function() {
        var results = [];
        result.push(me.selectToolbar);
        return results;
    },

    renderTpl: [
        '<div id="{id}-listEl" role="presentation" class="{baseCls}-list-ct ', Ext.dom.Element.unselectableCls, '" style="overflow:auto"></div>',
        '{%',
        'var me=values.$comp, pagingToolbar=me.pagingToolbar;',
        'if (pagingToolbar) {',
        'pagingToolbar.ownerLayout = me.componentLayout;',
        'Ext.DomHelper.generateMarkup(pagingToolbar.getRenderTree(), out);',
        '}',
        '%}',
        '{%',
        'var me=values.$comp, selectToolbar=me.selectToolbar;',
        'selectToolbar.ownerLayout = me.componentLayout;',
        'Ext.DomHelper.generateMarkup(selectToolbar.getRenderTree(), out);',
        '%}',
        {
            disableFormats: true
        }
    ],

    finishRenderChildren: function () {
        this.callParent(arguments);

        if (this.selectToolbar) {
            this.selectToolbar.finishRender();
        }
    },

    createSelectToolbar: function() {
        var combo = this.pickerField;
        return Ext.widget('toolbar', {
            id: this.id + '-select-toolbar',
            border: false,
            style: {
                'border-top': '1px solid #dddddd !important'
            },
            ownerCt: this,
            ownerLayout: this.getComponentLayout(),
            items:[
                {
                    xtype: 'tbfill'
                },
                {
                    xtype: 'container',
                    items: [
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.selectAll','YFN','Select All'),
                            handler: function(btn, e) {
                                combo.select(combo.getStore().getRange());

                                var dynIdClearButton = btn.up('container').up('toolbar').items.items[2].items.items[0].id;
                                Ext.getCmp(dynIdClearButton).enable();
                                //combo.setSelectedCount(combo.getStore().getRange().length);
                                e.stopEvent();
                            }
                        }
                    ]
                },
                {
                    xtype: 'container',
                    items: [
                        {
                            xtype: 'button',
                            id: this.id + '-clear-all-button',
                            text: Uni.I18n.translate('general.clearAll','YFN','Clear All'),
                            cls:'x-btn-default-small',
                            disabled: true,
                            handler: function(btn, e) {
                                combo.select([]);
                                //combo.setSelectedCount(0);
                                //combo.reset();
                                e.stopEvent();
                            }
                        }
                    ]
                },
                {
                    xtype: 'tbfill'
                }
            ]
        });
    },


    refresh: function(){
        var me = this,
            rendered = me.rendered;

        this.callParent(arguments);

        if (rendered && this.selectToolbar && this.selectToolbar.rendered && !me.preserveScrollOnRefresh) {
            me.el.appendChild(this.selectToolbar.el);
        }

        if (rendered && Ext.isIE6 && Ext.isStrict) {
            me.listEl.repaint();
        }
    },

    initComponent: function () {
        var me = this;
        if(me.pickerField && me.pickerField.multiSelect)
            me.selectToolbar = me.createSelectToolbar();
        me.callParent();
    }
});
