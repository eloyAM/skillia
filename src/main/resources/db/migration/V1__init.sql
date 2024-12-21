create sequence skill_id_seq start with 50 increment by 1;
create sequence skill_tag_id_seq start with 50 increment by 1;

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

create table skill_tag
(
    id   bigint       not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table skill_tagging
(
    skill_id bigint not null,
    tag_id   bigint not null,
    primary key (skill_id, tag_id)
);

alter table person_skill
    add constraint FK__person_skill__person
        foreign key (person_id)
            references person(username)
            on delete cascade;

alter table person_skill
    add constraint FK__person_skill__skill
        foreign key (skill_id)
            references skill(id)
            on delete cascade;

alter table skill_tagging
    add constraint FK__skill_tagging__tag
        foreign key (tag_id)
            references skill_tag(id);   -- TODO add on delete cascade when implemented in the model

alter table skill_tagging
    add constraint FK__skill_tagging__skill
        foreign key (skill_id)
            references skill(id)
            on delete cascade;