/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.override.panel.Tool', {
    override: 'Ext.panel.Tool',

    renderTpl: [
        '<div id="{id}-toolEl" src="{blank}" class="{baseCls}-img {baseCls}-{type}' +
        '{childElCls}" role="presentation"></div>'
    ]
});