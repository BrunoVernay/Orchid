package com.eden.orchid.impl.compilers.pebble;

import com.eden.orchid.api.OrchidContext;
import com.eden.orchid.api.compilers.TemplateFunction;
import com.eden.orchid.api.theme.pages.OrchidPage;
import com.eden.orchid.utilities.OrchidUtils;
import com.mitchellbosecke.pebble.extension.Function;
import com.mitchellbosecke.pebble.extension.escaper.SafeString;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

import javax.inject.Provider;
import java.util.List;
import java.util.Map;

public final class PebbleWrapperTemplateFunction implements Function {
    private final Provider<OrchidContext> contextProvider;
    private final String name;
    private final List<String> params;
    private final Class<? extends TemplateFunction> functionClass;

    public PebbleWrapperTemplateFunction(Provider<OrchidContext> contextProvider, String name, List<String> params, Class<? extends TemplateFunction> functionClass) {
        this.contextProvider = contextProvider;
        this.name = name;
        this.params = params;
        this.functionClass = functionClass;
    }

    @Override
    public List<String> getArgumentNames() {
        return params;
    }

    @Override
    public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
        TemplateFunction freshFunction = contextProvider.get().resolve(functionClass);

        Object pageVar = context.getVariable("page");
        final OrchidPage actualPage;
        if (pageVar instanceof OrchidPage) {
            actualPage = (OrchidPage) pageVar;
        }
        else {
            actualPage = null;
        }

        freshFunction.extractOptions(contextProvider.get(), args);

        Map<String, Object> data = contextProvider.get().getSiteData(actualPage);

        Object output = freshFunction.apply(contextProvider.get(), actualPage, data);

        if (freshFunction.isSafeString()) {
            return new SafeString(output.toString());
        } else {
            return output;
        }
    }

    public Provider<OrchidContext> getContextProvider() {
        return this.contextProvider;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getParams() {
        return this.params;
    }

    public Class<? extends TemplateFunction> getFunctionClass() {
        return this.functionClass;
    }
}
