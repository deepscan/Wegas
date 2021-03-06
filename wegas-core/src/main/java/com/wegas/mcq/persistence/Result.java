/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.mcq.persistence;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.wegas.core.Helper;
import com.wegas.core.ejb.VariableInstanceFacade;
import com.wegas.core.exception.client.WegasIncompatibleType;
import com.wegas.core.i18n.persistence.TranslatableContent;
import com.wegas.core.i18n.persistence.TranslationDeserializer;
import com.wegas.core.persistence.AbstractEntity;
import com.wegas.core.persistence.LabelledEntity;
import com.wegas.core.persistence.game.Script;
import com.wegas.core.persistence.variable.Beanjection;
import com.wegas.core.persistence.variable.Scripted;
import com.wegas.core.persistence.variable.Searchable;
import com.wegas.core.rest.util.Views;
import com.wegas.core.security.util.WegasPermission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.*;

/**
 * @author Francois-Xavier Aeberhard (fx at red-agent.com)
 */
@Entity
@JsonTypeName(value = "Result")
@Table(
        name = "MCQResult",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"choicedescriptor_id", "name"}),
            @UniqueConstraint(columnNames = {"choicedescriptor_id", "label"})
        },
        indexes = {
            @Index(columnList = "choicedescriptor_id"),
            @Index(columnList = "label_id"),
            @Index(columnList = "answer_id"),
            @Index(columnList = "ignorationanswer_id")
        }
)
@NamedQueries({
    @NamedQuery(name = "Result.findByName", query = "SELECT DISTINCT res FROM Result res WHERE res.choiceDescriptor.id=:choicedescriptorId AND res.name LIKE :name")
})
public class Result extends AbstractEntity implements Searchable, Scripted, LabelledEntity {

    private static final long serialVersionUID = 1L;

    @Version
    @Column(columnDefinition = "bigint default '0'::bigint")
    private Long version;

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     *
     */
    @Id
    @GeneratedValue
    @JsonView(Views.IndexI.class)
    private Long id;
    /**
     * Internal Name
     */
    private String name;

    /**
     * Displayed name
     */
    @JsonDeserialize(using = TranslationDeserializer.class)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TranslatableContent label;

    /**
     * Displayed answer when result selected and validated
     */
    @JsonDeserialize(using = TranslationDeserializer.class)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TranslatableContent answer;

    /**
     * Displayed answer when MCQ result not selected and validated
     */
    @JsonDeserialize(using = TranslationDeserializer.class)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TranslatableContent ignorationAnswer;

    /*
     *
     */
    @ElementCollection
    private List<String> files = new ArrayList<>();
    /**
     *
     */
    @Embedded
    @JsonView(Views.EditorI.class)
    private Script impact;
    /**
     *
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "content", column
                = @Column(name = "ignoration_content")),
        @AttributeOverride(name = "lang", column
                = @Column(name = "ignoration_language"))
    })
    @JsonView(Views.EditorI.class)
    private Script ignorationImpact;
    /**
     *
     */
    @ManyToOne
    @JsonBackReference
    private ChoiceDescriptor choiceDescriptor;

    /**
     * This link is here so the reference is updated on remove.
     */
    /*
      @OneToOne(mappedBy = "result", cascade = CascadeType.ALL, orphanRemoval = true)
      @JsonIgnore
      private CurrentResult currentResult;
     */
    /**
     * This field is here so deletion will be propagated to replies.
     */
    /*
    @OneToOne(mappedBy = "result", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Replies replies;
     */
    /**
     *
     */
    public Result() {
    }

    /**
     * @param name
     */
    public Result(String name) {
        this.label = new TranslatableContent();
        this.label.getModifiableTranslations().put("def", name);
        this.name = name;
    }

    /**
     * @param name
     * @param label
     */
    public Result(String name, TranslatableContent label) {
        this.name = name;
        this.label = label;
    }

    public Result(String name, Script impact) {
        this(name, impact, null);
    }

    /**
     *
     * @param name
     * @param impact
     * @param ignorationImpact
     */
    public Result(String name, Script impact, Script ignorationImpact) {
        this(name);
        this.impact = impact;
        this.ignorationImpact = ignorationImpact;
    }

    @Override
    public Boolean containsAll(final List<String> criterias) {
        return Helper.insensitiveContainsAll(this.getName(), criterias)
                || Helper.insensitiveContainsAll(this.getLabel(), criterias)
                || Helper.insensitiveContainsAll(this.getAnswer(), criterias)
                || Helper.insensitiveContainsAll(this.getIgnorationAnswer(), criterias)
                || (this.getImpact() != null && this.getImpact().containsAll(criterias))
                || (this.getIgnorationImpact() != null && this.getIgnorationImpact().containsAll(criterias));
    }

    @Override
    public List<Script> getScripts() {
        List<Script> ret = new ArrayList<>();
        ret.add(this.getImpact());
        if (this.getIgnorationImpact() != null) {
            ret.add(this.getIgnorationImpact());
        }
        return ret;
    }

    /**
     * @param a
     */
    @Override
    public void merge(AbstractEntity a) {
        if (a instanceof Result) {
            Result other = (Result) a;
            this.setVersion(other.getVersion());
            this.setName(other.getName());
            this.setLabel(TranslatableContent.merger(this.getLabel(), other.getLabel()));
            this.setAnswer(TranslatableContent.merger(this.getAnswer(), other.getAnswer()));
            this.setImpact(other.getImpact());
            this.setIgnorationAnswer(TranslatableContent.merger(this.getIgnorationAnswer(), other.getIgnorationAnswer()));
            this.setIgnorationImpact(other.getIgnorationImpact());
            this.setFiles(other.getFiles());
            this.setChoiceDescriptor(other.getChoiceDescriptor());
        } else {
            throw new WegasIncompatibleType(this.getClass().getSimpleName() + ".merge (" + a.getClass().getSimpleName() + ") is not possible");
        }
    }

    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * @return the choiceDescriptor
     */
    @JsonIgnore
    public ChoiceDescriptor getChoiceDescriptor() {
        return choiceDescriptor;
    }

    /**
     * @param choiceDescriptor the choiceDescriptor to set
     */
    public void setChoiceDescriptor(ChoiceDescriptor choiceDescriptor) {
        this.choiceDescriptor = choiceDescriptor;
    }

    /**
     * @return id from the parent choice descriptor
     */
    @JsonView(Views.IndexI.class)
    public Long getChoiceDescriptorId() {
        return choiceDescriptor.getId();
    }

    public void setChoiceDescriptorId(Long id) {
        // NOTHING TO TO....
    }

    /**
     * @return the answer
     */
    public TranslatableContent getAnswer() {
        return answer;
    }

    /**
     * @param answer the answer to set
     */
    public void setAnswer(TranslatableContent answer) {
        this.answer = answer;
        if (this.answer != null) {
            this.answer.setParentDescriptor(this.getChoiceDescriptor());
        }
    }

    /**
     * @return the impact
     */
    public Script getImpact() {
        return impact;
    }

    /**
     * @param impact the impact to set
     */
    public void setImpact(Script impact) {
        this.impact = impact;
    }

    /**
     * @return the ignoration answer
     */
    public TranslatableContent getIgnorationAnswer() {
        return ignorationAnswer;
    }

    /**
     * @param answer the answer to set
     */
    public void setIgnorationAnswer(TranslatableContent answer) {
        this.ignorationAnswer = answer;
        if (this.ignorationAnswer != null) {
            this.ignorationAnswer.setParentDescriptor(this.getChoiceDescriptor());
        }
    }

    /**
     * @return the impact
     */
    public Script getIgnorationImpact() {
        return ignorationImpact;
    }

    /**
     * @param impact the impact to set
     */
    public void setIgnorationImpact(Script impact) {
        this.ignorationImpact = impact;
    }

    /**
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the label
     */
    @Override
    public TranslatableContent getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    @Override
    public void setLabel(TranslatableContent label) {
        this.label = label;
        if (this.label != null) {
            this.label.setParentDescriptor(this.getChoiceDescriptor());
        }
    }

    /**
     * @return the files
     */
    public List<String> getFiles() {
        return files;
    }

    /**
     * @param files the files to set
     */
    public void setFiles(List<String> files) {
        this.files = files;
    }

    /**
     * @return the choiceInstances
     *
     * @JsonIgnore
     * public List<ChoiceInstance> getChoiceInstances() {
     * return currentResult.getChoiceInstances();
     * }
     */

    /*
    public void addChoiceInstance(ChoiceInstance choiceInstance) {
        if (this.currentResult == null) {
            this.currentResult = new CurrentResult();
            this.currentResult.setResult(this);
        }

        if (!this.currentResult.getChoiceInstances().contains(choiceInstance)) {
            this.currentResult.getChoiceInstances().add(choiceInstance);
        }
    }
     */
 /*
    public boolean removeChoiceInstance(ChoiceInstance choiceInstance) {
        return this.currentResult.remove(choiceInstance);
    }
     */

 /*
    public CurrentResult getCurrentResult() {
        return currentResult;
    }
     */
 /*
    public Replies getReplies() {
        return replies;
    }
     */

 /*
    public void addReply(Reply reply) {
        if (replies == null) {
            replies = new Replies();
            replies.setResult(this);
        }
        this.replies.add(reply);
    }
     */

 /*
    void removeReply(Reply reply) {
        this.replies.remove(reply);
    }
     */
    @Override
    public void updateCacheOnDelete(Beanjection beans) {
        VariableInstanceFacade vif = beans.getVariableInstanceFacade();

        // JPA query to fetch ChoiceInstance ci
        Collection<ChoiceInstance> choiceInstances = beans.getQuestionDescriptorFacade().getChoiceInstancesByResult(this);

        // clear currentResult
        for (ChoiceInstance cInstance : choiceInstances) {
            if (cInstance != null) {
                cInstance = (ChoiceInstance) vif.find(cInstance.getId());
                if (cInstance != null) {
                    cInstance.setCurrentResult(null);
                }
            }
        }

        // Destroy replies
        beans.getQuestionDescriptorFacade().cascadeDelete(this);
    }

    @Override
    public Collection<WegasPermission> getRequieredUpdatePermission() {
        return this.getChoiceDescriptor().getRequieredUpdatePermission();
    }

    @Override
    public Collection<WegasPermission> getRequieredReadPermission() {
        return this.getChoiceDescriptor().getRequieredReadPermission();
    }

    /*
    @PrePersist
    private void prePersist() {
        if (replies == null) {
            replies = new Replies();
            replies.setResult(this);
        }
        if (currentResult == null) {
            currentResult = new CurrentResult();
            currentResult.setResult(this);
        }
    }
     */
}
