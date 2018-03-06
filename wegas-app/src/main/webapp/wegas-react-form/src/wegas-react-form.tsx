import { Schema } from 'jsoninput';
import React from 'react';
import { render, unmountComponentAtNode } from 'react-dom';
import { css } from 'glamor';
import { debounce, cloneDeep } from 'lodash-es';
import promised from './HOC/loadAsyncComp';

const FORM = 'form';

const wegasSimpleButtonStyle = css({
    background: 'none !important',
    fontSize: '26px',
    padding: '0px 5px',
    verticalAlign: 'middle',
});
const saveBtnStyle = css({
    background: 'none',
    transition: '1s color',
});
const activeSaveBtnStyle = css(saveBtnStyle, {
    color: 'black',
    ':hover': {
        color: 'green',
    },
});
const inactiveSaveBtnStyle = css(saveBtnStyle, {
    color: 'gray',
});
const setSavingBtnStyle = css(saveBtnStyle, {
    color: '#4cb050',
});
const containerForm = css({
    position: 'relative',
    width: '100%',
    padding: '0 1em',
    maxWidth: '600px',
    marginBottom: '1em',
    boxSizing: 'border-box',
    '& input, & select, & textarea': {
        ':focus': {
            outline: 'medium auto #6AACF1',
        },
    },
});
const AsyncForm = promised(() => {
    import(/* webpackChunkName: "reactForm" */ './defaultViews');
    return import(/* webpackChunkName: "reactForm" */ 'jsoninput').then(
        RForm => {
            return (props: {
                schema: Schema;
                formRef: React.Ref<React.Component>;
                value?: {};
                onChange: (value: any) => void;
            }) => <RForm.default ref={props.formRef} {...props} />;
        }
    );
});
export function YUIload(Y: Y.YUI) {
    const Wegas: { [key: string]: any } = Y.Wegas;
    const Form = Y.Base.create(
        'wegas-react-form',
        Y.Widget,
        [Y.WidgetChild, Wegas.Widget, Wegas.Editable],
        {
            initializer() {
                this.plug(Y.Plugin.WidgetToolbar);
                this.publish('submit', {
                    emitFacade: true,
                });
                this.publish('updated', {
                    emitFacade: false,
                });
                // reduce number of calls when setting both 'schema' and 'value' at the same time
                this.renderForm = debounce(this.renderForm, 10);
            },
            renderUI() {
                Y.Array.each(this.get('buttons'), this.addButton, this);
                // ctrl-s shortcut
                this.get('contentBox').on(
                    'key',
                    this.save,
                    'down:83+ctrl',
                    this
                );
            },
            renderForm(value: {} | undefined, schema: Schema) {
                if (schema) {
                    const boundFire = (val: {}) => {
                        this.fire('updated', val);
                    };
                    render(
                        <div className={containerForm.toString()}>
                            <AsyncForm
                                formRef={form => this.set(FORM, form)}
                                schema={schema}
                                value={value}
                                onChange={boundFire}
                            />
                        </div>,
                        this.get('contentBox').getDOMNode()
                    );
                }
            },
            getValue() {
                return this.get(FORM) && this.get(FORM).getValue();
            },
            syncUI() {
                this.set('cfg', this.get('cfg'));
            },
            destructor() {
                unmountComponentAtNode(this.get('contentBox').getDOMNode());
                this.set(FORM, null);
            },
            addButton(b: any) {
                const btn = b;
                switch (b.action) {
                    case 'submit':
                        btn.on = {
                            click: Y.bind(this.save, this),
                        };
                        break;
                    default:
                        btn.on = {
                            click: Y.bind(
                                function click(this: any, action: string) {
                                    this.fire(action);
                                },
                                this,
                                b.action
                            ),
                        };
                        break;
                }
                this.toolbar.add(new Wegas.Button(btn));
            },
            destroyForm() {
                this.set(FORM, null);
            },
            save(e: any) {
                e.halt(true);

                const form = this.get(FORM);
                const val = form.getValue();

                if (form.validate().length) {
                    this.showMessage('error', 'Some fields are not valid.');
                    return;
                }
                // if (val.valueselector) {
                //     val = val.valueselector;
                // }
                this.animateSaveBtn();
                this.fire('submit', {
                    value: cloneDeep(val), // Immutability ...
                });
            },
            validate() {
                return this.get('form').validate();
            },
            // Set visual feedback for when the "save" button should be clicked
            activateSaveBtn() {
                const ptn =
                    this.get('contentBox')._node &&
                    this.get('contentBox').get('parentNode');
                if (!ptn) {
                    return;
                }
                const btn = ptn.one('.wegas-save-form-button');
                btn
                    .removeClass(setSavingBtnStyle.toLocaleString())
                    .removeClass(inactiveSaveBtnStyle.toString())
                    .addClass(activeSaveBtnStyle.toString());
                btn.setAttribute('title', 'Save your changes');
            },
            // Set normal visual appearance (i.e. when the "save" button does not need to be clicked)
            deactivateSaveBtn() {
                const ptn =
                    this.get('contentBox')._node &&
                    this.get('contentBox').get('parentNode');
                if (!ptn) {
                    return;
                }
                const btn = ptn.one('.wegas-save-form-button');
                btn
                    .removeClass(setSavingBtnStyle.toLocaleString())
                    .removeClass(activeSaveBtnStyle.toString())
                    .addClass(inactiveSaveBtnStyle.toString());
                btn.setAttribute('title', 'Nothing to save');
            },
            // Set visual feedback for when the "save" button is clicked and switches between saving and not saving
            animateSaveBtn(
                setSaving: boolean = true,
                milliSeconds: number = 2000
            ) {
                const btn = this.get('contentBox')
                    .get('parentNode')
                    .one('.wegas-save-form-button');
                if (setSaving) {
                    btn
                        .removeClass(inactiveSaveBtnStyle.toString())
                        .removeClass(activeSaveBtnStyle.toString())
                        .addClass(setSavingBtnStyle.toLocaleString());
                    btn.setAttribute('title', 'Saving ...');
                }
                if (!setSaving || milliSeconds >= 0) {
                    setTimeout(() => this.deactivateSaveBtn(), milliSeconds);
                }
            },
        },
        {
            /** @lends Y.Wegas.Form */
            EDITORNAME: 'Form',
            /**
             * <p><strong>Attributes</strong></p>
             * <ul>
             *    <li>values: values of fields of the form</li>
             *    <li>form: the form to manage (see YUI Form)</li>
             *    <li>cfg: configuation of the form (see YUI Form)</li>
             * </ul>
             *
             * @field
             * @static
             */
            ATTRS: {
                /**
                 * Values of fields of the form
                 */
                values: {
                    transient: true,
                    value: undefined,
                    setter(this: any, val: any) {
                        this.renderForm(val, this.get('cfg'));
                        return val;
                    },
                },
                /**
                 * The form to manage
                 */
                form: {
                    transient: true,
                },
                /**
                 * Configuation of the form
                 */
                cfg: {
                    type: 'object',
                    validator: Y.Lang.isObject,
                    properties: {
                        type: {
                            type: 'string',
                            value: 'object',
                            view: { type: 'hidden' },
                        },
                        properties: {
                            type: 'object',
                            required: true,
                            value: {},
                            additionalProperties: {
                                type: 'object',
                                properties: {
                                    type: {
                                        type: 'string',
                                        value: 'string',
                                        view: {
                                            label: 'Type',
                                            type: 'select',
                                            choices: [
                                                { value: 'string' },
                                                { value: 'number' },
                                                { value: 'boolean' },
                                            ],
                                        },
                                    },
                                    required: {
                                        type: 'boolean',
                                        view: { label: 'Required' },
                                    },
                                    view: {
                                        type: 'object',
                                        value: {},
                                        properties: {
                                            label: {
                                                errored: function requiredString(
                                                    v: string
                                                ) {
                                                    if (v && v.trim()) {
                                                        return '';
                                                    }
                                                    return 'is required!';
                                                },
                                                view: {
                                                    label: 'Label',
                                                },
                                                type: 'string',
                                            },
                                        },
                                    },
                                },
                            },
                            view: {
                                type: 'hashlist',
                                label: 'Fields',
                                keyLabel: 'Name',
                            },
                        },
                    },
                    setter(this: any, cfg: {}) {
                        this.renderForm(this.get('values'), cfg);
                        // this.setCfg(val);
                        return cfg;
                    },
                    index: 8,
                },
                buttons: {
                    type: 'array',
                    valueFn: () => [
                        {
                            type: 'Button',
                            action: 'submit',
                            cssClass: wegasSimpleButtonStyle.toString(),
                            label:
                                '<span class="wegas-save-form-button fa fa-floppy-o ' +
                                inactiveSaveBtnStyle.toString() +
                                '" title="No changes to save"></span>',
                        },
                    ],
                    view: { type: 'hidden' },
                },
            },
        }
    );
    (Form as any).Script = {
        // Register Global script methods
        register: function r(
            value: string,
            methodObjects: {
                key: {
                    label: string;
                    arguments: any[];
                    className: string;
                    returns: string;
                };
            }
        ) {
            import(/* webpackChunkName: "reactForm" */ './Script/index').then(
                ({ register }) => {
                    register(value, methodObjects);
                }
            );
        },
        MultiVariableMethod(...args: any[]) {
            import(/* webpackChunkName: "reactForm" */ './defaultViews');
            return import(/* webpackChunkName: "reactForm" */ './Script/index').then(
                ({ IndependantMultiVariableMethod }) =>
                    IndependantMultiVariableMethod(...args)
            );
        },

        MultiVariableCondition(...args: any[]) {
            import(/* webpackChunkName: "reactForm" */ './defaultViews');
            return import(/* webpackChunkName: "reactForm" */ './Script/index').then(
                ({ IndependantMultiVariableCondition }) =>
                    IndependantMultiVariableCondition(...args)
            );
        },
        VariableStatement(...args: any[]) {
            import(/* webpackChunkName: "reactForm" */ './defaultViews');
            return import(/* webpackChunkName: "reactForm" */ './Script/index').then(
                ({ IndependantVariableStatement }) =>
                    IndependantVariableStatement(...args)
            );
        },
    };

    /* Add relevant plugin*/
    // Wegas.Form.ATTRS.plugins = Y.clone(Wegas.Widget.ATTRS.plugins);
    // Wegas.Form.ATTRS.plugins._inputex.items.push({ // eslint-disable-line
    //     type: 'Button',
    //     label: 'Save to',
    //     data: 'SaveObjectAction'
    // });

    Wegas.RForm = Form;
}