package com.elster.jupiter.http.whiteboard;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * UserJWT that's a DTO that stores information
 * about token and it's relation to a possible user.
 * <p>
 * A case when userId is null is possible, when we create a token
 * for a 3rd party service
 */
public final class UserJWT {

    @Size(max = Table.UUID_LENGTH)
    private String jwtId;

    private BigDecimal userId;

    @Size(max = Table.MAX_STRING_LENGTH)
    private String token;

    private Instant creationDate;

    private Instant expirationDate;

    private final DataModel dataModel;

    @Inject
    public UserJWT(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public UserJWT init(final String jwtId, final BigDecimal userId, final String token, final Instant expirationDate) {
        this.jwtId = jwtId;
        this.userId = userId;
        this.token = token;
        this.expirationDate = expirationDate;
        return this;
    }

    public String getJwtId() {
        return jwtId;
    }

    public void setJwtId(String jwtId) {
        this.jwtId = jwtId;
    }

    public BigDecimal getUserId() {
        return userId;
    }

    public void setUserId(BigDecimal userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Instant expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void save() {
        Save.CREATE.save(dataModel, this);
        dataModel.update(this);
    }

    public void update() {
        dataModel.mapper(UserJWT.class).update(this);
    }

    public void delete() {
        dataModel.mapper(UserJWT.class).remove(this);
    }
}
