package com.eden.orchid.api.theme.components;

import com.caseyjbrooks.clog.Clog;
import com.eden.orchid.api.OrchidContext;
import com.eden.orchid.api.theme.pages.OrchidPage;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ComponentHolder {

    private final OrchidContext context;
    private final JSONArray componentsJson;
    private final Map<String, Class<? extends OrchidComponent>> componentTypesMap;

    private List<OrchidComponent> components;

    @Inject
    public ComponentHolder(OrchidContext context, JSONArray componentsJson) {
        this.context = context;
        this.componentsJson = componentsJson;

        Set<OrchidComponent> componentTypes = context.resolveSet(OrchidComponent.class);
        componentTypesMap = new HashMap<>();

        for (OrchidComponent componentType : componentTypes) {
            componentTypesMap.put(componentType.getKey(), componentType.getClass());
        }
    }

    public boolean isEmpty() {
        if (components != null) {
            if(components.size() > 0) {
                return false;
            }
        }
        else if (componentsJson != null) {
            if(componentsJson.length() > 0) {
                return false;
            }
        }

        return true;
    }

    public boolean hasComponent(String key) {
        if (components == null) {
            for (int i = 0; i < componentsJson.length(); i++) {
                JSONObject componentJson = componentsJson.getJSONObject(i);
                String componentType = componentJson.getString("type");
                if(componentType.equals(key)) {
                    return true;
                }
            }
        }
        else {
            for(OrchidComponent component : components) {
                if(component.getKey().equals(key)) {
                    return true;
                }
            }
        }

        return false;
    }

    public JSONObject getComponentData(String key) {
        for (int i = 0; i < componentsJson.length(); i++) {
            JSONObject componentJson = componentsJson.getJSONObject(i);
            String componentType = componentJson.getString("type");
            if(componentType.equals(key)) {
                invalidateComponents();
                return componentJson;
            }
        }

        return null;
    }

    public List<OrchidComponent> getComponents(OrchidPage containingPage) {
        if (components == null) {
            components = new ArrayList<>();

            for (int i = 0; i < componentsJson.length(); i++) {
                JSONObject componentJson = componentsJson.getJSONObject(i);
                String componentType = componentJson.getString("type");

                if(componentTypesMap.containsKey(componentType)) {
                    OrchidComponent component = context.getInjector().getInstance(componentTypesMap.get(componentType));
                    component.setOrder((i+1)*10);

                    if(component.canBeUsedOnPage(containingPage, this, componentsJson, components)) {
                        component.setPage(containingPage);
                        component.extractOptions(context, componentJson);
                        components.add(component);
                    }
                }
                else {
                    Clog.w("Component {} (on page {} at {}) could not be found", componentType, containingPage.getTitle(), containingPage.getLink());
                }
            }

            components.sort(Comparator.comparingInt(OrchidComponent::getOrder));
        }

        return components;
    }

    public List<OrchidComponent> getRemainingComponents(OrchidPage containingPage) {
        return getComponents(containingPage);
    }

    public void addComponent(JSONObject menuItemJson) {
        invalidateComponents();
        componentsJson.put(menuItemJson);
    }

    public void addComponents(JSONArray menuItemsJson) {
        invalidateComponents();
        for (int i = 0; i < menuItemsJson.length(); i++) {
            componentsJson.put(menuItemsJson.get(i));
        }
    }

    public void invalidateComponents() {
        components = null;
    }

}
