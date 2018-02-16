package com.eden.orchid.impl.themes.menus;

import com.eden.common.util.EdenUtils;
import com.eden.orchid.api.OrchidContext;
import com.eden.orchid.api.options.annotations.Description;
import com.eden.orchid.api.options.annotations.Option;
import com.eden.orchid.api.theme.menus.menuItem.OrchidMenuItemImpl;
import com.eden.orchid.api.theme.menus.menuItem.OrchidMenuItem;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public final class DividerMenuItem extends OrchidMenuItem {

    @Option
    @Description("An optional title for this divider, to create a contextual section within the menu.")
    public String title;

    @Inject
    public DividerMenuItem(OrchidContext context) {
        super(context, "separator", 100);
    }

    @Override
    public List<OrchidMenuItemImpl> getMenuItems() {
        List<OrchidMenuItemImpl> menuItems = new ArrayList<>();

        if(!EdenUtils.isEmpty(title)) {
            menuItems.add(new OrchidMenuItemImpl(context, title));
        }
        else {
            menuItems.add(new OrchidMenuItemImpl(context));
        }

        return menuItems;
    }
}
