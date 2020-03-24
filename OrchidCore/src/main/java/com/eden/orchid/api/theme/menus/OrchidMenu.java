package com.eden.orchid.api.theme.menus;

import com.eden.orchid.api.OrchidContext;
import com.eden.orchid.api.theme.components.ModularPageList;
import com.eden.orchid.api.theme.pages.OrchidPage;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public final class OrchidMenu extends ModularPageList<OrchidMenu, OrchidMenuFactory> {

    @Inject
    public OrchidMenu() {
        super();
    }

    @Override
    protected Class<OrchidMenuFactory> getItemClass() {
        return OrchidMenuFactory.class;
    }

    public List<MenuItem> getMenuItems(OrchidPage containingPage) {
        ArrayList<MenuItem> menuItemsChildren = new ArrayList<>();
        for (OrchidMenuFactory menuItem : get(containingPage)) {
            List<MenuItem> impls = menuItem.getMenuItems(containingPage.getContext(), containingPage);
            if (impls.size() > 0 && menuItem.getAsSubmenu()) {
                MenuItem innerMenuItem = new MenuItem.Builder(containingPage.getContext()).title(menuItem.getSubmenuTitle()).children(impls).data(menuItem.getAllData()).build();
                menuItemsChildren.add(innerMenuItem);
            } else {
                impls.forEach(impl -> impl.setAllData(menuItem.getAllData()));
                menuItemsChildren.addAll(impls);
            }
        }
        return menuItemsChildren;
    }
}
