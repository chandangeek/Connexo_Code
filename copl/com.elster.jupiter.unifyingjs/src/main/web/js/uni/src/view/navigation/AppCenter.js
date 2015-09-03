/**
 * @class Uni.view.navigation.AppCenter
 */
Ext.define('Uni.view.navigation.AppCenter', {
    extend: 'Ext.button.Button',
    xtype: 'uni-nav-appcenter',

    text: '',
    scale: 'medium',
    iconCls: 'icon-menu3',
    cls: Uni.About.baseCssPrefix + 'nav-appcenter',

    menu: {
        xtype: 'menu',
        plain: true,
        showSeparator: false,
        forceLayout: true,
        cls: Uni.About.baseCssPrefix + 'nav-appcenter-menu',
        items: [
            {
                xtype: 'dataview',
                cls: Uni.About.baseCssPrefix + 'nav-appcenter-dataview',
                tpl: [
                    '<div class="handlebar"></div>',
                    '<tpl for=".">',
                    '<a href="{url}"',
                    '<tpl if="isExternal"> target="_blank"</tpl>',
                    '>',
                    '<div class="app-item',
                    '<tpl if="isActive"> x-pressed</tpl>',
                    '">',
                    '<div class="icon uni-icon-{icon}">&nbsp;</div>',
                    '<span class="name">{name}</span>',
                    '</div>',
                    '</a>',
                    '</tpl>'
                ],
                itemSelector: 'div.app-item',
                store: 'apps',
                listeners: {
                    itemclick: function (dataview, record) {
                        if (record.get('isActive')) {
                            dataview.up('menu').hide();
                        }
                    }
                }
            }
        ]
    },

    initComponent: function () {
        this.callParent(arguments);
    }
});