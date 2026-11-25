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