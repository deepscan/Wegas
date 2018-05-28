import { css } from 'emotion';
import * as React from 'react';
import { editor } from 'monaco-editor';

interface EditorProps {
  value?: string;
  language?: 'javascript' | 'css' | 'json';
  onChange?: (value: string) => void;
  onBlur?: (value: string) => void;
}
const overflowHide = css({
  overflow: 'hidden',
  width: '100%',
  height: '100%',
});
class SrcEditor extends React.Component<EditorProps> {
  private editor: editor.IStandaloneCodeEditor | null = null;
  private lastValue?: string = '';
  private outsideChange: boolean = false;
  private container: HTMLDivElement | null = null;

  static defaultProps = {
    language: 'javascript',
    onBlur: () => {},
    onChange: () => {},
  };
  constructor(props: EditorProps) {
    super(props);
  }
  componentDidUpdate(prevProps: EditorProps) {
    if (this.lastValue !== this.props.value) {
      this.lastValue = this.props.value;
      this.outsideChange = true;
      this.editor!.setValue(this.props.value!);
      this.outsideChange = false;
    }
    if (this.props.language !== prevProps.language) {
      import('monaco-editor').then(monaco => {
        monaco.editor.setModelLanguage(
          this.editor!.getModel(),
          this.props.language!,
        );
      });
    }
  }
  componentDidMount() {
    this.lastValue = this.props.value;
    import('monaco-editor').then(monaco => {
      if (this.container != null) {
        this.editor = monaco.editor.create(this.container, {
          theme: 'vs-dark',
          language: this.props.language,
          value: this.props.value,
        });
        this.editor.onDidBlurEditor(() =>
          this.props.onBlur!(this.editor!.getValue()),
        );
        this.editor.onDidChangeModelContent(() => {
          if (!this.outsideChange) {
            this.lastValue = this.editor!.getValue();
            this.props.onChange!(this.lastValue);
          }
        });
      }
    });
  }
  getValue() {
    if (this.editor != null) {
      return this.editor.getValue();
    }
    return this.props.value;
  }
  componentWillMount() {
    if (this.editor != null) {
      this.editor.dispose();
    }
  }
  refContainer = (n: HTMLDivElement | null) => {
    this.container = n;
  };
  render() {
    return <div className={overflowHide} ref={this.refContainer} />;
  }
}
export default SrcEditor;
