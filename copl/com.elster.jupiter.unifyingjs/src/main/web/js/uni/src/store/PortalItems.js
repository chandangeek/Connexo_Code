/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.store.PortalItems
 *
 * If you have several widgets that need to be shown on a common page, you will need
 * to create a {@link Uni.model.PortalItem} for each widget on that page. E.g. there is
 * an administration page that is filled with several portal widgets.
 *
 * This store is used to keep track of the portal items and listen to changes to them in
 * {@link Uni.controller.Portal}.
 *
 * See {@link Uni.controller.Portal} for more detailed information.
 *
 */
Ext.define('Uni.store.PortalItems', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.PortalItem',
    storeId: 'portalItems',
    singleton: true,
    autoLoad: false,
    clearOnPageLoad: false,
    clearRemovedOnLoad: false,

    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
            root: 'items'
        }
    }
});