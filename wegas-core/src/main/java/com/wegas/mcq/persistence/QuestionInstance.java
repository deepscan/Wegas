/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.mcq.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wegas.core.Helper;
import com.wegas.core.exception.client.WegasIncompatibleType;
import com.wegas.core.persistence.AbstractEntity;
import com.wegas.core.persistence.EntityComparators;
import com.wegas.core.persistence.InstanceOwner;
import com.wegas.core.persistence.game.Player;
import com.wegas.core.persistence.variable.VariableInstance;
import static java.lang.Boolean.FALSE;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author Francois-Xavier Aeberhard (fx at red-agent.com)
 */
@Entity
public class QuestionInstance extends VariableInstance {

    private static final long serialVersionUID = 1L;
    //private static final Logger logger = LoggerFactory.getLogger(QuestionInstance.class);

    /**
     *
     */
    private Boolean active = true;
    /**
     *
     */
    private Boolean unread = true;
    /**
     * False until the user has clicked on the global question-wide "submit"
     * button.
     */
    @Column(columnDefinition = "boolean default false")
    private Boolean validated = FALSE;

    /**
     * @param a
     */
    @Override
    public void merge(AbstractEntity a) {
        if (a instanceof QuestionInstance) {
            QuestionInstance other = (QuestionInstance) a;
            super.merge(a);
            this.setActive(other.getActive());
            this.setUnread(other.getUnread());
            Boolean v = other.getValidated();
            this.setValidated(v);
        } else {
            throw new WegasIncompatibleType(this.getClass().getSimpleName() + ".merge (" + a.getClass().getSimpleName() + ") is not possible");
        }
    }

    /**
     * @return the active
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * @return unmodifiable reply list, ordered by createdTime
     */
    @JsonIgnore
    public List<Reply> getSortedReplies() {
        return Helper.copyAndSort(this.getReplies(), new EntityComparators.CreateTimeComparator<>());
    }

    @JsonIgnore
    public List<Reply> getSortedReplies(Player p) {
        return Helper.copyAndSort(this.getReplies(p), new EntityComparators.CreateTimeComparator<>());
    }

    public List<Reply> getReplies(Player p) {
        List<Reply> replies = new ArrayList<>();
        QuestionDescriptor qD = (QuestionDescriptor) this.findDescriptor();

        for (ChoiceDescriptor cd : qD.getItems()) {
            if (this.isDefaultInstance()) {
                replies.addAll(cd.getDefaultInstance().getReplies());
            } else {
                replies.addAll(cd.getInstance(p).getReplies());
            }
        }

        return replies;
    }

    @JsonIgnore
    public List<Reply> getReplies() {
        InstanceOwner owner = this.getOwner();
        return this.getReplies(owner != null ? owner.getAnyLivePlayer() : null);
    }

    public void setReplies(List<Reply> replies) {
    }

    /**
     * @return the unread
     */
    public Boolean getUnread() {
        return this.unread;
    }

    /**
     * @param unread the unread to set
     */
    public void setUnread(Boolean unread) {
        this.unread = unread;
    }

    // ~~~ Sugar ~~~
    /**
     *
     */
    public void activate() {
        this.setActive(true);
    }

    /**
     *
     */
    public void desactivate() {
        this.deactivate();
    }

    /**
     *
     */
    public void deactivate() {
        this.setActive(false);
    }

    /**
     * @param validated the validation status to set
     */
    public void setValidated(Boolean validated) {
        this.validated = validated;
    }

    /**
     * @return The validation status of the question
     */
    public Boolean getValidated() {
        return this.validated;
    }

    /**
     * @param index
     *
     * @return iest choiceDescriptor
     */
    public ChoiceDescriptor item(int index) {
        return ((QuestionDescriptor) this.getDescriptor()).item(index);
    }

    @JsonIgnore
    public boolean isSelectable() {
        QuestionDescriptor qd = (QuestionDescriptor) this.findDescriptor();
        Integer maxReplies = qd.getMaxReplies();
        // the question must be selectable
        boolean selectable = (qd.getCbx() && !this.getValidated()) // a not yet validated cbx question
                || maxReplies == null // OR number of answers is unlimited
                || this.getReplies().size() < maxReplies; // OR maximum number not reached
        if (selectable) {
            //and at least one choice should bee selectable too
            InstanceOwner owner = this.getOwner();
            Player p = owner != null ? owner.getAnyLivePlayer() : null;

            for (ChoiceDescriptor cd : qd.getItems()) {
                if (cd.isSelectable(p)) {
                    // at least 1 choice is still selectable -> OK
                    return true;
                }
            }
            // no selectable left
            return false;
        }
        return selectable;
    }
}
