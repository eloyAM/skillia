create sequence idgenerator start with 1000 increment by 50;

--

create table company
(
    version integer not null,
    id      bigint  not null,
    name    varchar(255),
    primary key (id)
);

--

create table contact
(
    version    integer not null,
    company_id bigint  not null,
    id         bigint  not null,
    status_id  bigint  not null,
    email      varchar(255),
    first_name varchar(255),
    last_name  varchar(255),
    primary key (id)
);

--

create table status
(
    version integer not null,
    id      bigint  not null,
    name    varchar(255),
    primary key (id)
);

--

alter table if exists contact
    add constraint FKpgbqt6dnai52x55o1qvsx1dfn
        foreign key (company_id)
            references company;

alter table if exists contact
    add constraint FKtp0gbknv4j92yko7ucc3tpp2y
        foreign key (status_id)
            references status;
