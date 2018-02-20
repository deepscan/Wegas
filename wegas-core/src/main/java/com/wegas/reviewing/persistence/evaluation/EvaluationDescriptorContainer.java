/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.reviewing.persistence.evaluation;

import com.wegas.core.exception.client.WegasIncompatibleType;
import com.wegas.core.persistence.AbstractEntity;
import com.wegas.core.persistence.ListUtils;
import com.wegas.core.persistence.views.Views;
import com.wegas.core.persistence.views.WegasJsonView;
import com.wegas.core.security.util.WegasPermission;
import com.wegas.reviewing.persistence.PeerReviewDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple wrapper to group several evaluation descriptor
 *
 * @author Maxence Laurent (maxence.laurent gmail.com)
 * @see EvaluationDescriptor
 * @see PeerReviewDescriptor
 */
@Entity
public class EvaluationDescriptorContainer extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(EvaluationDescriptorContainer.class);

    @Id
    @GeneratedValue
    @WegasJsonView(Views.IndexI.class)
    private Long id;

    /**
     * List of evaluations
     */
    @OneToMany(mappedBy = "container", cascade = CascadeType.ALL, orphanRemoval = true)
    @WegasJsonView(Views.EditorI.class)
    @NotNull
    private List<EvaluationDescriptor> evaluations = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "feedback")
    @JsonbTransient
    private PeerReviewDescriptor fbPeerReviewDescriptor;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "fbComments")
    @JsonbTransient
    private PeerReviewDescriptor commentsPeerReviewDescriptor;

    /**
     * Empty constructor
     */
    public EvaluationDescriptorContainer() {
        super();
    }

    public PeerReviewDescriptor getFbPeerReviewDescriptor() {
        return fbPeerReviewDescriptor;
    }

    public void setFbPeerReviewDescriptor(PeerReviewDescriptor fbPeerReviewDescriptor) {
        this.fbPeerReviewDescriptor = fbPeerReviewDescriptor;
    }

    public PeerReviewDescriptor getCommentsPeerReviewDescriptor() {
        return commentsPeerReviewDescriptor;
    }

    public void setCommentsPeerReviewDescriptor(PeerReviewDescriptor commentsPeerReviewDescriptor) {
        this.commentsPeerReviewDescriptor = commentsPeerReviewDescriptor;
    }

    /**
     * get the evaluation list
     *
     * @return list of EvaluationDescriptor
     */
    public List<EvaluationDescriptor> getEvaluations() {
        return evaluations;
    }

    /**
     * set the evaluation descriptions
     *
     * @param evaluations list of evaluation descriptor
     */
    public void setEvaluations(List<EvaluationDescriptor> evaluations) {
        this.evaluations = evaluations;
        for (EvaluationDescriptor ed : this.evaluations) {
            ed.setContainer(this);
        }
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void merge(AbstractEntity a) {
        if (a instanceof EvaluationDescriptorContainer) {
            EvaluationDescriptorContainer other = (EvaluationDescriptorContainer) a;
            this.setEvaluations(ListUtils.mergeLists(this.getEvaluations(), other.getEvaluations()));
        } else {
            throw new WegasIncompatibleType(this.getClass().getSimpleName() + ".merge (" + a.getClass().getSimpleName() + ") is not possible");
        }
    }

    /**
     * back reference to PeerReviewDescriptor through FbPRD or CommentsPRD
     *
     * @return
     */
    private PeerReviewDescriptor getEffectiveDescriptor() {
        if (this.getFbPeerReviewDescriptor() != null) {
            return this.getFbPeerReviewDescriptor();
        } else {
            return this.getCommentsPeerReviewDescriptor();
        }
    }

    @Override
    public Collection<WegasPermission> getRequieredUpdatePermission() {
        return this.getEffectiveDescriptor().getRequieredUpdatePermission();
    }

    @Override
    public Collection<WegasPermission> getRequieredReadPermission() {
        return this.getEffectiveDescriptor().getRequieredReadPermission();
    }
}
