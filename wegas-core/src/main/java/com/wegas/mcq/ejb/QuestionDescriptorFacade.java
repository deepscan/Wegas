/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013, 2014, 2015 School of Business and Engineering Vaud, Comem
 * Licensed under the MIT License
 */
package com.wegas.mcq.ejb;

import com.wegas.core.ejb.BaseFacade;
import com.wegas.core.ejb.PlayerFacade;
import com.wegas.core.ejb.RequestFacade;
import com.wegas.core.ejb.ScriptEventFacade;
import com.wegas.core.ejb.ScriptFacade;
import com.wegas.core.event.internal.DescriptorRevivedEvent;
import com.wegas.core.exception.client.WegasErrorMessage;
import com.wegas.core.exception.client.WegasRuntimeException;
import com.wegas.core.exception.client.WegasScriptException;
import com.wegas.core.exception.internal.WegasNoResultException;
import com.wegas.core.persistence.game.Player;
import com.wegas.mcq.persistence.*;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Francois-Xavier Aeberhard <fx@red-agent.com>
 */
@Stateless
@LocalBean
public class QuestionDescriptorFacade extends BaseFacade<ChoiceDescriptor> {

    static final private Logger logger = LoggerFactory.getLogger(QuestionDescriptorFacade.class);
    /**
     *
     */
    @EJB
    private PlayerFacade playerFacade;
    /**
     *
     */
    @EJB
    private ScriptFacade scriptManager;
    /**
     *
     */
    @EJB
    private RequestFacade requestFacade;

    /**
     *
     */
    @EJB
    QuestionSingleton questionSingleton;
    /**
     *
     */
    @Inject
    private ScriptEventFacade scriptEvent;

    /**
     * Find a result identified by the given name belonging to the given
     * descriptor
     *
     * @param choiceDescriptor
     * @param name
     * @return
     * @throws WegasNoResultException
     */
    public Result findResult(final ChoiceDescriptor choiceDescriptor, final String name) throws WegasNoResultException {
        final CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery cq = cb.createQuery();
        Root<Result> result = cq.from(Result.class);
        cq.where(cb.and(
                cb.equal(result.get("choiceDescriptor"), choiceDescriptor),
                cb.like(result.get("name"), name)));
        final Query q = getEntityManager().createQuery(cq);
        try {
            return (Result) q.getSingleResult();
        } catch (NoResultException ex) {
            throw new WegasNoResultException(ex);
        }
    }

    /**
     *
     * @param event
     */
    public void descriptorRevivedEvent(@Observes DescriptorRevivedEvent event) {
        logger.debug("Received DescriptorRevivedEvent event");

        if (event.getEntity() instanceof ChoiceDescriptor) {
            ChoiceDescriptor choice = (ChoiceDescriptor) event.getEntity();
            ChoiceInstance defaultInstance = ((ChoiceInstance) choice.getDefaultInstance());
            if (defaultInstance.getDeserializedCurrentResultName() != null && !defaultInstance.getDeserializedCurrentResultName().isEmpty()) {
                try {
                    Result cr = findResult(choice, defaultInstance.getDeserializedCurrentResultName());
                    defaultInstance.setCurrentResult(cr);
                } catch (WegasNoResultException ex) {
                    throw WegasErrorMessage.error("Error while setting current result");
                }
            } else if (defaultInstance.getCurrentResultIndex() != null
                    && defaultInstance.getCurrentResultIndex() >= 0
                    && defaultInstance.getCurrentResultIndex() < choice.getResults().size()) {
                Result cr = choice.getResults().get(defaultInstance.getCurrentResultIndex());
                defaultInstance.setCurrentResult(cr);
            }
        }
    }

    /**
     *
     */
    public QuestionDescriptorFacade() {
        super(ChoiceDescriptor.class);
    }

    /**
     *
     * @param replyId
     * @param r
     * @return
     */
    public Reply updateReply(Long replyId, Reply r) {
        final Reply oldEntity = this.getEntityManager().find(Reply.class, replyId);
        oldEntity.merge(r);
        return oldEntity;
    }

    /**
     * @deprecated @param instanceId
     * @return
     */
    public int findReplyCount(Long instanceId) {
        final Query query = getEntityManager().createQuery("SELECT COUNT(r) FROM Reply r WHERE r.questionInstance.id = :id");
        query.setParameter("id", instanceId);
        try {
            return ((Number) query.getSingleResult()).intValue();
        } catch (NoResultException ex) {
            return 0;
        }
    }

    /**
     *
     * @param choiceId
     * @param player
     * @param startTime
     * @return
     */
    public Reply selectChoice(Long choiceId, Player player, Long startTime) {
        Reply reply = questionSingleton.createReply(choiceId, player, startTime);
        try {
            scriptEvent.fire(player, "replySelect", new EventObject(reply));
        } catch (WegasScriptException e) {
            // GOTCHA no eventManager is instantiated
            logger.error("EventListener error (\"replySelect\")", e);
        }

        return reply;
    }

    /**
     *
     * @param choiceId
     * @param playerId
     * @return
     */
    public Reply selectChoice(Long choiceId, Long playerId) {
        return this.selectChoice(choiceId, playerFacade.find(playerId), Long.valueOf(0));
    }

    /**
     *
     * @param choiceId
     * @param playerId
     * @param startTime
     * @return
     */
    public Reply selectChoice(Long choiceId, Long playerId, Long startTime) {
        return this.selectChoice(choiceId, playerFacade.find(playerId), startTime);
    }

    /**
     *
     * @param choiceId
     * @param playerId
     * @return
     */
    public Reply selectAndValidateChoice(Long choiceId, Long playerId) {
        Player player = playerFacade.find(playerId);
        Reply reply = this.selectChoice(choiceId, player, Long.valueOf(0));
        try {
            this.validateReply(player, reply.getId());
        } catch (WegasRuntimeException e) {
            logger.error("CANCEL REPLY", e);
            this.cancelReplyTransactionnal(playerId, reply.getId());
            throw e;
        }
        return reply;
    }

    /**
     * FOR TEST USAGE ONLY
     *
     * @param choiceId
     * @param playerId
     * @return
     * @throws WegasRuntimeException
     */
    public Reply selectAndValidateChoiceTEST(Long choiceId, Long playerId) throws WegasRuntimeException {
        Reply reply = this.selectChoiceTEST(choiceId, playerFacade.find(playerId), Long.valueOf(0));
        try {
            this.validateReply(playerId, reply.getId());
        } catch (WegasRuntimeException e) {
            this.cancelReply(playerId, reply.getId());
            throw e;
        }

        requestFacade.commit();
        return reply;
    }

    /**
     * FOR TEST USAGE ONLY
     *
     * @param choiceId
     * @param player
     * @param startTime
     * @return
     */
    public Reply selectChoiceTEST(Long choiceId, Player player, Long startTime) {
        Reply reply = questionSingleton.createReplyUntransactionnal(choiceId, player, startTime);
        try {
            scriptEvent.fire(player, "replySelect", new EventObject(reply));
        } catch (WegasScriptException e) {
            // GOTCHA no eventManager is instantiated
            logger.error("EventListener error (\"replySelect\") (TEST)", e);
        }

        return reply;
    }

    /**
     *
     * @param playerId
     * @param replyId
     * @return
     */
    public Reply cancelReplyTransactionnal(Long playerId, Long replyId) {
        Reply reply = questionSingleton.cancelReplyTransactionnal(playerId, replyId);
        //try {
        //scriptEvent.fire(playerFacade.find(playerId), "replyCancel", new EventObject(reply));// Throw an event
        //} catch (WegasRuntimeException e) {
        // GOTCHA no eventManager is instantiated
        //}
        return reply;
    }

    /**
     *
     * @param playerId
     * @param replyId
     * @return
     */
    public Reply cancelReply(Long playerId, Long replyId) {

        final Reply reply = getEntityManager().find(Reply.class, replyId);
        getEntityManager().remove(reply);

        try {
            scriptEvent.fire(playerFacade.find(playerId), "replyCancel", new EventObject(reply));// Throw an event
        } catch (WegasRuntimeException e) {
            // GOTCHA no eventManager is instantiated
        }

        return reply;
    }

    /**
     *
     * @param player
     * @param validateReply
     * @throws com.wegas.core.exception.client.WegasRuntimeException
     */
    public void validateReply(final Player player, final Reply validateReply) throws WegasRuntimeException {
        final ChoiceDescriptor choiceDescriptor = validateReply.getResult().getChoiceDescriptor();
        validateReply.setResult(choiceDescriptor.getInstance(player).getResult());// Refresh the current result

        scriptManager.eval(player, validateReply.getResult().getImpact());
        try {
            scriptEvent.fire(player, "replyValidate", new EventObject(validateReply, choiceDescriptor.getInstance(player), validateReply.getQuestionInstance()));
        } catch (WegasRuntimeException e) {
            logger.error("EventListener error (\"replyValidate\")", e);
            // GOTCHA no eventManager is instantiated
        }
    }

    /**
     *
     * @param player
     * @param replyVariableInstanceId
     */
    public void validateReply(Player player, Long replyVariableInstanceId) {
        this.validateReply(player, getEntityManager().find(Reply.class, replyVariableInstanceId));
    }

    /**
     *
     * @param playerId
     * @param replyVariableInstanceId
     */
    public void validateReply(Long playerId, Long replyVariableInstanceId) {
        this.validateReply(playerFacade.find(playerId), replyVariableInstanceId);
    }

    /**
     *
     */
    public static class EventObject {

        /**
         *
         */
        public Reply reply;

        /**
         *
         */
        public ChoiceInstance choice;

        /**
         *
         */
        public QuestionInstance question;

        /**
         *
         * @param reply
         * @param choice
         * @param question
         */
        public EventObject(Reply reply, ChoiceInstance choice, QuestionInstance question) {
            this.reply = reply;
            this.choice = choice;
            this.question = question;
        }

        /**
         *
         * @param reply
         */
        public EventObject(Reply reply) {
            this.reply = reply;
        }

    }
}
