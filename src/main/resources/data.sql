-- INSERT INTO skill (name)
-- VALUES ('C++'),
--        ('Java'),
--        ('English'),
--        ('Communication'),
--        ('Testing'),
--        ('Open source'),
--        ('Python');

-- insert into skill(id, name) values (nextval('skill_id_seq'), 'sequencing');
-- To avoid conflicts with the auto-generated ids, instead of getting the next val to insert, the corresponding sequence
-- would have a start value greater than the last inserted one manually

INSERT INTO skill (id, name)
VALUES (1, 'C++'),
       (2, 'Java'),
       (3, 'English'),
       (4, 'Communication'),
       (5, 'Testing'),
       (6, 'Open source'),
       (7, 'Python');

INSERT INTO person (username, email, display_name, title, department)
VALUES ('eloy.abellan', 'eloy.abellan@example.com', 'Eloy Abellán Mayor', 'Junior Engineer', 'Innovation'),
       ('juan.canovas', 'juan.canovas@example.com', 'Juan Cánovas Hernández', 'Senior Engineer', 'Development'),
       ('jacob.smith', 'jacob.smith@example.com', 'Jacob Smith', 'Head Of Accounting', 'Accounting'),
       ('hernan.cortes', NULL, NULL, NULL, NULL);

INSERT INTO person_skill (person_id, skill_id, level)
VALUES ('eloy.abellan', 1, 3),
       ('eloy.abellan', 2, 2),
       ('juan.canovas', 1, 4),
       ('jacob.smith', 2, 5),
       ('jacob.smith', 1, 1);
