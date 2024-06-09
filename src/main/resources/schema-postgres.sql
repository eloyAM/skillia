create sequence skill_id_seq start with 50 increment by 1;

create table person
(
    department varchar(255),
    email      varchar(255),
    full_name  varchar(255),
    title      varchar(255),
    username   varchar(255) not null,
    primary key (username)
);

create table person_skill
(
    level     integer      not null,
    skill_id  bigint       not null,
    person_id varchar(255) not null,
    primary key (skill_id, person_id)
);

create table skill
(
    id   bigint       not null,
    name varchar(255) not null unique,
    primary key (id)
);

alter table if exists person_skill
    add constraint FK__person_skill__person
        foreign key (person_id)
            references person
            on delete cascade;

alter table if exists person_skill
    add constraint FK__person_skill__skill
        foreign key (skill_id)
            references skill
            on delete cascade;
