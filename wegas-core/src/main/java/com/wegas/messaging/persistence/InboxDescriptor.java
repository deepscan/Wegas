/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.messaging.persistence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wegas.core.i18n.persistence.TranslatableContent;
import com.wegas.core.persistence.AbstractEntity;
import com.wegas.core.persistence.game.Player;
import com.wegas.core.persistence.variable.VariableDescriptor;
import java.util.List;
import static java.lang.Boolean.FALSE;
import javax.persistence.Column;
import javax.persistence.Entity;
import jdk.nashorn.api.scripting.JSObject;

/**
 *
 * @author Francois-Xavier Aeberhard (fx at red-agent.com)
 */
@Entity
@JsonIgnoreProperties(value = {"description"})
public class InboxDescriptor extends VariableDescriptor<InboxInstance> {

    private static final long serialVersionUID = 1L;

    /**
     * Tells if the inbox has a capacity of just one message.
     */
    @Column(columnDefinition = "boolean default false")
    private Boolean capped = FALSE;

    /**
     * Merge descriptor
     *
     * @param a descriptor to read value from
     */
    @Override
    public void merge(AbstractEntity a) {
        super.merge(a);
        InboxDescriptor other = (InboxDescriptor) a;
        this.setCapped(other.getCapped());
    }

    /**
     * @return the limited capacity
     */
    public Boolean getCapped() {
        return capped;
    }

    /**
     * @param capped the capacity limitation to set
     */
    public void setCapped(Boolean capped) {
        this.capped = capped;
    }

    public Message sendMessage(Player p, Message message) {
        this.getInstance(p).sendMessage(message);
        return message;
    }
    /**
     *
     * Sugar to be used from scripts.
     *
     * @param p       message recipient
     * @param from    sender
     * @param subject message subject
     * @param body    message body
     *
     * @return The sent message
     *
     * @see Message
     */
    public Message sendMessage(Player p, String from, String subject, String body) {
        return this.getInstance(p).sendMessage(from, subject, body);
    }

    /**
     *
     * Sugar to be used from scripts.
     *
     * @param p       message recipient
     * @param from    message sender
     * @param subject message subject
     * @param body    message body
     * @param token   internal message identifier (can be used within a
     *                {@link #isTokenMarkedAsRead script condition} to check whether or not
     *                message has been read)
     *
     * @return The sent message
     *
     * @see Message
     */
    public Message sendMessageWithToken(Player p, String from, String subject, String body, String token) {
        return this.getInstance(p).sendMessage(from, subject, body, null, token, null);
    }

    /**
     *
     * Sugar to be used from scripts.
     *
     * @param p       message recipient
     * @param from    message sender
     * @param subject message subject
     * @param body    message body
     * @param date    the date the message has been sent (free text, eg. 'Monday
     *                Morning', 'may the 4th', 'thrid period', and so on)
     *
     * @return The sent message
     *
     * @see Message
     */
    public Message sendDatedMessage(Player p, String from, String date, String subject, String body) {
        return this.getInstance(p).sendMessage(from, subject, body, date);
    }

    /**
     *
     * Sugar to be used from scripts.
     *
     * @param p            message recipient
     * @param from         message sender
     * @param subject      message subject
     * @param body         message body
     * @param attachments
     *
     * @return {@link Message} the sent message
     */
    public Message sendMessage(Player p, String from, String subject, String body, List<String> attachments) {
        return this.getInstance(p).sendMessage(from, subject, body, attachments);
    }

    /**
     *
     * Sugar to be used from scripts.
     *
     * @param p            message recipient
     * @param from         message sender
     * @param subject      message subject
     * @param body         message body
     * @param date         the date the message has been sent (free text, eg. 'Monday
     *                     Morning', 'may the 4th', 'thrid period', and so on)
     * @param attachments
     *
     * @return {@link Message} the sent message
     */
    public Message sendDatedMessage(Player p, String from, String date, String subject, String body, List<String> attachments) {
        return this.getInstance(p).sendMessage(from, subject, body, date, attachments);
    }

    /**
     *
     * Sugar to be used from scripts.
     *
     * @param p            message recipient
     * @param from         message sender
     * @param subject      message subject
     * @param body         message body
     * @param date         the date the message has been sent (free text, eg. 'Monday
     *                     Morning', 'may the 4th', 'thrid period', and so on)
     * @param token        internal message identifier (can be used within a
     *                     {@link #isTokenMarkedAsRead script condition} to check whether or not
     *                     message has been read)
     * @param attachments
     *
     * @return {@link Message} the sent message
     */
    public Message sendMessage(Player p, String from, String date, String subject, String body, String token, List<String> attachments) {
        return this.getInstance(p).sendMessage(from, subject, body, date, token, attachments);
    }

    /**
     *
     * I18n Sugar to be used from scripts.
     *
     * @param p            message recipient
     * @param from         message sender
     * @param subject      message subject
     * @param body         message body
     * @param date         the date the message has been sent (free text, eg. 'Monday
     *                     Morning', 'may the 4th', 'thrid period', and so on)
     * @param token        internal message identifier (can be used within a
     *                     {@link #isTokenMarkedAsRead script condition} to check whether or not
     *                     message has been read)
     * @param attachments
     *
     * @return
     */
    public Message sendMessage(Player p, TranslatableContent from, TranslatableContent date, TranslatableContent subject,
            TranslatableContent body, String token, List<Attachment> attachments) {
        return this.getInstance(p).sendMessage(from, subject, body, date, token, attachments);
    }

    public Message sendMessage(Player p, JSObject from, JSObject date, JSObject subject,
            JSObject body, String token, List<JSObject> attachments) {
        return this.getInstance(p).sendMessage(from, subject, body, date, token, attachments);
    }

    /**
     *
     * @param p
     *
     * @return check if the given player's inbox is empty
     */
    public boolean isEmpty(Player p) {
        return this.getInstance(p).getMessages().isEmpty();
    }

    /**
     *
     * @param player {@link Player}
     *
     * @return int unread message count for given player
     */
    public int getUnreadCount(Player player) {
        return this.getInstance(player).getUnreadCount();
    }

    /**
     * Check message read status
     *
     * @param self
     * @param token
     *
     * @return true is a message identified by the token exists and has been
     *         read, false otherwise
     */
    public boolean isTokenMarkedAsRead(Player self, String token) {
        return this.getInstance(self).isTokenMarkedAsRead(token);
    }

}
