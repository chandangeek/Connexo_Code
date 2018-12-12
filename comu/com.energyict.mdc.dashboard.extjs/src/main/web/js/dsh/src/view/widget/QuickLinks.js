/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                            '<tpl if="href">',
                                '<li><a href="{href}"',
                                    '<tpl if="target">', // yellowfin reports are shown in a new tab.
                                        ' target="{target}"',
                                    '</tpl>',
                                '>{link}</a></li>',
                            '</tpl>',
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
