package com.technolab.spring.views.library;

import com.technolab.spring.backend.Broadcaster;
import com.technolab.spring.backend.CrudMessage;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.shared.Registration;
import org.springframework.beans.factory.annotation.Autowired;

import com.technolab.spring.backend.UserService;
import com.technolab.spring.backend.User;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import com.technolab.spring.MainView;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.technolab.spring.ui.PaginatedGrid;

import java.util.List;


@Route(value = "user", layout = MainView.class)
@RouteAlias(value = "user", layout = MainView.class)
@PageTitle("Пользователи")
@CssImport("styles/views/library/library-view.css")
public class UserView extends Div implements AfterNavigationObserver {
    @Autowired
    BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private UserService service;

    Registration broadcasterRegistration;

    private PaginatedGrid<User> users;

    /**
     * Добавление листенера изменений
     *
     * @param attachEvent
     */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        broadcasterRegistration = Broadcaster.register(message -> processMessage(message));
    }

    /**
     * Удаление листенера изменений
     *
     * @param detachEvent
     */
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        broadcasterRegistration.remove();
        broadcasterRegistration = null;
    }

    /**
     * Обновление таблицы, если другой пользователь вносил правки
     *
     * @param message
     */
    private void processMessage(CrudMessage message) {
        if (message.getObject() instanceof User) {
            User user = (User) message.getObject();
            switch (message.getOperation()) {
                case INSERT:
                    insertUser(user, false);
                    break;
                case UPDATE:
                    updateUser(user, false);
                    break;
                case DELETE:
                    deleteUser(user, false);
                    break;
            }
        }
    }

    /**
     * Инициализация формы
     */
    public UserView() {
        setId("user-view");
        // Configure Grid
        users = new PaginatedGrid<>();
        users.setHeight("90%");
        users.setPageSize(5);
        users.setPaginatorSize(5);
        users.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        users.addColumn(User::getName).setHeader("Login").setSortable(true).setFlexGrow(1);
        users.addComponentColumn(this::createRemoveButton).setHeader("Удалить").setFlexGrow(0).setWidth("100px");
        users.addItemDoubleClickListener(this::onUserEdit);

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        setSizeFull();
        Button newButton = new Button("Добавить пользователя", this::onUserNew);
        layout.add(newButton);
        createGridLayout(layout);

        add(layout);
    }

    /**
     * Генерация кнопок удаления в таблице
     *
     * @param user
     * @return
     */
    private Button createRemoveButton(User user) {
        Button button = new Button();
        button.setIcon(new Icon(VaadinIcon.CLOSE));
        button.addClickListener(event -> {
            Dialog dialog = new Dialog();
            dialog.add(new Label("Подтвердите удаление"));
            Button confirmButton = new Button("OK", evt -> {
                deleteUser(user, true);
                dialog.close();
            });
            Button cancelButton = new Button("Отмена", evt -> {
                dialog.close();
            });
            dialog.add(confirmButton, cancelButton);
            dialog.open();
        });
        return button;
    }

    /**
     * Удаление пользователя
     *
     * @param user
     * @param localTransaction
     */
    private void deleteUser(User user, boolean localTransaction) {
        if (localTransaction) {
            service.deleteUser(user);
        } else {
            ListDataProvider<User> dataProvider = (ListDataProvider<User>) users.getProvider();
            dataProvider.getItems().remove(user);
            this.getUI().get().access(() -> {
                dataProvider.refreshAll();
                users.refreshPaginator();
            });
        }
    }

    /**
     * Обновление пользователя
     *
     * @param user
     * @param localTransaction
     */
    private void updateUser(User user, boolean localTransaction) {
        if (localTransaction) {
            service.updateUser(user);
        } else {
            ListDataProvider<User> dataProvider = (ListDataProvider<User>) users.getProvider();
            List items = ((List) dataProvider.getItems());
            if (items.contains(user)) {
                items.set(items.indexOf(user), user);
                this.getUI().get().access(() -> {
                    dataProvider.refreshItem(user);
                });
            } else {
                this.getUI().get().access(() -> {
                    afterNavigation(null);
                });
            }
        }
    }

    /**
     * Добавление пользователя
     *
     * @param user
     * @param localTransaction
     */
    private void insertUser(User user, boolean localTransaction) {
        if (localTransaction) {
            service.insertUser(user);
        } else {
            this.getUI().get().access(() -> {
                afterNavigation(null);
                users.select(user);
            });
        }
    }

    /**
     * Создание/изменение пользователя
     *
     * @param user
     */
    private void editUser(User user) {
        boolean isNew = user.getId() == null;
        Binder<User> binder;
        binder = new Binder<>(User.class);

        Dialog editorDialog = new Dialog();
        Div editorDiv = new Div();
        editorDiv.add(new Label("Пользователь"));
        editorDiv.setId("editor-layout");
        FormLayout formLayout = new FormLayout();
        TextField name = new TextField();
        PasswordField password = new PasswordField();

        Button save = new Button("OK", evt -> {
            user.setName(name.getValue());
            user.setPassword(passwordEncoder.encode(password.getValue()));
            try {
                if (isNew) {
                    insertUser(user, true);
                } else {
                    updateUser(user, true);
                }
                editorDialog.close();
            } catch (DataIntegrityViolationException ex) {
                (new Notification(
                        "Пользователь с таким именем уже существует", 3000,
                        Notification.Position.MIDDLE)).open();
            }
        });
        Button cancel = new Button("Отмена", evt -> {
            editorDialog.close();
        });
        addFormItem(editorDiv, formLayout, name, "name");
        addFormItem(editorDiv, formLayout, password, "password");
        createButtonLayout(editorDiv, cancel, save);
        editorDialog.add(editorDiv);
        binder.bind(name, "name");
        binder.bind(password, "password");
        binder.readBean(user);
        //Вывод зашифрованного пароля ничего не даст, пользователь должен вводить руками
        password.setValue("");
        editorDialog.open();
    }

    private void createButtonLayout(Div editorDiv, Button cancel, Button save) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setId("button-layout");
        buttonLayout.setWidthFull();
        buttonLayout.setSpacing(true);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(cancel, save);
        editorDiv.add(buttonLayout);
    }

    private void createGridLayout(VerticalLayout layout) {
        Div wrapper = new Div();
        wrapper.setId("wrapper");
        wrapper.setSizeFull();
        layout.add(wrapper);
        wrapper.add(users);
    }

    private void addFormItem(Div wrapper, FormLayout formLayout,
                             AbstractField field, String fieldName) {
        formLayout.addFormItem(field, fieldName);
        wrapper.add(formLayout);
        field.getElement().getClassList().add("full-width");
    }

    /**
     * Ленивая инициализация таблицы
     *
     * @param event
     */
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        users.setItems(service.getUsers());
    }

    /**
     * Редактирование элемента
     *
     * @param event
     */
    private void onUserEdit(ItemDoubleClickEvent<User> event) {
        editUser(event.getItem());
    }

    /**
     * Создание нового пользователя
     *
     * @param event
     */
    private void onUserNew(ClickEvent<Button> event) {
        editUser(new User());
    }
}
