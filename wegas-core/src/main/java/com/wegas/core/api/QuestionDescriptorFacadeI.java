/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.api;

import com.wegas.core.exception.client.WegasRuntimeException;
import com.wegas.core.persistence.game.Player;
import com.wegas.mcq.ejb.QuestionDescriptorFacade.ReplyValidate;
import com.wegas.mcq.ejb.QuestionDescriptorFacade.WhValidate;
import com.wegas.mcq.persistence.QuestionInstance;
import com.wegas.mcq.persistence.Reply;
import com.wegas.mcq.persistence.wh.WhQuestionInstance;
import com.wegas.messaging.persistence.Message;
import jdk.nashorn.api.scripting.JSObject;

/**
 *
 * @author maxence
 */
public interface QuestionDescriptorFacadeI {

    /**
     * @param playerId id of player who wants to cancel the reply
     * @param replyId  id of reply to cancel
     *
     * @return reply being canceled
     */
    Reply cancelReply(Long playerId, Long replyId);

    /**
     * Create an ignoration Reply
     *
     * @param choiceId
     * @param player
     *
     * @return new reply
     */
    Reply ignoreChoice(Long choiceId, Player player);

    /**
     *
     * {@link #selectChoice(java.lang.Long, com.wegas.core.persistence.game.Player, java.lang.Long)  selectChoice} + {@link #validateReply(com.wegas.core.persistence.game.Player, java.lang.Long)  validateReply}
     * in one shot
     *
     * @param choiceId selected choice id
     * @param playerId id of player who select the choice
     *
     * @return the new validated reply
     */
    Reply selectAndValidateChoice(Long choiceId, Long playerId);

    /**
     * {@link #selectChoice(java.lang.Long, java.lang.Long, java.lang.Long) selectChoice}
     * with startTime = 0
     *
     * @param choiceId
     * @param playerId
     *
     * @return the new reply
     */
    Reply selectChoice(Long choiceId, Long playerId);

    /**
     * @param choiceId  selected choice id
     * @param playerId  id of player who select the choice
     * @param startTime time the player select the choice
     *
     * @return the new reply
     */
    Reply selectChoice(Long choiceId, Long playerId, Long startTime);

    /**
     * create a reply for given player based on given choice
     *
     * @param choiceId  selected choice
     * @param player    player who select the choice
     * @param startTime time the player select the choice
     *
     * @return the new reply
     */
    Reply selectChoice(Long choiceId, Player player, Long startTime);

    /**
     * @param replyId
     * @param r
     *
     * @return the updated reply
     */
    Reply updateReply(Long replyId, Reply r);

    /**
     * Validate either a checkboxed QuestionInstance or a WhQuestionInstance
     *
     * @param questionInstanceId
     * @param playerId
     */
    void validateQuestion(Long questionInstanceId, Long playerId);

    /**
     * Validate either a checkboxed QuestionInstance or a WhQuestionInstance
     *
     * @param questionInstanceId
     * @param player
     */
    void validateQuestion(Long questionInstanceId, Player player);

    /**
     * Validates a question that's marked as checkbox type: sequentially
     * validates all replies (i.e. selected choices) and processes all other
     * choices as "ignored".
     *
     * @param validateQuestion
     * @param player
     *
     * @throws com.wegas.core.exception.client.WegasRuntimeException
     */
    void validateQuestion(final QuestionInstance validateQuestion, final Player player) throws WegasRuntimeException;

    /**
     * Validate a wh-question
     *
     * @param validateQuestion
     * @param player
     *
     * @throws WegasRuntimeException
     */
    void validateQuestion(final WhQuestionInstance validateQuestion, final Player player) throws WegasRuntimeException;

    /**
     * @param playerId
     * @param replyVariableInstanceId
     */
    void validateReply(Long playerId, Long replyVariableInstanceId);

    /**
     * @param player
     * @param replyVariableInstanceId
     */
    void validateReply(Player player, Long replyVariableInstanceId);

    /**
     * @param player
     * @param validateReply
     *
     * @throws com.wegas.core.exception.client.WegasRuntimeException
     */
    void validateReply(final Player player, final Reply validateReply) throws WegasRuntimeException;

    /**
     * According to whValidate event, create a message to be send to an inbox
     *
     * @param self       message recipient
     * @param whValidate open question event
     * @param i18n       the JS i18n object
     *
     * @return a message which summarise the answer
     */
    Message buildWhValidateMessage(Player self, WhValidate whValidate, JSObject i18n);

    /**
     * According to ReplyValidate event, create a message to be send to an inbox
     *
     * @param self          message recipient
     * @param replyValidate QuestionDescription replyValidate event
     * @param config        key/value object which may contains: <ul>
     * <li>includeHistory :boolean </li>
     * </ul>
     * @param i18n          the JS i18n object
     *
     * @return a message which summarise the answer
     */
    Message buildReplyValidateMessage(Player self, ReplyValidate replyValidate, JSObject i18n, JSObject config);

}
