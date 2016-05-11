package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.util.sql.SqlFragment;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Provides an implementation for the {@link SqlFragment} interface
 * that merely holds a String.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-04 (13:47)
 */
public class TextFragment implements SqlFragment {
    private final String text;

    public TextFragment(String text) {
        this.text = text;
    }

    @Override
    public int bind(PreparedStatement statement, int position) throws SQLException {
        // Nothing to bind
        return position;
    }

    @Override
    public String getText() {
        return this.text;
    }

}