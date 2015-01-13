/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013, 2014, 2015 School of Business and Engineering Vaud, Comem
 * Licensed under the MIT License
 */
package com.wegas.core.persistence.variable.statemachine;

import com.wegas.core.persistence.AbstractEntity;
import com.wegas.core.persistence.game.Script;
import com.wegas.core.persistence.variable.Searchable;
import com.wegas.core.rest.util.Views;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
//import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.wegas.core.persistence.variable.Scripted;

/**
 *
 * @author Cyril Junod <cyril.junod at gmail.com>
 */
@Entity
@Access(AccessType.FIELD)
//@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes(value = {
    @JsonSubTypes.Type(name = "DialogueTransition", value = DialogueTransition.class)
})
public class Transition extends AbstractEntity implements Searchable, Scripted {

    private static final long serialVersionUID = 1L;
    /**
     *
     */
    @Id
    @GeneratedValue
    @JsonView(Views.IndexI.class)
    private Long id;

    /**
     *
     */
    @JsonView(Views.EditorExtendedI.class)
    private Integer index = 0;

    /**
     *
     */
    private Long nextStateId;

    /**
     *
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "content", column
                = @Column(name = "onTransition_content")),
        @AttributeOverride(name = "lang", column
                = @Column(name = "onTransition_language"))
    })
    @JsonView(Views.EditorExtendedI.class)
    private Script preStateImpact;

    /**
     *
     */
    @Embedded
    private Script triggerCondition;

    @Override
    public Boolean contains(final String criteria) {
        return this.containsAll(new ArrayList<String>() {
            {
                add(criteria);
            }
        });
    }

    @Override
    public Boolean containsAll(final List<String> criterias) {
        if (this.getPreStateImpact() != null && this.getPreStateImpact().containsAll(criterias)) {
            return true;
        }
        return this.getTriggerCondition() != null && this.getTriggerCondition().containsAll(criterias);
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * @return the index
     */
    public Integer getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(Integer index) {
        this.index = index;
    }

    /**
     *
     * @return
     */
    public Long getNextStateId() {
        return nextStateId;
    }

    /**
     *
     * @param nextStateId
     */
    public void setNextStateId(Long nextStateId) {
        this.nextStateId = nextStateId;
    }

    /**
     *
     * @return
     */
    public Script getPreStateImpact() {
        return preStateImpact;
    }

    /**
     *
     * @param preStateImpact
     */
    public void setPreStateImpact(Script preStateImpact) {
        this.preStateImpact = preStateImpact;
    }

    @Override
    public List<Script> getScripts() {
        List<Script> ret = new ArrayList<>();
        ret.add(this.triggerCondition);
        ret.add(this.preStateImpact);
        return ret;
    }

    /**
     *
     * @return
     */
    public Script getTriggerCondition() {
        return triggerCondition;
    }

    /**
     *
     * @param triggerCondition
     */
    public void setTriggerCondition(Script triggerCondition) {
        this.triggerCondition = triggerCondition;
    }

    @Override
    public void merge(AbstractEntity other) {
        Transition newTranstion = (Transition) other;
        this.nextStateId = newTranstion.nextStateId;
        this.preStateImpact = newTranstion.preStateImpact;
        this.triggerCondition = newTranstion.triggerCondition;
        this.index = newTranstion.index;
    }

    @Override
    public String toString() {
        return "Transition{" + "triggerCondition=" + triggerCondition + ", nextStateId=" + nextStateId + '}';
    }
}
