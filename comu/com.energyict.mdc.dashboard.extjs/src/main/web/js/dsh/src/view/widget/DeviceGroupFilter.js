Ext.define('Dsh.view.widget.DeviceGroupFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.device-group-filter',
    layout: 'hbox',

    initComponent: function () {
        var me = this;
        var store = Ext.getStore('Dsh.store.filter.DeviceGroup' || 'ext-empty-store');

        this.items = [
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
                label: Uni.I18n.translate('general.deviceGroup', 'DSH', 'Device group') + ': ',
                arrowAlign: 'right',
                groupName:null, // yellowfin reports use names instead of id
                menuAlign: 'tl-bl',
                menu: {
                    enableScrolling: true,
                    maxHeight: 350,
                    itemId: 'mnu-device-group-filter',
                    router: me.router,
                    listeners: {
                        click: function (cmp, item) {
                            this.router.filter.set('deviceGroup', item.value);
                            this.router.filter.save();
                            this.groupName = Ext.isNumber(item.value) ? item.text: null;
                        }
                    }
                },
                setValue: function(value) {
                    var item = this.menu.items.findBy(function(item){return item.value == value});
                    if (item) {
                        item.setActive();
                        this.setText(this.label + item.text);
                        this.groupName = Ext.isNumber(item.value) ? Ext.String.htmlEncode(item.text): null;
                        this.fireEvent('change', this); // the event is handled by CommOverview and ConnOverview controllers to update quick links.
                    }

                }
            }
        ];

        this.callParent(arguments);

        var button = me.down('#device-group');
        Ext.suspendLayouts();

        store.load(function () {
            var menu = button.menu;
            menu.removeAll();
            menu.add({
                text: Uni.I18n.translate('overview.widget.headerSection.none', 'DSH', 'None'),
                value: ''
            });

            store.each(function (item) {
                menu.add({
                    text: Ext.String.htmlEncode(item.get('name')),
                    value: item.get('id')
                })
            });

            button.setValue(me.router.filter.get('deviceGroup'));
        });

        Ext.resumeLayouts(true);
    }
});
