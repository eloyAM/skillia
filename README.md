# Skillia

Skillia is a simple web skills tracker/matrix developed with Java
(Spring Boot 3 for backend and Vaadin 24 for the frontend).

It is designed for enterprises, the login is done through an LDAP server,
and the user's info includes their department and job title, apart from the
username, full name and email.

It allows the people in the HR department to create skills and assign a rating for each employee
from 1 to 5, the higher, the better.
Any user can then see and filter the skilled employees.
The HR role is based on LDAP group membership (your_domain->groups->hr).
No other roles are considered.

A REST API is available as well, under the domain `/api`.
The OpenAPI doc is automatically generated and retrievable at `/api/openapi/v3/api-docs`
and the Swagger UI can be accessed through `/swagger-ui/index.html`

## Try it

By default, it runs on port 8080, or the one defined in the environment var `PORT`.

A docker-compose file is included in this repo which includes the app,
a PostgreSQL database and an OpenLDAP server which gets populated from the `skillia.ldif` file.

Previously build the app in production mode with `./mvnw vaadin:clean-frontend clean package -Pproduction`.
This will build both the frontend and backend and then generate a `.jar` file.
Without the production profile, the app is built in dev mode,
including the Spring Boot Dev Tools and a dev server for the frontend for faster reloads.

All the users have the same password: `1234`.

The users with full access are:

- `hugo.reyes`
- `raquel.huertas`

Some standard users are:

- `andrea.riquelme`
- `paz.vidal`
- `jacob.smith`

Each time the app starts, it will populate its users DB from the LDAP server.

## Test it

There are integration tests for both the backend and the frontend.
Some of them use [TestContainers](https://testcontainers.com/), which requires a docker environment.

There are some [Selenium](https://www.selenium.dev/) tests, which you may run in headless mode with the option
`webdriver.headless=true` (false by default).

They run on Chrome, and the binary can be supplied with the option `webdriver.chrome.binary=<chrome_binary_path>`,
otherwise, it will try to find it based on your `PATH` and common installation locations.
The driver will be downloaded automatically (for stable versions only).

Selenium tests may fail if you use a development build, as in such a case,
the frontend is built during runtime instead of compile time, causing a timeout.