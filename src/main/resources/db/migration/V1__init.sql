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
    level              integer     not null,
    skill_id           bigint      not null,
    person_id          varchar(40) not null,
    last_modified_date TIMESTAMP(6) WITH TIME ZONE,
    primary key (skill_id, person_id)
);

create table skill
(
    id          bigint      not null,
    name        varchar(70) not null unique,
    description varchar(250),
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

create index IDX__person_department on person (department);
create index IDX__department_skill_groups__department_id on department_skill_groups (department_id);
create index IDX__skill_group_skills__group_id on skill_group_skills (group_id);
create index IDX__skill_tagging__skill_id on skill_tagging (skill_id);

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


-- VIEWS

-- All person_skill along with person and skill details, if available
create
or replace view person_skill_view as
select ps.person_id          as username,
       p.full_name           as person_name,
       p.department          as person_department,
       ps.skill_id,
       s.name                as skill_name,
       ps.level              as skill_level,
       ps.last_modified_date as rating_date
from person_skill ps
         left join person p on p.username = ps.person_id
         left join skill s on s.id = ps.skill_id;

-- All skill along with skill_tag, if available
create
or replace view skill_with_tags_view as
select s.id          as skill_id,
       s.name        as skill_name,
       s.description as skill_description,
       t.id          as tag_id,
       t.name        as tag_name
from skill s
         left join skill_tagging st on st.skill_id = s.id
         left join skill_tag t on t.id = st.tag_id;

-- All skill_group along with skill, if available
create
or replace view skill_group_skills_view as
select g.id          as group_id,
       g.name        as group_name,
       g.description as group_description,
       s.id          as skill_id,
       s.name        as skill_name,
       s.description as skill_description
from skill_group g
         left join skill_group_skills gss on gss.group_id = g.id
         left join skill s on s.id = gss.skill_id;

-- All department along with skill_group, if available
create
or replace view department_skill_groups_view as
select d.id          as department_id,
       d.name        as department_name,
       g.id          as skill_group_id,
       g.name        as skill_group_name,
       g.description as skill_group_description
from department d
         left join department_skill_groups dsg on dsg.department_id = d.id
         left join skill_group g on g.id = dsg.group_id;

-- All department and person
-- Union instead of full join as not supported by H2
create
or replace view department_person_view as
select department.id     as department_id,
       department.name   as department_name,
       person.department as person_department,
       person.username,
       person.full_name  as person_full_name,
       person.email,
       person.title      as person_title
from department
         left join person on department.name = person.department
union
select department.id     as department_id,
       department.name   as department_name,
       person.department as person_department,
       person.username,
       person.full_name  as person_full_name,
       person.email,
       person.title      as person_title
from department
         right join person on department.name = person.department;