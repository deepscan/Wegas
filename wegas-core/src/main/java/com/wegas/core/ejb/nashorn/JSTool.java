
package com.wegas.core.ejb.nashorn;
/*
    Using lots of internal class from nashorn...
    @TODO
    JDK 9 make them available on jdk.nashorn.api.tree.*
    jdk.nashorn.internal.runtime.* should not be useful anymore.
    
    remove <compilerArgument>-XDignore.symbol.file</compilerArgument>
    from pom.xml's maven-compiler entry.
    Used to have access to some internals (jdk.internal.org.objectweb.asm*)
 */

import com.wegas.core.exception.client.WegasRuntimeException;
import jdk.nashorn.internal.ir.*;
import jdk.nashorn.internal.ir.visitor.SimpleNodeVisitor;
import jdk.nashorn.internal.parser.Parser;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.Source;
import jdk.nashorn.internal.runtime.options.Options;

import java.util.ArrayList;
import java.util.Collections;

public class JSTool {
    /**
     * Convert code in String form to it's AST from.
     * Nashorn's AST
     *
     * @param code source
     * @return AST
     */
    public static FunctionNode parse(String code) {
        Options options = new Options("nashorn");
        options.set("anon.functions", true);
        options.set("parse.only", true);
        options.set("scripting", true);

        ErrorManager errors = new ErrorManager();
        Context context = new Context(options, errors, Thread.currentThread().getContextClassLoader());
        Source source = Source.sourceFor("internal", code);
        final Parser parser = new Parser(context.getEnv(), source, errors);
        return parser.parse();
    }

    /**
     * Inject a specific sanitizer code into some chosen place of given code.
     *
     * @param code      code to sanitize
     * @param injection code to inject inline. It should end with a semicolon.
     * @return Sanitized code
     */
    public static String sanitize(String code, String injection) {
        final FunctionNode node = parse(code);
        final Visitor visitor = new Visitor(code, injection);
        node.getBody().accept(visitor);
        // inject at start to avoid rewrite from the code.
        return injection + visitor.getResult();
    }

    private static class Visitor extends SimpleNodeVisitor {
        private int off = 0;
        private final StringBuilder res;
        private final String toInject;
        private final ArrayList<Operation> operations = new ArrayList<>();

        public String getResult() {
            // Sort operations by index. Due to parser already hoisting some statements.
            // then apply them. operations could be applied directly with jdk9 (AST order isn't changed)
            Collections.sort(this.operations);
            for (Operation op : operations) {
                res.insert(op.getIdx() + off, op.getInsertion());
                off += op.getInsertion().length();
            }
            this.operations.clear();
            return res.toString();
        }

        public Visitor(String code, String injection) {
            super();
            this.res = new StringBuilder(code);
            this.toInject = injection;
        }

        @Override
        public boolean enterFunctionNode(FunctionNode functionNode) {
            int idx = functionNode.getBody().getStart() + 1;
            this.operations.add(new Operation(idx, toInject));
            return true;
        }

        @Override
        public boolean enterWhileNode(WhileNode whileNode) {
            whileNode.getTest().accept(this);
            blockWrap(whileNode.getBody());
            return false;
        }

        @Override
        public boolean enterForNode(ForNode forNode) {
            if (forNode.getInit() != null) {
                forNode.getInit().accept(this);
            }
            if (forNode.getTest() != null) {
                forNode.getTest().accept(this);
            }
            if (forNode.getModify() != null) {
                forNode.getModify().accept(this);
            }
            blockWrap(forNode.getBody());
            return false;
        }

        /**
         * Check for some "forbidden" function calls
         *
         * @param callNode function call
         * @return continue
         */
        @Override
        public boolean enterCallNode(CallNode callNode) {
            final Expression function = callNode.getFunction();
            if (function instanceof IdentNode) {
                if ("Function".equals(((IdentNode) function).getName())) {
                    throw new JSParseError("Function is Evil");
                }
                if ("eval".equals(((IdentNode) function).getName())) {
                    throw new JSParseError("Eval is Evil");
                }
            }
            return super.enterCallNode(callNode);
        }

        /**
         * Wraps everything (BlockStatement included -- blockchain) into a BlockStatement
         * starting with the injection text
         *
         * @param block the BlockStatement to wrap.
         *              The parser gives us a BlockStatement even for a single Statement in the source
         */
        private void blockWrap(Block block) {

            int idx = block.getStart();
            this.operations.add(new Operation(idx, "{"));
            this.operations.add(new Operation(idx, toInject));
            block.accept(this);
            this.operations.add(new Operation(block.getFinish(), "}"));
        }

        /**
         * Sortable operation which can take place on a string.
         * @deprecated since JDK9. Operations on StringBuilder can take place immediately.
         */
        @Deprecated
        private static class Operation implements Comparable<Operation> {
            /**
             * Source index to which the insertion should take place.
             */
            private final int idx;
            /**
             * Insertion code
             */
            private final String insertion;

            private Operation(int idx, String insertion) {
                this.idx = idx;
                this.insertion = insertion;
            }

            @Override
            public int compareTo(Operation operation) {
                return this.getIdx() - operation.getIdx();
            }

            public int getIdx() {
                return idx;
            }

            public String getInsertion() {
                return insertion;
            }
        }
    }

    public static class JSParseError extends WegasRuntimeException {
        public JSParseError(String message) {
            super(message);
        }
    }
}
