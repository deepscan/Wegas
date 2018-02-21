/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core;

import com.wegas.core.event.client.CustomEvent;
import com.wegas.core.event.client.EntityUpdatedEvent;
import com.wegas.core.event.client.ExceptionEvent;
import com.wegas.core.exception.client.WegasErrorMessage;
import com.wegas.core.exception.client.WegasOutOfBoundException;
import com.wegas.core.exception.client.WegasRuntimeException;
import com.wegas.core.exception.client.WegasScriptException;
import com.wegas.core.persistence.AbstractEntity;
import com.wegas.core.persistence.game.Game;
import com.wegas.core.persistence.game.GameModel;
import com.wegas.core.persistence.game.Player;
import com.wegas.core.persistence.game.Team;
import com.wegas.core.persistence.variable.ListDescriptor;
import com.wegas.core.persistence.variable.ListInstance;
import com.wegas.core.persistence.variable.primitive.BooleanDescriptor;
import com.wegas.core.persistence.variable.primitive.BooleanInstance;
import com.wegas.core.persistence.variable.primitive.NumberDescriptor;
import com.wegas.core.persistence.variable.primitive.NumberInstance;
import com.wegas.core.persistence.variable.primitive.ObjectDescriptor;
import com.wegas.core.persistence.variable.primitive.ObjectInstance;
import com.wegas.core.persistence.variable.primitive.StringDescriptor;
import com.wegas.core.persistence.variable.primitive.StringInstance;
import com.wegas.core.persistence.variable.primitive.TextDescriptor;
import com.wegas.core.persistence.variable.primitive.TextInstance;
import com.wegas.core.persistence.variable.statemachine.Coordinate;
import com.wegas.core.persistence.variable.statemachine.State;
import com.wegas.core.persistence.variable.statemachine.StateMachineDescriptor;
import com.wegas.core.persistence.variable.statemachine.StateMachineInstance;
import com.wegas.core.persistence.variable.statemachine.Transition;
import com.wegas.core.rest.util.JsonbProvider;
import com.wegas.core.rest.util.ManagedResponse;
import com.wegas.core.security.facebook.FacebookAccount;
import com.wegas.core.security.guest.GuestJpaAccount;
import com.wegas.core.security.jparealm.JpaAccount;
import com.wegas.core.security.persistence.AbstractAccount;
import com.wegas.core.security.persistence.Permission;
import com.wegas.core.security.persistence.User;
import com.wegas.mcq.persistence.ChoiceDescriptor;
import com.wegas.mcq.persistence.ChoiceInstance;
import com.wegas.mcq.persistence.QuestionDescriptor;
import com.wegas.mcq.persistence.QuestionInstance;
import com.wegas.mcq.persistence.Reply;
import com.wegas.mcq.persistence.Result;
import com.wegas.mcq.persistence.SingleResultChoiceDescriptor;
import com.wegas.messaging.persistence.InboxDescriptor;
import com.wegas.messaging.persistence.InboxInstance;
import com.wegas.messaging.persistence.Message;
import com.wegas.resourceManagement.persistence.Activity;
import com.wegas.resourceManagement.persistence.Assignment;
import com.wegas.resourceManagement.persistence.Occupation;
import com.wegas.resourceManagement.persistence.ResourceDescriptor;
import com.wegas.resourceManagement.persistence.ResourceInstance;
import com.wegas.resourceManagement.persistence.TaskDescriptor;
import com.wegas.resourceManagement.persistence.TaskInstance;
import com.wegas.resourceManagement.persistence.WRequirement;
import java.util.ArrayList;
import java.util.List;
import javax.json.bind.Jsonb;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Maxence Laurent (maxence.laurent at gmail.com)
 */
public class SerializationTest {

    private static Logger logger = LoggerFactory.getLogger(SerializationTest.class);

    Jsonb jsonb;

    public SerializationTest() {
    }

    @Before
    public void setUp() {
        jsonb = JsonbProvider.getMapper(null);
    }

    @After
    public void tearDown() {
    }

    public void assertPropertyEquals(String json, String property, String expected) {
        String pattern = "\"" + property + "\":\"" + expected + "\"";
        assertTrue("Expected " + expected + ", found " + json, json.contains(pattern));
    }

    @Test
    public void testFSMSerialization() {
        StateMachineDescriptor smD = new StateMachineDescriptor();
        StateMachineInstance smI = new StateMachineInstance();
        smD.setDefaultInstance(smI);
        smI.setDefaultDescriptor(smD);

        State s1 = new State();
        Coordinate coord1 = new Coordinate();
        coord1.setX(100);
        coord1.setY(100);
        s1.setEditorPosition(coord1);
        s1.setLabel("label");
        smD.addState(1L, s1);

        logger.error("Coordinate: {}", coord1);

        State s2 = new State();
        Coordinate coord2 = new Coordinate();
        coord2.setX(500);
        coord2.setY(500);
        s2.setEditorPosition(coord2);
        smD.addState(2L, s2);

        Transition trans1 = new Transition();
        trans1.setNextStateId(2L);
        s1.addTransition(trans1);

        assertPropertyEquals(jsonb.toJson(smD), "@class", "FSMDescriptor");
        assertPropertyEquals(jsonb.toJson(smI), "@class", "FSMInstance");

        assertPropertyEquals(jsonb.toJson(s1), "@class", "State");
        assertPropertyEquals(jsonb.toJson(coord1), "@class", "Coordinate");

        assertPropertyEquals(jsonb.toJson(s2), "@class", "State");
        assertPropertyEquals(jsonb.toJson(coord2), "@class", "Coordinate");

        assertPropertyEquals(jsonb.toJson(trans1), "@class", "Transition");
    }

    @Test
    public void testVariableSerialization() {
        ListDescriptor listD = new ListDescriptor("LIST");
        ListInstance listI = new ListInstance();
        listD.setDefaultInstance(listI);
        listI.setDefaultDescriptor(listD);

        BooleanDescriptor blnD = new BooleanDescriptor("BlnD");
        BooleanInstance blnI = new BooleanInstance(true);
        blnD.setDefaultInstance(blnI);
        blnI.setDefaultDescriptor(blnD);
        listD.addItem(blnD);

        NumberDescriptor numD = new NumberDescriptor("numD");
        NumberInstance numI = new NumberInstance(12.3);
        numD.setDefaultInstance(numI);
        numI.setDefaultDescriptor(numD);
        listD.addItem(numD);

        ObjectDescriptor objD = new ObjectDescriptor();
        ObjectInstance objI = new ObjectInstance();
        objD.setDefaultInstance(objI);
        objI.setDefaultDescriptor(objD);
        objI.setProperty("Key", "Value");

        StringDescriptor stringD = new StringDescriptor();
        StringInstance stringI = new StringInstance();
        stringD.setDefaultInstance(stringI);
        stringI.setDefaultDescriptor(stringD);

        TextDescriptor textD = new TextDescriptor();
        TextInstance textI = new TextInstance();
        textD.setDefaultInstance(textI);
        textI.setDefaultDescriptor(textD);

        assertPropertyEquals(jsonb.toJson(listD), "@class", "ListDescriptor");
        assertPropertyEquals(jsonb.toJson(listI), "@class", "ListInstance");

        assertPropertyEquals(jsonb.toJson(blnD), "@class", "BooleanDescriptor");
        assertPropertyEquals(jsonb.toJson(blnI), "@class", "BooleanInstance");

        assertPropertyEquals(jsonb.toJson(numD), "@class", "NumberDescriptor");
        assertPropertyEquals(jsonb.toJson(numI), "@class", "NumberInstance");

        assertPropertyEquals(jsonb.toJson(objD), "@class", "ObjectDescriptor");
        assertPropertyEquals(jsonb.toJson(objI), "@class", "ObjectInstance");

        assertPropertyEquals(jsonb.toJson(stringD), "@class", "StringDescriptor");
        assertPropertyEquals(jsonb.toJson(stringI), "@class", "StringInstance");

        assertPropertyEquals(jsonb.toJson(textD), "@class", "TextDescriptor");
        assertPropertyEquals(jsonb.toJson(textI), "@class", "TextInstance");

    }

    @Test
    public void testGameSerialization() {
        GameModel gameModel = new GameModel("DasGameModel");
        gameModel.setId(999L);
        Game game = new Game("GameName", "Das-Token");
        gameModel.addGame(game);
        Team team1 = new Team("DasTeam");
        team1.setGame(game);
        game.addTeam(team1);

        FacebookAccount fbAccount = new FacebookAccount();
        GuestJpaAccount guAccount = new GuestJpaAccount();
        JpaAccount jpaAccount = new JpaAccount();

        User fbUser = new User(fbAccount);
        User guUser = new User(guAccount);
        User jpaUser = new User(jpaAccount);

        Player fbPlayer = new Player("Facebook Player");
        Player guPlayer = new Player("Guest Player");
        Player jpaPlayer = new Player("JPA Player");

        team1.addPlayer(fbPlayer);
        team1.addPlayer(guPlayer);
        team1.addPlayer(jpaPlayer);

        fbUser.getPlayers().add(fbPlayer);
        guUser.getPlayers().add(guPlayer);
        jpaUser.getPlayers().add(jpaPlayer);

        Permission permission = new Permission();
        permission.setUser(jpaUser);
        jpaUser.addPermission(permission);

        assertPropertyEquals(jsonb.toJson(game), "@class", "Game");
        assertPropertyEquals(jsonb.toJson(team1), "@class", "Team");

        assertPropertyEquals(jsonb.toJson(fbAccount), "@class", "FacebookAccount");
        assertPropertyEquals(jsonb.toJson(guAccount), "@class", "GuestJpaAccount");
        assertPropertyEquals(jsonb.toJson(jpaAccount), "@class", "JpaAccount");

        assertPropertyEquals(jsonb.toJson(jpaUser), "@class", "User");
        assertPropertyEquals(jsonb.toJson(jpaPlayer), "@class", "Player");
        assertPropertyEquals(jsonb.toJson(permission), "@class", "Permission");
    }

    @Test
    public void testMCQSerialization() {
        QuestionDescriptor questionD = new QuestionDescriptor();
        QuestionInstance questionI = new QuestionInstance();
        questionD.setDefaultInstance(questionI);
        questionI.setDefaultDescriptor(questionD);

        ChoiceDescriptor choiceD = new ChoiceDescriptor();
        ChoiceInstance choiceI = new ChoiceInstance();
        choiceD.setDefaultInstance(choiceI);
        choiceI.setDefaultDescriptor(choiceD);
        choiceD.setQuestion(questionD);
        questionD.addItem(choiceD);

        Result result11 = new Result("R1.1");
        Result result12 = new Result("R1.2");

        choiceD.addResult(result11);
        choiceD.addResult(result12);

        SingleResultChoiceDescriptor singleResult = new SingleResultChoiceDescriptor();
        ChoiceInstance singleChoiceI = new ChoiceInstance();
        singleChoiceI.setDefaultDescriptor(singleResult);
        singleResult.setDefaultInstance(singleChoiceI);
        Result result21 = new Result("R2.1");
        singleResult.addResult(result21);

        Reply reply = new Reply();
        choiceI.addReply(reply);
        reply.setChoiceInstance(choiceI);
        //result11.addReply(reply);
        reply.setResult(result11);

        assertPropertyEquals(jsonb.toJson(questionD), "@class", "QuestionDescriptor");
        assertPropertyEquals(jsonb.toJson(questionI), "@class", "QuestionInstance");

        assertPropertyEquals(jsonb.toJson(questionD), "@class", "ChoiceDescriptor");
        assertPropertyEquals(jsonb.toJson(choiceI), "@class", "ChoiceInstance");

        assertPropertyEquals(jsonb.toJson(result11), "@class", "Result");
        assertPropertyEquals(jsonb.toJson(result12), "@class", "Result");

        assertPropertyEquals(jsonb.toJson(singleResult), "@class", "SingleResultChoiceDescriptor");
        assertPropertyEquals(jsonb.toJson(singleChoiceI), "@class", "ChoiceInstance");
        assertPropertyEquals(jsonb.toJson(result21), "@class", "Result");

        assertPropertyEquals(jsonb.toJson(reply), "@class", "Reply");
    }

    @Test
    public void testMessagingSerialization() {
        InboxDescriptor inboxD = new InboxDescriptor();
        InboxInstance inboxI = new InboxInstance();
        inboxI.setDefaultDescriptor(inboxD);
        inboxD.setDefaultInstance(inboxI);
        Message msg1 = new Message("FROM", "SUBJECT", "CONTENT");
        Message msg2 = new Message("FROM", "SUBJECT", "CONTENT");
        msg1.setInboxInstance(inboxI);
        msg2.setInboxInstance(inboxI);
        inboxI.addMessage(msg1);
        inboxI.addMessage(msg2);

        assertPropertyEquals(jsonb.toJson(inboxD), "@class", "InboxDescriptor");
        assertPropertyEquals(jsonb.toJson(inboxI), "@class", "InboxInstance");
        assertPropertyEquals(jsonb.toJson(msg1), "@class", "Message");
        assertPropertyEquals(jsonb.toJson(msg2), "@class", "Message");
    }

    @Test
    public void testResourceManagementSerialization() {
        /*
         *  RESOURCE MANAGEMENT  
         */

        String propertyValue = "Some value";

        TaskDescriptor taskD = new TaskDescriptor();
        TaskDescriptor taskD2 = new TaskDescriptor();
        taskD.setDescription("DESC");
        taskD.addPredecessor(taskD2);
        taskD.setName("taskD");
        taskD.setProperty("descriptorProperty", propertyValue);
        TaskInstance taskI = new TaskInstance();
        taskI.setDefaultDescriptor(taskD);
        taskD.setDefaultInstance(taskI);
        taskI.getPlannification().add(1);
        taskI.getPlannification().add(2);
        taskI.setProperty("instanceProperty", propertyValue);

        assertPropertyEquals(jsonb.toJson(taskD), "@class", "TaskDescriptor");
        assertPropertyEquals(jsonb.toJson(taskI), "@class", "TaskInstance");

        String strTaskD = jsonb.toJson(taskD);
        String strTaskI = jsonb.toJson(taskI);

        TaskInstance readTaskI = jsonb.fromJson(strTaskI, TaskInstance.class);
        TaskDescriptor readTaskD = jsonb.fromJson(strTaskD, TaskDescriptor.class);

        assertEquals(propertyValue, readTaskI.getProperty("instanceProperty"));
        assertEquals(propertyValue, readTaskD.getProperty("descriptorProperty"));

        ResourceDescriptor resourceD = new ResourceDescriptor();
        resourceD.setName("resourceD");
        resourceD.setDescription("DESC");
        ResourceInstance resourceI = new ResourceInstance();
        resourceD.setDefaultInstance(resourceI);
        resourceI.setProperty("Level", "8");

        assertPropertyEquals(jsonb.toJson(resourceD), "@class", "ResourceDescriptor");
        assertPropertyEquals(jsonb.toJson(resourceI), "@class", "ResourceInstance");

        Activity activity = new Activity();
        taskI.addActivity(activity);

        Assignment assignment = new Assignment();
        assignment.setTaskInstance(taskI);
        assignment.setResourceInstance(resourceI);

        Occupation occupation = new Occupation(2.0);
        occupation.setResourceInstance(resourceI);

        WRequirement req = new WRequirement("Carpenter");
        taskI.getRequirements().add(req);

        activity.setRequirement(req);

        assertPropertyEquals(jsonb.toJson(activity), "@class", "Activity");
        assertPropertyEquals(jsonb.toJson(assignment), "@class", "Assignment");
        assertPropertyEquals(jsonb.toJson(occupation), "@class", "Occupation");
        assertPropertyEquals(jsonb.toJson(req), "@class", "WRequirement");
    }

    @Test
    public void testExceptionMapper() {
        NumberDescriptor nd = new NumberDescriptor("x");
        NumberInstance ns = new NumberInstance(0);

        nd.setDefaultInstance(ns);
        ns.setDefaultDescriptor(nd);

        nd.setMaxValue(10.0);
        //nd.setMinValue(0L);

        nd.getDefaultInstance().setValue(-10);

        String json = jsonb.toJson(new WegasOutOfBoundException(nd.getMinValue(), nd.getMaxValue(), ns.getValue(), nd.getName(), nd.getLabel()));
        System.out.println("WOOB: " + json);
        assertPropertyEquals(json, "@class", "WegasOutOfBoundException");

        json = jsonb.toJson(WegasErrorMessage.error("This is an error"));
        assertPropertyEquals(json, "@class", "WegasErrorMessage");

        json = jsonb.toJson(new WegasScriptException("var a = tagada;", 123, "script exception", null));
        assertPropertyEquals(json, "@class", "WegasScriptException");
    }

    @Test
    public void testManagedModeResponse() {
        String payload = "DummyPayload";
        NumberDescriptor ndPayload = new NumberDescriptor("x");
        NumberInstance niPayload = new NumberInstance(5);
        niPayload.setDefaultDescriptor(ndPayload);
        ndPayload.setDefaultInstance(niPayload);

        CustomEvent custom = new CustomEvent("Dummy CustomEvent", payload);

        List<WegasRuntimeException> exceptions = new ArrayList<>();
        exceptions.add(new WegasOutOfBoundException(0.0, 10.0, 15.0, ndPayload.getLabel(), ndPayload.getLabel()));
        exceptions.add(WegasErrorMessage.error("Error Message"));
        exceptions.add(new WegasScriptException("var a = truc;", 123, "OUPS"));

        List<AbstractEntity> instances = new ArrayList<>();
        NumberDescriptor numberDescriptor = new NumberDescriptor("y");
        NumberInstance numberInstance = new NumberInstance(0.0);
        numberDescriptor.setDefaultInstance(numberInstance);
        numberInstance.setDefaultDescriptor(numberDescriptor);
        instances.add(numberInstance);

        EntityUpdatedEvent update = new EntityUpdatedEvent(instances);

        ExceptionEvent ex = new ExceptionEvent(exceptions);

        ManagedResponse managedResponse = new ManagedResponse();
        managedResponse.getEvents().add(custom);
        managedResponse.getEvents().add(ex);
        managedResponse.getEvents().add(update);

        String json = jsonb.toJson(managedResponse);

        System.out.println("JSON: " + json);

    }

    @Test
    public void testJpaAccount() {
        JpaAccount ja = new JpaAccount();
        ja.setFirstname("Alan");
        ja.setLastname("Smithee");
        ja.setEmail("alan@local");
        ja.setUsername("alan@local");

        String strJa = jsonb.toJson(ja);

        jsonb.fromJson(strJa, AbstractAccount.class);

        // 
    }

}
