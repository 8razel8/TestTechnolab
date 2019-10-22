package com.technolab.spring.views.library;

import com.technolab.spring.MainView;
import com.technolab.spring.backend.*;
import com.technolab.spring.ui.PaginatedGrid;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.*;
import com.vaadin.flow.shared.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;


@Route(value = "", layout = MainView.class)
@RouteAlias(value = "book", layout = MainView.class)
@PageTitle("Книги")
@CssImport("styles/views/library/library-view.css")
public class BookView extends Div implements AfterNavigationObserver {

    @Autowired
    private BookService service;
    @Autowired
    private UserService userService;

    private String currentUser;

    Registration broadcasterRegistration;

    private PaginatedGrid<Book> books;

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
        if (message.getObject() instanceof Book) {
            Book book = (Book) message.getObject();
            switch (message.getOperation()) {
                case INSERT:
                    insertBook(book, false);
                    break;
                case UPDATE:
                    updateBook(book, false);
                    break;
                case DELETE:
                    deleteBook(book, false);
                    break;
            }
        }
    }

    /**
     * Инициализация формы
     */
    public BookView() {
        setId("book-view");
        // Configure Grid
        this.currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        books = new PaginatedGrid<>();
        books.setHeight("90%");
        books.setPageSize(5);
        books.setPaginatorSize(5);
        books.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        books.addColumn(Book::getIsbn).setHeader("ISBN").setSortable(true).setFlexGrow(1);
        books.addColumn(Book::getName).setHeader("Название").setSortable(true).setFlexGrow(1);
        books.addColumn(Book::getAuthor).setHeader("Автор").setSortable(true).setFlexGrow(1);
        books.addComponentColumn(this::createTakeButton).setHeader("Кем взята").setFlexGrow(1).setWidth("100px");
        books.addComponentColumn(this::createRemoveButton).setHeader("Удалить").setFlexGrow(0).setWidth("100px");
        books.addItemDoubleClickListener(this::onBookEdit);

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        setSizeFull();
        Button newButton = new Button("Добавить книгу", this::onBookNew);
        layout.add(newButton);
        createGridLayout(layout);

        add(layout);
    }

    /**
     * Управление книгой
     *
     * @param book
     * @return
     */
    private Component createTakeButton(Book book) {
        if (book.getReader() == null) {
            return new Button("Взять", event -> {
                book.setReader(userService.findUserByName(currentUser));
                updateBook(book, true);
            });
        } else {
            if (currentUser.equals(book.getReader().getName())) {
                return new Button("Вернуть", event -> {
                    book.setReader(null);
                    updateBook(book, true);
                });
            } else {
                return new Label(book.getReader().getName());
            }
        }
    }

    /**
     * Генерация кнопок удаления в таблице
     *
     * @param book
     * @return
     */
    private Button createRemoveButton(Book book) {
        Button button = new Button();
        button.setIcon(new Icon(VaadinIcon.CLOSE));
        button.addClickListener(event -> {
            Dialog dialog = new Dialog();
            dialog.add(new Label("Подтвердите удаление"));
            Button confirmButton = new Button("OK", evt -> {
                deleteBook(book, true);
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
     * Удаление книги
     *
     * @param book
     * @param localTransaction
     */
    private void deleteBook(Book book, boolean localTransaction) {
        if (localTransaction) {
            service.deleteBook(book);
        } else {
            ListDataProvider<Book> dataProvider = (ListDataProvider<Book>) books.getProvider();
            dataProvider.getItems().remove(book);
            this.getUI().get().access(() -> {
                dataProvider.refreshAll();
                books.refreshPaginator();
            });
        }
    }

    /**
     * Обновление книги
     *
     * @param book
     * @param localTransaction
     */
    private void updateBook(Book book, boolean localTransaction) {
        if (localTransaction) {
            service.updateBook(book);
        } else {
            ListDataProvider<Book> dataProvider = (ListDataProvider<Book>) books.getProvider();
            List items = ((List) dataProvider.getItems());
            if (items.contains(book)) {
                items.set(items.indexOf(book), book);
                this.getUI().get().access(() -> {
                    dataProvider.refreshItem(book);
                });
            } else {
                this.getUI().get().access(() -> {
                    afterNavigation(null);
                });
            }
        }
    }

    /**
     * Добавление книги
     *
     * @param book
     * @param localTransaction
     */
    private void insertBook(Book book, boolean localTransaction) {
        if (localTransaction) {
            service.insertBook(book);
        } else {
            this.getUI().get().access(() -> {
                afterNavigation(null);
                books.select(book);
            });
        }
    }

    /**
     * Создание/изменение книги
     *
     * @param book
     */
    private void editBook(Book book) {
        boolean isNew = book.getId() == null;
        Binder<Book> binder;
        binder = new Binder<>(Book.class);

        Dialog editorDialog = new Dialog();
        Div editorDiv = new Div();
        editorDiv.add(new Label("Книга"));
        editorDiv.setId("editor-layout");
        FormLayout formLayout = new FormLayout();
        TextField isbn = new TextField();
        TextField name = new TextField();
        TextField author = new TextField();

        Button save = new Button("OK", evt -> {
            book.setIsbn(isbn.getValue());
            book.setName(name.getValue());
            book.setAuthor(author.getValue());
            try {

                if (isNew) {
                    insertBook(book, true);
                } else {
                    updateBook(book, true);
                }
                editorDialog.close();
            } catch (DataIntegrityViolationException ex) {
                (new Notification(
                        "Ошибка проверки уникальности полей, проверьте ISBN", 3000,
                        Notification.Position.MIDDLE)).open();
            }
        });
        Button cancel = new Button("Отмена", evt -> {
            editorDialog.close();
        });
        addFormItem(editorDiv, formLayout, isbn, "ISBN");
        addFormItem(editorDiv, formLayout, name, "Название");
        addFormItem(editorDiv, formLayout, author, "Автор");
        createButtonLayout(editorDiv, cancel, save);
        editorDialog.add(editorDiv);
        binder.bind(name, "name");
        binder.bind(isbn, "isbn");
        binder.bind(author, "author");
        binder.readBean(book);
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
        wrapper.add(books);
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
        books.setItems(service.getBooks());
    }

    /**
     * Редактирование элемента
     *
     * @param event
     */
    private void onBookEdit(ItemDoubleClickEvent<Book> event) {
        editBook(event.getItem());
    }

    /**
     * Создание новой книги
     *
     * @param event
     */
    private void onBookNew(ClickEvent<Button> event) {
        editBook(new Book());
    }
}
