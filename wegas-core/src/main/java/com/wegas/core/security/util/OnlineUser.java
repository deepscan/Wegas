/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2017 School of Business and Engineering Vaud, Comem
 * Licensed under the MIT License
 */
package com.wegas.core.security.util;

import com.wegas.core.security.persistence.User;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Maxence Laurent (maxence laurent gmail.com)
 */
public class OnlineUser implements Serializable {

    private static final long serialVersionUID = -8980828303309755447L;

    private final String fullname;
    private final String email;
    private final String username;
    private final Date connectionDate;
    private final Long userId;
    private final Long mainAccountId;
    private final Long highestRole;

    public OnlineUser(User user, long highestRole) {
        this.fullname = user.getName();
        this.username = user.getMainAccount().getUsername();
        this.email = user.getMainAccount().getEmail();
        this.connectionDate = new Date();
        this.userId = user.getId();
        this.mainAccountId = user.getMainAccount().getId();
        this.highestRole = highestRole;
    }

    public String getFullname() {
        return fullname;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public Date getConnectionDate() {
        return connectionDate;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getMainAccountId() {
        return mainAccountId;
    }

    public Long getHighestRole(){
        return highestRole;
    }
}
