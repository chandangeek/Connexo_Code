/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Pandora.store.SearchResults', {
    extend: 'Ext.data.Store',
    requires: 'Pandora.model.Station',
    model: 'Pandora.model.Station',
    
    sorters: ['name']
});