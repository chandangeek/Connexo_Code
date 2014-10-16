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
        var store = Ext.getStore('Dsh.store.filter.DeviceGroup' || 'ext-empty-store');
        this.items = [
            {
                xtype: 'toolbar',
                items: [
                    {
                        xtype: 'container',
                        html: Uni.I18n.translate('overview.widget.headerSection.filter', 'DSH', 'Filter'),
                        cls: 'x-form-display-field',
                        style: {
                            paddingRight: '10px'
                        }
                    },
                    {
                        xtype: 'button',
                        style: {
                            'background-color': '#71adc7'
                        },
                        itemId: 'device-group',
                        label: Uni.I18n.translate('overview.widget.headerSection.deviceGroupLabel', 'DSH', 'Device group') + ': ',
                        arrowAlign: 'right',
                        menu: {
                            router: me.router,
                            listeners: {
                                click: function (cmp, item) {
                                    this.router.filter.set('deviceGroup', item.value);
                                    this.router.filter.save();
                                }
                            }
                        },
                        setValue: function(value) {
                            var item = this.menu.items.findBy(function(item){return item.value == value});
                            if (item) {
                                item.setActive();
                                this.setText(this.label + item.text);
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
                        style: {
                            'background-color': '#71adc7'
                        },
                        text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'DSH', 'Refresh'),
                        icon: '/apps/sky/resources/images/form/restore.png'
                    }
                ]
            }
        ];
        this.callParent(arguments);

        var button = me.down('#device-group');
        store.load(function () {
            var menu = button.menu;
            menu.removeAll();
            menu.add({
                text: Uni.I18n.translate('overview.widget.headerSection.none', 'DSH', 'None'),
                value: ''
            });

            store.each(function (item) {
                menu.add({
                    text: item.get('name'),
                    value: item.get('id')
                })
            });

            button.setValue(me.router.filter.get('deviceGroup'));
        });
    }
});