package com.technolab.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.technolab.spring.views.library.BookView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabVariant;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import com.technolab.spring.views.library.UserView;

/**
 * Одностраничная реализация
 */
@Push
@JsModule("./styles/shared-styles.js")
@PWA(name = "library", shortName = "library")
@Theme(value = Lumo.class, variant = Lumo.LIGHT)
public class MainView extends AppLayout {

    private final Tabs menu;

    public MainView() {
        menu = createMenuTabs();
        addToNavbar(menu);
    }

    /**
     * Создание панели навигации
     *
     * @return
     */
    private static Tabs createMenuTabs() {
        final Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.HORIZONTAL);
        tabs.add(getAvailableTabs());
        tabs.setSizeFull();
        return tabs;
    }

    /**
     * Инициализация CRUD страниц
     *
     * @return
     */
    private static Tab[] getAvailableTabs() {
        final List<Tab> tabs = new ArrayList<>();
        tabs.add(createTab("Книги", BookView.class));
        tabs.add(createTab("Пользователи", UserView.class));
        return tabs.toArray(new Tab[tabs.size()]);
    }

    private static Tab createTab(String title,
                                 Class<? extends Component> viewClass) {
        return createTab(populateLink(new RouterLink(null, viewClass), title));
    }

    private static Tab createTab(Component content) {
        final Tab tab = new Tab();
        tab.addThemeVariants(TabVariant.LUMO_ICON_ON_TOP);
        tab.add(content);
        return tab;
    }

    private static <T extends HasComponents> T populateLink(T a, String title) {
        a.add(title);
        return a;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        selectTab();
    }

    /**
     * Переключение между страницами
     */
    private void selectTab() {
        String target = RouteConfiguration.forSessionScope()
                .getUrl(getContent().getClass());
        Optional<Component> tabToSelect = menu.getChildren().filter(tab -> {
            Component child = tab.getChildren().findFirst().get();
            return child instanceof RouterLink
                    && ((RouterLink) child).getHref().equals(target);
        }).findFirst();
        tabToSelect.ifPresent(tab -> menu.setSelectedTab((Tab) tab));
    }
}
