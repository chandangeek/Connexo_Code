Ext.define('Dsh.view.widget.HeaderSection', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.header-section',
    itemId: 'header-section',
    layout: 'fit',
    router: null,
    ui: 'large',

    initComponent: function () {
        var me = this;
        me.title = me.router.getRoute().title;
        this.items = [
            {
                xtype: 'toolbar',
                items: [
                    {
                        xtype: 'container',
                        html: Uni.I18n.translate('overview.widget.headerSection.deviceGroupLabel', 'DSH', 'For device group'),
                        style: 'margin-right: 10px'
                    },
                    {
                        xtype: 'combobox',
                        displayField: 'name',
                        forceSelection: true,
                        valueField: 'id',
                        store: 'Dsh.store.filter.DeviceGroup',
                        router: me.router,
                        listeners: {
                            change: function (cmp, value) {
                                this.router.filter.set('deviceGroup', value);
                                this.router.filter.save();
                            }
                        }
                    },
                    '->',
                    {
                        xtype: 'displayfield',
                        itemId: 'last-updated-field',
                        style: 'margin-right: 10px'
                    },
                    {
                        xtype: 'button',
                        itemId: 'refresh-btn',
                        ui: 'action',
                        text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'DSH', 'Refresh'),
                        icon: '/apps/sky/resources/images/form/restore.png'
                    }
                ]
            }
        ];
        this.callParent(arguments);

        var combo = this.down('combobox');
        combo.getStore().load(function(){
            combo.setValue(me.router.filter.get('deviceGroup'));
        });
    }
});