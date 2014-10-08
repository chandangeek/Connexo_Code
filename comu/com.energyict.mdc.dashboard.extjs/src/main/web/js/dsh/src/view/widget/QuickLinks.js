Ext.define('Dsh.view.widget.QuickLinks', {
    extend: 'Ext.panel.Panel',
    ui: 'tile',
    alias: 'widget.quick-links',
    data: [],
    items: [
        {
            itemId: 'quicklinksTplPanel',
            tpl: new Ext.XTemplate(
                '<div class="quick-links">',
                    '<h3>' + Uni.I18n.translate('overview.widget.quicklinks.title', 'DSH', 'Quick links') + '</h3>',
                '<ul>',
                '<tpl for=".">',
                '<li><a href="{href}">{link}</a></li>',
                '</tpl>',
                '</ul>',
                '</div>'
            )
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
        this.down('#quicklinksTplPanel').data = this.data;
    }
});