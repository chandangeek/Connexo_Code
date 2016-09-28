Ext.define('Mdc.property.UsagePoint', {
    extend: 'Uni.property.view.property.Base',

    requires: [
        'Mdc.store.UsagePointsForDeviceAttributes'
    ],

    getEditCmp: function () {
        var me = this,
            store = Ext.getStore('Mdc.store.UsagePointsForDeviceAttributes');

        store.load();
        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            store: store,
            typeAhead: true,
            typeAheadDelay: 100,
            minChars: 0,
            queryMode: 'remote',
            displayField: 'name',
            valueField: 'id',
            queryParam: 'like',
            width: me.width,
            forceSelection: true,
            readOnly: me.isReadOnly,
            blankText: me.blankText,
            listConfig: {
                pageSize: store.pageSize,
                refresh: function () {
                    var me = this,
                        toolbar = me.pagingToolbar;

                    me.superclass.refresh.call(me);
                    if (me.rendered && toolbar && toolbar.rendered && !me.preserveScrollOnRefresh) {
                        me.el.appendChild(toolbar.el);
                    }

                    if (me.rendered) {
                        me.el.last().setVisible(me.getStore().getTotalCount() > me.pageSize);
                    }
                },
                createPagingToolbar: function () {
                    var list = this;
                    return Ext.widget('container', {
                        id: list.id + '-paging-toolbar',
                        bindStore: function (store) {
                            if (store) {
                                store.on('load', list.refresh.bind(list));
                            }
                        },
                        cls: Ext.baseCSSPrefix + 'boundlist-item combo-limit-notification',
                        html: Uni.I18n.translate('general.combobox.narrow', 'MDC', 'Keep typing to narrow down'),
                        ownerCt: list,
                        ownerLayout: list.getComponentLayout()
                    });
                }
            }
        }
    },

    getField: function () {
        return this.down('combobox');
    },

    markInvalid: function (error) {
        this.down('combobox').markInvalid(error);
    },

    clearInvalid: function (error) {
        this.down('combobox') && this.down('combobox').clearInvalid();
    }
});