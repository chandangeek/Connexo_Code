/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Pandora.store.RecentSongs', {
    extend: 'Ext.data.Store',
    requires: 'Pandora.model.Song',    
    model: 'Pandora.model.Song'
});