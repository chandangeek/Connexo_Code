/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.search.Basic
 *
 * Temporary simple search button while features/stories are being developed.
 * Also see: http://jira.eict.vpdc/browse/JP-651
 */
Ext.define('Uni.view.search.Basic', {
    extend: 'Ext.button.Button',
    alias: 'widget.searchBasic',
    itemId: 'searchButton',
    cls: 'search-button',
    glyph: 'xe021@icomoon',
    scale: 'small'
});