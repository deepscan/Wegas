/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.rest.util;

import com.wegas.core.persistence.JsonSerializable;
import com.wegas.core.persistence.game.Player;

/**
 *
 * @author Maxence Laurent (maxence.laurent at gmail.com)
 */
public class Email implements JsonSerializable {

    private String subject;
    private String from;
    private String replyTo;
    private String body;
    private Player[] to;

    public Email() {
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Player[] getTo() {
        return to;
    }

    public void setTo(Player[] to) {
        this.to = to;
    }
}
