# Skillia

Skillia is a simple web skills tracker/matrix fully developed on Java
(Spring Boot 3 for the backend and Vaadin 24 for the frontend).

It is designed for an enterprise environment, authentication and authorization happens through an LDAP server,
and the users' information is extracted from there, including their department and job title, apart from the
username, full name and email.

It is aimed to rate and keep track of the employees' skills.
Each skill can be rated with a level from 1 to 5 (the higher, the better),
either by the HR department or by each employee itself.
Any user can consult the employees skill matrix and any user profile.
People in the HR department are in charge of creating the skills,
skill tags and group of skills and assign those groups to departments
(where a group might be assigned to multiple departments).
The relevant skills for each employee are therefore determined based on their department.

The HR role is based on LDAP group membership (your_domain->groups->hr).
No other role is taken into account.

Default user data mapping from LDAP (given an `inetOrgPerson` object class):

| User field | LDAP attribute     |
|------------|--------------------|
| Username   | `uid`              |
| Full name  | `cn`               |
| Email      | `mail`             |
| Title      | `title`            |
| Department | `departmentNumber` |

A REST API is exposed following the `/api` path.
The OpenAPI documentation is automatically generated and retrievable at `/api/openapi/v3/api-docs`
and the Swagger UI can be accessed through `/swagger-ui/index.html`

## Try it

It runs by default on port 8080, or the one defined in the environment var `PORT`.

An example docker-compose file is included to run the app (the jar must be built previously), along with
a PostgreSQL database and an OpenLDAP server which gets populated from the `skillia.ldif` file.
There is also a LAM (LDAP Account Manager) instance configured allowing to manage the LDAP server entries.

Build the app in production mode by executing `./mvnw vaadin:clean-frontend clean package -Pproduction`.
This will build both the frontend and backend and then generate an executable `.jar` file.
Without specifying the production profile, the app is built in dev mode,
including the Spring Boot Dev Tools and a dev server for the frontend for faster reloads.
This way, the dependencies will be embedded, running an H2 in-memory database, and an UnboundID in-memory LDAP server.
Anyway, multiple spring/maven profiles are available to choose how to run/connect to the external dependencies,
including:

- h2 (active by default)
- embedded-ldap (active by default)
- embedded-postgres
- embedded-mariadb
- postgres
- mariadb
- mssql
- ...

Database operations are mainly based on JPA and the database initialization script (managed by Flyway) uses generic SQL,
so it shouldn't take much work to run the app with a relational database other than PostgreSQL.

For the sample LDAP instance, all the users have the same password: `1234`.

The users with full access are:

- `hugo.reyes`
- `raquel.huertas`

Some of the standard users are:

- `andrea.riquelme`
- `paz.vidal`
- `jacob.smith`

Each time the app starts, it will populate its own database with the users and departments from the LDAP server.
Some sample skills and other data are added if no other data exists
(can be disabled with the `initialization.create-sample-data` property set to `false`).

## Test it

`./mvnw vaadin:clean-frontend clean verify -Pit`

There are integration tests for both the backend and the frontend.
Some of them use [TestContainers](https://testcontainers.com/), which requires a docker environment.

There are some [Selenium](https://www.selenium.dev/) tests, which you may run in headless mode with the option
`webdriver.headless=true` (false by default).

They run on Chrome, and the binary can be supplied with the option `webdriver.chrome.binary=<chrome_binary_path>`,
otherwise, it will try to find it based on your `PATH` and common installation locations.
The driver will be downloaded automatically (for stable versions only).

Selenium tests may fail if you use a development build, as in such a case,
the frontend is built during runtime instead of compile time, causing a timeout.