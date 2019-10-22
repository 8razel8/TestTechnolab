create table USER
(
    ID   INT AUTO_INCREMENT PRIMARY KEY,
    NAME varchar(100) not null UNIQUE,
    PWD  varchar(100) not null
);
create table BOOK
(
    ID      INT AUTO_INCREMENT PRIMARY KEY,
    ISBN    varchar(17)  not null UNIQUE,
    AUTHOR  varchar(100) not null,
    NAME    varchar(100) not null,
    USER_ID INT
);

ALTER TABLE BOOK
    ADD CONSTRAINT BOOK_USER FOREIGN KEY (USER_ID)
        REFERENCES USER (ID)
        ON UPDATE CASCADE ON DELETE SET NULL;

insert into USER (NAME, PWD)
values ('user1', '$2a$10$QAAc/MT8LBwfConldG5b5efzLQUz9iy6EjWD7VLutKvihwT2A0VuS');--pwd=test
insert into USER (NAME, PWD)
values ('user2', '$2a$10$QAAc/MT8LBwfConldG5b5efzLQUz9iy6EjWD7VLutKvihwT2A0VuS');--pwd=test
insert into USER (NAME, PWD)
values ('user3', '$2a$10$QAAc/MT8LBwfConldG5b5efzLQUz9iy6EjWD7VLutKvihwT2A0VuS');--pwd=test

insert into BOOK (ISBN, AUTHOR, NAME)
values ('978-5-390-00143-3', 'А.В. Аверин', 'Управление персоналом, кадровая и социальная политика в организации');
insert into BOOK (ISBN, AUTHOR, NAME)
values ('5-9487-00899-2', 'В.В Адамчук', 'Экономика и социология труда');
insert into BOOK (ISBN, AUTHOR, NAME)
values ('5-188-04022-2', 'Л.Л. Ермолович', 'Анализ эффективности использования рабочей силы');
insert into BOOK (ISBN, AUTHOR, NAME)
values ('978-5-238-02321-5', 'И. Ансофф', 'Стратегическое управление');
insert into BOOK (ISBN, AUTHOR, NAME)
values ('978-5-390-03143-3', 'Л. Е. Басовский', 'Комплексный экономический анализ хозяйственной деятельности');
insert into BOOK (ISBN, AUTHOR, NAME)
values ('5-188-14022-2', 'С. Белозерова', 'Социальные аспекты трансформации трудовых отношений в промышленности');
insert into BOOK (ISBN, AUTHOR, NAME)
values ('5-9487-01899-2', 'И.А. Бланк', 'Менеджмент');
insert into BOOK (ISBN, AUTHOR, NAME)
values ('978-5-9273-1527-7', 'А.Н. Богатко', 'Система управления развитием предприятия');
insert into BOOK (ISBN, AUTHOR, NAME)
values ('455-5-480-01323-3', 'Е. Борисова', 'Планирование персонала легко в теории, сложно на практике');
insert into BOOK (ISBN, AUTHOR, NAME)
values ('978-5-390-02143-3', ' А.П. Волгин, В.И. Матирко', 'Управление персоналом в условиях рыночной экономики');
insert into BOOK (ISBN, AUTHOR, NAME)
values ('978-5-238-12321-5', 'Н.А. Волгин', 'Стимулирование производственного труда');
insert into BOOK (ISBN, AUTHOR, NAME)
values ('978-5-9273-2527-7', 'Н.М. Воловская', 'Экономика и социология труда');
insert into BOOK (ISBN, AUTHOR, NAME)
values ('455-6-480-01323-3', 'Б.М. Генкин', 'Экономика и социология труда');
insert into BOOK (ISBN, AUTHOR, NAME)
values ('978-5-390-01143-3', 'Е.С. Гордеева', 'Кадровый резерв как эффективная система');
insert into BOOK (ISBN, AUTHOR, NAME)
values ('5-288-04022-2', 'Е.А. Дубова', 'Управление персоналом в быстрорастущих компаниях');

