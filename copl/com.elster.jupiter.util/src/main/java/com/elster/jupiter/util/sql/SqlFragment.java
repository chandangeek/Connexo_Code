package com.elster.jupiter.util.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface SqlFragment {

	int bind(PreparedStatement statement , int position) throws SQLException;

    String getText();
}
