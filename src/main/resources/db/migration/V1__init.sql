create sequence skill_id_seq start with 50 increment by 1;
create sequence skill_tag_id_seq start with 50 increment by 1;
create sequence department__id_seq start with 50 increment by 1;
create sequence skill_group__id_seq start with 50 increment by 1;

create table person
(
    department varchar(50),
    email      varchar(254),
    full_name  varchar(100),
    title      varchar(50),
    username   varchar(40) not null,
    primary key (username)
);

create table person_skill
(
    level     integer     not null,
    skill_id  bigint      not null,
    person_id varchar(40) not null,
    primary key (skill_id, person_id)
);

create table skill
(
    id   bigint      not null,
    name varchar(70) not null unique,
    primary key (id)
);

create table skill_tag
(
    id   bigint      not null,
    name varchar(70) not null unique,
    primary key (id)
);

create table skill_tagging
(
    skill_id bigint not null,
    tag_id   bigint not null,
    primary key (skill_id, tag_id)
);

create table department
(
    id   bigint      not null,
    name varchar(50) not null unique,
    primary key (id)
);

create table skill_group
(
    id          bigint      not null,
    name        varchar(50) not null unique,
    description varchar(250),
    primary key (id)
);

create table skill_group_skills
(
    skill_id bigint not null,
    group_id bigint not null,
    primary key (skill_id, group_id)
);

create table department_skill_groups
(
    department_id bigint not null,
    group_id      bigint not null,
    primary key (department_id, group_id)
);

alter table person_skill
    add constraint FK__person_skill__person
        foreign key (person_id)
            references person (username)
            on delete cascade;

alter table person_skill
    add constraint FK__person_skill__skill
        foreign key (skill_id)
            references skill (id)
            on delete cascade;

alter table skill_tagging
    add constraint FK__skill_tagging__tag
        foreign key (tag_id)
            references skill_tag (id);
-- TODO adding "on delete cascade" is limited to the one side of the join
--  corresponding to the referenced type on the owning entity

alter table skill_tagging
    add constraint FK__skill_tagging__skill
        foreign key (skill_id)
            references skill (id)
            on delete cascade;

alter table skill_group_skills
    add constraint FK__skill_group_skills__skill_id
        foreign key (skill_id)
            references skill (id)
            on delete cascade;

alter table skill_group_skills
    add constraint FK__skill_group_skills__group_id
        foreign key (group_id)
            references skill_group (id);
--             on delete cascade;
-- TODO adding "on delete cascade" is limited to the one side of the join
--  corresponding to the referenced type on the owning entity

alter table department_skill_groups
    add constraint FK__department_skill_groups__department_id
        foreign key (department_id)
            references department (id)
            on delete cascade;

alter table department_skill_groups
    add constraint FK__department_skill_groups__group_id
        foreign key (group_id)
            references skill_group (id);
--             on delete cascade;
-- TODO adding "on delete cascade" is limited to the one side of the join
--  corresponding to the referenced type on the owning entity