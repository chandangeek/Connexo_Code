Ext.define('Dsh.view.widget.QuickLinks', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.quick-links',
    itemId: 'quick-links',
    data: [],
    items: [
        {
            itemId: 'quicklinksTplPanel',
            tpl: new Ext.XTemplate(
                '<div>',
                    '<h3>Quick links</h3>', //TODO: localize
                    '<ul style="list-style: none; padding: 0; margin-top: 5px">',
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