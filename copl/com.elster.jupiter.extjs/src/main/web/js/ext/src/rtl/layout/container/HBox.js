/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.layout.container.HBox', {
    override: 'Ext.layout.container.HBox',

    rtlNames: {
        beforeX: 'right',
        afterX: 'left',
        getScrollLeft: 'rtlGetScrollLeft',
        setScrollLeft: 'rtlSetScrollLeft',
        scrollTo: 'rtlScrollTo',
        beforeScrollerSuffix: '-after-scroller',
        afterScrollerSuffix: '-before-scroller'
    }
});