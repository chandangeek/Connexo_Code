/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.model.PortalItem
 *
 * If you have several widgets that need to be shown on a common page, you will need
 * to create a {@link Uni.model.PortalItem} for each widget on that page. E.g. there is
 * an administration page that is filled with several portal widgets.
 *
 * This model class provides the portal item configuration, including title and component.
 *
 * See {@link Uni.controller.Portal} for more detailed information.
 *
 */
Ext.define('Uni.model.PortalItem', {
    extend: 'Ext.data.Model',
    fields: [
        'title',
        'portal',
        'index',
        'items',
        'itemId',
        'afterrender'
    ],
    proxy: {
        type: 'memory'
    }
});