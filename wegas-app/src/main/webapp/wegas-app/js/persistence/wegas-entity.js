/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018  School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
/**
 * @fileoverview
 * @author Francois-Xavier Aeberhard <fx@red-agent.com>
 */
YUI.add('wegas-entity', function(Y) {
    'use strict';
    var STRING = 'string',
        NUMBER = 'number',
        HIDDEN = 'hidden',
        ARRAY = 'array',
        NAME = 'name',
        BUTTON = 'Button',
        TEXT = 'text',
        HTML = 'html',
        ITEMS = 'items',
        GROUP = 'group',
        Wegas = Y.namespace('Wegas'),
        persistence = Y.namespace('Wegas.persistence'),
        Base = Y.Base,
        Entity,
        IDATTRDEF = {
            type: NUMBER,
            optional: true, // The id is optional for entites that have not been persisted
            index: -20,
            view: {
                type: HIDDEN
            }
        },
        PERMISSION = {
            optional: true,
            type: ARRAY,
            items: {
                type: 'object',
                properties: {
                    id: {
                        type: 'number',
                        view: {type: 'hidden'}
                    },
                    '@class': {
                        value: 'Permission',
                        view: {type: 'hidden'}
                    },
                    value: {
                        type: 'string',
                        view: {
                            label: 'Value'
                        }
                    }
                }
            },
            view: {
                className: 'wegas-advanced-feature'
            }
        };
    /**
     * 
     * @param {{type:"string" | "html", description?:string, label?:string, index?:number}} param 
     */
    Y.Wegas.Helper.getTranslationAttr = function(param) {
        var TRANSLATION_CONTENT_ATTR_DEF = {
            id: IDATTRDEF,
            "@class": {
                value: "TranslatableContent",
                type: STRING,
                view: {
                    type: HIDDEN
                }
            },
            translations: {
                type: "object",
                value: {},
                additionalProperties: {
                    type: ["null", STRING],
                    required: true,
                    view: {
                        label: "translation",
                        type: ""
                    }
                },
                view: {
                    type: "I18n" + param.type,
                    keyLabel: "lang",
                    label: param.label,
                    description: param.description,
                    borderTop: param.borderTop

                }
            }
        },
            TRANSLATION_VIEW = {
                type: "object",
                index: param.index,
                visible: param.visible,
                preProcessAST: function(argDesc, value, tools) {
                    if (value) {
                        if (value.type === 'Literal') {
                            return tools.valueToAST({
                                "@class": "TranslatableContent",
                                "translations": {
                                    "def": value.value
                                }
                            }, argDesc);
                        } else if (value.value === undefined && value.type === "Identifier") {
                            return tools.valueToAST({
                                "@class": "TranslatableContent",
                                "translations": {}
                            }, argDesc);
                        }
                    }
                    return value;
                },
                getter: function(value) {
                    if (typeof value === "string") {
                        //  backward compatibility: raw String to default translation
                        return {
                            "@class": "TranslatableContent",
                            "translations": {
                                "def": value
                            }
                        };
                    }
                    return value;
                },
                valueFn: function() {
                    return {
                        "@class": "TranslatableContent",
                        translations: {}
                    };
                },
                properties: TRANSLATION_CONTENT_ATTR_DEF
            };
        return TRANSLATION_VIEW;
    };
    /**
     * @class Entity is used to represent db objects
     * @name Y.Wegas.persistence.Entity
     * @extends Y.Base
     * @augments Y.Wegas.Editable
     * @constructor
     */
    Entity = Base.create(
        'Entity',
        Base,
        [Wegas.Editable],
        {},
        {
            ATTRS: {
                initialized: {
                    transient: true
                },
                destroyed: {
                    transient: true
                },
                id: {
                    type: NUMBER,
                    optional: true, // The id is optional for entites that have not been persisted
                    writeOnce: 'initOnly',
                    setter: function(val) {
                        return val * 1;
                    },
                    index: -20,
                    view: {
                        type: 'uneditable',
                        className: 'wegas-advanced-feature',
                        label: 'Id',
                        layout: 'shortInline'
                    }
                },
                '@class': {
                    value: 'null',
                    required: true,
                    writeOnce: 'initOnly',
                    type: STRING,
                    view: {
                        type: HIDDEN
                    }
                }
            },
            /**
             *  Defines edition menu to be used in editor
             */
            EDITMENU: [],
            /**
             * Defines methods available in wysiwyge script editor
             */
            METHODS: {}
        }
    );
    persistence.Entity = Entity;
    /**
     *
     */
    persistence.DefaultEntity = Base.create(
        'DefaultEntity',
        Entity,
        [],
        {
            initializer: function(cfg) {
                this.set('val', cfg);
            },
            toJSON: function() {
                return this.get('val');
            }
        },
        {
            ATTRS: {
                val: {}
            }
        }
    );
    /**
     * ManagedResponse mapper
     */
    persistence['ManagedResponse'] = Base.create(
        'ManagedResponse',
        Entity,
        [],
        {},
        {
            ATTRS: {
                deletedEntities: {
                    value: []
                },
                updatedEntities: {
                    value: []
                },
                events: {
                    value: []
                }
            }
        }
    );
    /**
     *
     */
    persistence.EntityUpdatedEvent = Base.create(
        'EntityUpdatedEvent',
        persistence.Entity,
        [],
        {},
        {
            ATTRS: {
                updatedEntities: {
                    value: []
                }
            }
        }
    );
    persistence.VariableContainer = function() {};
    Y.mix(persistence.VariableContainer.prototype, {
        /**
         * Extend clone to add transient childs
         */
        clone: function() {
            var object = Wegas.Editable.prototype.clone.call(this), i;
            object.items = [];
            for (i in this.get(ITEMS)) {
                if (this.get(ITEMS).hasOwnProperty(i)) {
                    object.items.push(this.get(ITEMS)[i].clone());
                }
            }
            return object;
        },
        /**
         *
         * @param {type} i
         * @returns {Y.Wegas.persistence.VariableDescriptor}
         */
        item: function(i) {
            return Y.Wegas.Facade.Variable.cache.findById(this.get("itemsIds")[i]);
        },
        size: function() {
            return this.get("itemsIds").length;
        }
    });
    persistence.VariableContainer.ATTRS = {
        itemsIds: {
            type: ARRAY,
            value: [],
            "transient": true,
            view: {
                type: HIDDEN
            }
        },
        items: {
            type: ARRAY,
            value: [],
            "transient": true,
            view: {
                type: HIDDEN
            },
            /*
             * one would use setter, but more complicated to keep up to date
             * @param {type} val
             * @returns {undefined}
             */
            getter: function() {
                return Y.Array.map(this.get('itemsIds'), Y.Wegas.Facade.Variable.cache.findById, Y.Wegas.Facade.Variable.cache);
            }
        }
    };
    // I18N

    persistence.GameModelLanguage = Base.create("GameModelLanguage", persistence.Entity, [], {}, {
        EDITORNAME: "Language",
        ATTRS: {
            id: IDATTRDEF,
            '@class': {
                type: "string",
                value: 'GameModelLanguage',
                view: {
                    type: HIDDEN
                }
            },
            refName: {
                type: "string",
                view: {
                    type: 'uneditable',
                    label: "refName",
                    description: "Internal identifier"
                }
            },
            code: {
                type: "string",
                view: {
                    label: "language code name"
                }
            },
            active: {
                type: "boolean",
                view: {
                    label: "Active"
                }
            },
            lang: {
                type: "string",
                view: {
                    label: "language full name"
                }
            }
        }
    });
    persistence.TranslatableContent = Base.create("TranslatableContent", persistence.Entity, [], {}, {
        EDITORNAME: "TranslatableContent",
        ATTRS: {
            id: IDATTRDEF,
            '@class': {
                type: "string",
                value: 'TranslatableContent',
                view: {
                    type: HIDDEN
                }
            },
            translations: {
                type: "object",
                additionalProperties: {
                    type: STRING,
                    required: true,
                    view: {
                        label: "translation"
                    }
                },
                view: {
                    label: "lang",
                    type: "hashlist",
                    keyLabel: "lang"
                }
            }
        }
    });
    /**
     * GameModel mapper
     */
    persistence.GameModel = Base.create("GameModel", persistence.Entity, [persistence.VariableContainer], {}, {
        EDITORNAME: "Scenario",
        ATTRS: {
            name: {
                type: STRING,
                view: {
                    label: 'Name',
                    className: 'editor-form-gamemodel-name'
                }
            },
            games: {
                type: ARRAY,
                value: [],
                transient: true
            },
            scriptLibrary: {
                value: {},
                transient: true
            },
            clientScriptLibrary: {
                value: {},
                transient: true
            },
            cssLibrary: {
                value: {},
                transient: true
            },
            activeLanguages: {
                "transient": true,
                getter: function(val) {
                    return Y.Array.filter(this.get("languages"), function(l) {
                        return l.get("active");
                    });
                }
            },
            languages: {
                type: 'array',
                view: {
                    type: "hidden"
                }
            },
            properties: {
                type: 'object',
                value: {},
                properties: {
                    guestAllowed: {
                        type: "boolean",
                        view: {label: "Guest allowed?"}
                    },
                    freeForAll: {
                        type: 'boolean',
                        view: {
                            label: 'Game is played',
                            type: 'select',
                            choices: [
                                {
                                    value: true,
                                    label: 'individually'
                                },
                                {
                                    value: false,
                                    label: 'in team'
                                }
                            ]
                        }
                    },
                    scriptUri: {
                        type: STRING,
                        view: {label: 'Server scripts'}
                    },
                    clientScriptUri: {
                        type: STRING,
                        view: {label: 'Client scripts'}
                    },
                    cssUri: {
                        type: STRING,
                        view: {label: 'Stylesheets'}
                    },
                    logID: {
                        type: STRING,
                        view: {label: 'Log ID'}
                    },
                    pagesUri: {
                        type: STRING,
                        view: {label: 'Pages'}
                    },
                    // still in use ??
                    imageUri: {
                        type: STRING,
                        view: {
                            type: 'hidden',
                            label: 'Image uri',
                            className: 'wegas-advanced-feature'
                        }
                    },
                    iconUri: {
                        type: STRING,
                        view: {
                            type: 'uneditable',
                            label: 'Icon uri',
                            className: 'wegas-advanced-feature',
                            description: 'Use the lobby to edit the icon'
                        }
                    },
                    // still in use ??
                    websocket: {
                        type: STRING,
                        view: {
                            type: 'hidden',
                            label: 'Websocket'
                        }
                    }
                }
            },
            description: {
                type: STRING,
                view: {
                    type: 'html',
                    label: 'Description'
                }
            },
            comments: {
                type: [STRING, 'null'],
                index: 100,
                view: {
                    type: 'textarea',
                    className: 'wegas-comments',
                    placeholder: 'Optional comments'
                }
            },
            canView: {
                transient: true
            },
            canEdit: {
                transient: true
            },
            canDuplicate: {
                transient: true
            },
            canInstantiate: {
                transient: true
            },
            createdTime: {
                transient: true
            },
            createdByName: {
                transient: true
            }
        },
        EDITMENU: []
    }
    );
    /**
     * Game mapper
     */
    persistence.Game = Base.create(
        'Game',
        persistence.Entity,
        [],
        {},
        {
            ATTRS: {
                gameModelId: {
                    type: STRING,
                    view: {
                        type: HIDDEN
                    }
                },
                name: {
                    type: STRING,
                    view: {
                        className: 'wegas-game-name',
                        label: 'Name'
                    }
                },
                gameModelName: {
                    //"transient": true
                    type: STRING,
                    view: {
                        type: 'uneditable',
                        label: 'Scenario',
                        className: 'wegas-game-scenario'
                    }
                },
                createdByName: {
                    transient: true
                },
                createdTime: {
                    transient: true
                },
                updatedTime: {
                    transient: true
                },
                gameModel: {
                    //                                                      // Extended view only
                    transient: true
                },
                properties: {
                    transient: true
                },
                teams: {
                    transient: true,
                    value: []
                },
                shareLabel: {
                    type: STRING,
                    optional: true,
                    view: {
                        type: 'uneditable',
                        label: 'Game access',
                        className: 'wegas-game-subtitle'
                    }
                },
                access: {
                    type: STRING
                },
                token: {
                    type: STRING
                },
                keys: {
                    type: ARRAY,
                    value: []
                },
                accountkeys: {
                    type: ARRAY,
                    value: []
                },
                playersCount: {
                    transient: true,
                    getter: function() {
                        var count = 0;
                        Y.Array.each(this.get('teams'), function(t) {
                            if (!(t instanceof persistence.DebugTeam)) {
                                count += t.get('players').length;
                            }
                        });
                        return count;
                    }
                }
            },
            EDITMENU: []
        }
    );
    persistence.DebugGame = persistence.Game;
    /**
     * Team mapper
     */
    persistence.Team = Base.create("Team", persistence.Entity, [], {
        getPlayerByStatus: function(statuses) {
            var i, player;
            if (!Array.isArray(statuses)) {
                statuses = [statuses];
            }

            for (i in this.get("players")) {
                player = this.get("players")[i];
                if (statuses.indexOf(player.get("status")) >= 0) {
                    return player;
                }
            }

            return null;
        },
        getLivePlayer: function() {
            return this.getPlayerByStatus("LIVE");
        },
        getStatus: function() {
            if (this.isLive()) {
                return "LIVE";
            } else if (this.getPlayerByStatus(["PROCESSING", "SEC_PROCESSING"])) {
                return "PROCESSING";
            } else if (this.getPlayerByStatus(["WAITING", "RESCHEDULED"])) {
                return "WAITING";
            } else {
                return "FAILED";
            }

        },
        /**
         * is the team live? ie does it contains at least one live player ?
         * @returns {Boolean}
         */
        isLive: function() {
            return this.getLivePlayer() !== null;
        }
    }, {
        ATTRS: {
            '@class': {
                value: 'Team'
            },
            name: {
                type: STRING
            },
            notes: {
                value: '',
                view: {
                    type: HIDDEN
                }
            },
            declaredSize: {
                transient: true
            },
            status: {
                "transient": true
            },
            players: {
                value: [],
                view: {
                    type: HIDDEN
                }
            },
            gameId: IDATTRDEF
        },
        EDITMENU: []
    }
    );
    /**
     *
     */
    persistence.DebugTeam = Base.create(
        'DebugTeam',
        persistence.Team,
        [],
        {},
        {}
    );
    /**
     * Player mapper
     */
    persistence.Player = Base.create(
        'Player',
        persistence.Entity,
        [],
        {},
        {
            ATTRS: {
                name: {
                    type: STRING
                },
                teamId: IDATTRDEF,
                userId: {
                    transient: true
                },
                team: {
                    transient: true,
                    getter: function() {
                        return Wegas.Facade.Game.cache.find(
                            'id',
                            this.get('teamId')
                            );
                    }
                },
                verifiedId: {
                    transient: true
                },
                homeOrg: {
                    "transient": true
                },
                status: {
                    "transient": true
                },
                refName: {
                    type: "string"
                }
            },
            EDITMENU: []
        }
    );
    /**
     * User mapper
     */
    persistence.User = Base.create(
        'User',
        persistence.Entity,
        [],
        {
            getMainAccount: function() {
                return this.get('accounts')[0];
            }
        },
        {
            ATTRS: {
                name: {
                    type: STRING,
                    transient: true,
                    getter: function(val) {
                        if (this.getMainAccount()) {
                            return this.getMainAccount().getPublicName();
                        }
                        return val;
                    }
                },
                password: {
                    type: STRING
                },
                accounts: {
                    type: ARRAY
                }
            }
        }
    );
    /**
     * Role mapper
     */
    persistence.Role = Base.create(
        'Role',
        persistence.Entity,
        [],
        {},
        {
            ATTRS: {
                name: {
                    type: STRING
                },
                description: {
                    transient: true,
                    type: STRING,
                    format: TEXT,
                    optional: true
                },
                permissions: PERMISSION
            },
            EDITMENU: [
                {
                    type: 'EditEntityButton',
                    label: 'Edit group'
                },
                {
                    type: 'DeleteEntityButton'
                }
            ]
        }
    );
    /**
     * JpaAccount mapper
     */
    persistence.JpaAccount = Base.create(
        'JpaAccount',
        persistence.Entity,
        [],
        {
            getPublicName: function() {
                return this.get(NAME);
            }
        },
        {
            ATTRS: {
                '@class': {
                    value: 'JpaAccount'
                },
                name: {
                    transient: true,
                    getter: function() {
                        if (this.get('firstname') || this.get('lastname')) {
                            return (
                                this.get('firstname') +
                                ' ' +
                                (this.get('lastname') || '')
                                );
                        } else {
                            return this.get('email');
                        }
                    }
                },
                firstname: {
                    type: STRING,
                    view: {
                        label: 'First name'
                    }
                },
                lastname: {
                    label: 'Last name',
                    type: STRING,
                    view: {
                        label: 'Last name'
                    }
                },
                email: {
                    type: STRING,
                    view: {
                        type: 'string'
                    }
                },
                username: {
                    type: STRING,
                    optional: true,
                    view: {
                        label: 'Username',
                        description: 'Can be used to log in'
                    }
                },
                hash: {
                    transient: true
                },
                password: {
                    type: STRING,
                    optional: true,
                    view: {
                        type: 'password',
                        label: 'Password',
                        strengthIndicator: false,
                        capsLockWarning: true,
                        description: 'Leave blank for no change'
                    }
                },
                passwordConfirm: {
                    type: STRING,
                    optional: true,
                    errored: function(val, formVal) {
                        if (val !== formVal.password) {
                            return 'Passwords do not match';
                        }
                    },
                    view: {
                        type: 'password',
                        label: 'Confirm password'
                    }
                },
                roles: {
                    optional: true,
                    type: ARRAY,
                    items: {
                        type: STRING
                    },
                    view: {
                        label: 'Groups'
                    }
                },
                permissions: PERMISSION
            },
            EDITMENU: [
                {
                    type: 'EditEntityButton',
                    label: 'Edit user'
                },
                {
                    type: 'DeleteEntityButton'
                }
            ]
        }
    );
    /**
     * AaiAccount mapper
     */
    persistence.AaiAccount = Base.create(
        'AaiAccount',
        persistence.Entity,
        [],
        {
            getPublicName: function() {
                return this.get(NAME);
            }
        },
        {
            ATTRS: {
                '@class': {
                    value: 'AaiAccount'
                },
                name: {
                    transient: true,
                    getter: function() {
                        if (this.get('firstname') || this.get('lastname')) {
                            return (
                                this.get('firstname') +
                                ' ' +
                                (this.get('lastname') || '')
                                );
                        } else {
                            return this.get('email');
                        }
                    }
                },
                firstname: {
                    type: STRING,
                    view: {
                        label: 'First name'
                    }
                },
                lastname: {
                    label: 'Last name',
                    type: STRING,
                    view: {
                        label: 'Last name'
                    }
                },
                email: {
                    type: STRING,
                    view: {
                        type: 'email'
                    }
                },
                roles: {
                    optional: true,
                    type: ARRAY,
                    items: {
                        type: STRING,
                        choices: []
                    },
                    view: {
                        label: 'Groups'
                    }
                },
                permissions: PERMISSION
            },
            EDITMENU: [
                {
                    type: 'DeleteEntityButton'
                }
            ]
        }
    );
    /**
     * GuestJpaAccount mapper
     */
    persistence.GuestJpaAccount = Base.create(
        'GuestJpaAccount',
        persistence.Entity,
        [],
        {
            getPublicName: function() {
                return 'Guest';
            }
        },
        {
            ATTRS: {
                '@class': {
                    value: 'GuestJpaAccount'
                },
                permissions: {
                    transient: true,
                    value: []
                }
            },
            EDITMENU: [
                {
                    type: 'DeleteEntityButton'
                }
            ]
        }
    );
});
