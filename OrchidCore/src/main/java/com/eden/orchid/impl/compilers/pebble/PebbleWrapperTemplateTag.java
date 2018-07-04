package com.eden.orchid.impl.compilers.pebble;

import com.caseyjbrooks.clog.Clog;
import com.eden.common.util.EdenUtils;
import com.eden.orchid.api.OrchidContext;
import com.eden.orchid.api.compilers.TemplateTag;
import com.eden.orchid.api.options.OptionsExtractor;
import com.google.inject.Provider;
import com.mitchellbosecke.pebble.error.ParserException;
import com.mitchellbosecke.pebble.extension.NodeVisitor;
import com.mitchellbosecke.pebble.lexer.Token;
import com.mitchellbosecke.pebble.lexer.TokenStream;
import com.mitchellbosecke.pebble.node.AbstractRenderableNode;
import com.mitchellbosecke.pebble.node.BodyNode;
import com.mitchellbosecke.pebble.node.RenderableNode;
import com.mitchellbosecke.pebble.node.expression.Expression;
import com.mitchellbosecke.pebble.node.expression.FilterExpression;
import com.mitchellbosecke.pebble.node.expression.RenderableNodeExpression;
import com.mitchellbosecke.pebble.parser.Parser;
import com.mitchellbosecke.pebble.template.EvaluationContextImpl;
import com.mitchellbosecke.pebble.template.PebbleTemplateImpl;
import com.mitchellbosecke.pebble.tokenParser.TokenParser;
import com.mitchellbosecke.pebble.utils.StringUtils;
import lombok.Getter;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
public final class PebbleWrapperTemplateTag implements TokenParser {

    private final Provider<OrchidContext> contextProvider;
    private final String name;
    private final String[] parameters;
    private final boolean hasContent;
    private final Class<? extends TemplateTag> tagClass;

    public PebbleWrapperTemplateTag(
            Provider<OrchidContext> contextProvider,
            String name,
            String[] parameters,
            boolean hasContent,
            Class<? extends TemplateTag> tagClass) {
        this.contextProvider = contextProvider;
        this.name = name.toLowerCase();
        this.parameters = parameters;
        this.hasContent = hasContent;
        this.tagClass = tagClass;
    }

    @Override
    public String getTag() {
        return name;
    }

    @Override
    public RenderableNode parse(Token token, Parser parser) throws ParserException {
        TokenStream stream = parser.getStream();
        int lineNumber = token.getLineNumber();

        // skip over the tag name token
        stream.next();

        // parameter expressions will be added here
        Map<String, Expression<?>> paramExpressionMap = parseParams(stream, parser);
        Expression<?> tagBodyExpression = null;
        if(hasContent) {
            tagBodyExpression = parseBody(stream, parser);
        }
        else {
            stream.expect(Token.Type.EXECUTE_END);
        }

        return new TemplateTagNode(lineNumber, paramExpressionMap, tagBodyExpression);
    }

    private Map<String, Expression<?>> parseParams(TokenStream stream, Parser parser) throws ParserException {

        // Get list of available parameter names
        OptionsExtractor extractor = contextProvider.get().getInjector().getInstance(OptionsExtractor.class);
        List<String> remainingParameters = new ArrayList<>(extractor.getOptionNames(tagClass));

        // parameter expressions will be added here
        Map<String, Expression<?>> paramExpressionMap = new HashMap<>();
        if(!isParameterEnd(stream)) {
            if (stream.current().test(Token.Type.NAME)) {
                // parse parameters as map of key=value pairs
                while (!isParameterEnd(stream)) {
                    Token paramNameToken = stream.expect(Token.Type.NAME);

                    Optional<String> foundKey = remainingParameters.stream().filter(key -> key.equalsIgnoreCase(paramNameToken.getValue())).findAny();

                    if (foundKey.isPresent()) {
                        String paramKey = foundKey.get();
                        remainingParameters.remove(paramKey);
                        stream.expect(Token.Type.PUNCTUATION, "=");
                        Expression<?> parsedExpression = parser.getExpressionParser().parseExpression();
                        paramExpressionMap.put(paramKey, parsedExpression);
                    }
                    else {
                        throw new ParserException(null, Clog.format("Could not parse parameter {}.", paramNameToken.getValue()), stream.current().getLineNumber(), "");
                    }
                }
            }
            else {
                // parse the parameters sequentially
                int i = 0;
                while (i < parameters.length && !isParameterEnd(stream)) {
                    Expression<?> parsedExpression = parser.getExpressionParser().parseExpression();
                    paramExpressionMap.put(parameters[i], parsedExpression);
                    i++;
                }
            }
        }

        return paramExpressionMap;
    }

    private boolean isParameterEnd(TokenStream stream) {
        return stream.current().test(Token.Type.EXECUTE_END) || stream.current().test(Token.Type.PUNCTUATION, ":");
    }

    private Expression<?> parseBody(TokenStream stream, Parser parser) throws ParserException {
        List<Expression<?>> filterInvocationExpressions = new ArrayList<>();

        if(stream.current().test(Token.Type.PUNCTUATION, ":")) {
            stream.next();
            stream.expect(Token.Type.PUNCTUATION, ":");

            filterInvocationExpressions.add(parser.getExpressionParser().parseFilterInvocationExpression());

            while(stream.current().test(Token.Type.OPERATOR, "|")){
                // skip the '|' token
                stream.next();
                filterInvocationExpressions.add(parser.getExpressionParser().parseFilterInvocationExpression());
            }
        }

        stream.expect(Token.Type.EXECUTE_END);

        BodyNode body = parser.subparse(token -> token.test(Token.Type.NAME, "end" + name));

        stream.next();
        stream.expect(Token.Type.EXECUTE_END);

        Expression<?> lastExpression = new RenderableNodeExpression(body, stream.current().getLineNumber());

        for(Expression<?> filterInvocationExpression : filterInvocationExpressions){

            FilterExpression filterExpression = new FilterExpression();
            filterExpression.setRight(filterInvocationExpression);
            filterExpression.setLeft(lastExpression);

            lastExpression = filterExpression;
        }

        return lastExpression;
    }

    private class TemplateTagNode extends AbstractRenderableNode {

        private final Map<String, Expression<?>> paramExpressionMap;
        private final Expression<?> tagBodyExpression;

        public TemplateTagNode(int lineNumber, Map<String, Expression<?>> paramExpressionMap, Expression<?> tagBodyExpression) {
            super(lineNumber);
            this.paramExpressionMap = paramExpressionMap;
            this.tagBodyExpression = tagBodyExpression;
        }

        @Override
        public void render(PebbleTemplateImpl self, Writer writer, EvaluationContextImpl context) throws IOException {
            TemplateTag freshTag = contextProvider.get().getInjector().getInstance(tagClass);

            Map<String, Object> evaluatedParamExpressionMap = new HashMap<>();

            for(Map.Entry<String, Expression<?>> entry : paramExpressionMap.entrySet()) {
                evaluatedParamExpressionMap.put(entry.getKey(), entry.getValue().evaluate(self, context));
            }

            String bodyContent = null;

            if(tagBodyExpression != null) {
                bodyContent = StringUtils.toString(tagBodyExpression.evaluate(self, context)).trim();
                freshTag.setContent(bodyContent);
                evaluatedParamExpressionMap.put("content", bodyContent);
            }

            JSONObject object = new JSONObject(evaluatedParamExpressionMap);
            freshTag.extractOptions(contextProvider.get(), object);

            if(freshTag.rendersContent()) {
                Map<String, Object> templateArgs = new HashMap<>();
                templateArgs.put("tag", freshTag);

                try {
                    self.includeTemplate(writer, context, "tags/" + name, templateArgs);
                }
                catch (Exception e) {
                    if (!EdenUtils.isEmpty(bodyContent)) {
                        writer.append(bodyContent.trim());
                    }
                }
            }
        }

        @Override
        public void accept(NodeVisitor visitor) {
            visitor.visit(this);
        }
    }

}
