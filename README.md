# OrangeHRM Selenium Test Framework

Java-based automated tests for OrangeHRM workflows. The project uses Selenium
WebDriver and TestNG for browser tests, Rest Assured for an API check, Apache
POI to read Excel test data, and ExtentReports for HTML reporting.

## Contents

- [Features](#features)
- [Technology stack](#technology-stack)
- [Project layout](#project-layout)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Run the tests](#run-the-tests)
- [Selenium Grid with Docker Compose](#selenium-grid-with-docker-compose)
- [Test coverage](#test-coverage)
- [Reports](#reports)
- [Jenkins](#jenkins)
- [Troubleshooting](#troubleshooting)

## Features

- Page Object Model for login and home-page interactions.
- Selenium tests can use a remote Selenium Grid or locally installed browsers.
- TestNG suite and data providers, including workbook-driven login and employee
  test data.
- API response checks with Rest Assured.
- Optional employee-record verification against a local OrangeHRM MySQL
  database.
- ExtentReports output and Log4j2 logging.

## Technology stack

| Component | Version |
| --- | --- |
| Java (LTS) | 25 |
| Maven | 3.9.x |
| Selenium Java | 4.27.0 |
| TestNG | 7.10.2 |
| Rest Assured | 5.5.0 |
| Apache POI | 5.4.0 |
| ExtentReports | 5.1.2 |
| Selenium Grid images | 4.34.0 |

## Project layout

```text
.
├── docker/
│   └── docker-compose.yml       # Selenium Hub and browser nodes
├── src/
│   ├── main/
│   │   ├── java/com/orangehrm/
│   │   │   ├── actiondriver/    # Shared WebDriver actions
│   │   │   ├── base/            # Browser lifecycle and configuration
│   │   │   ├── listeners/       # TestNG and ExtentReports lifecycle
│   │   │   ├── pages/           # Page objects
│   │   │   └── utilities/       # API, database, Excel, reporting utilities
│   │   └── resources/
│   │       ├── config.properties
│   │       └── log4j2.xml
│   └── test/
│       ├── java/com/orangehrm/test/
│       └── resources/
│           ├── testng.xml
│           └── testdata/TestData.xlsx
├── Jenkinsfile
└── pom.xml
```

## Prerequisites

- A JDK 25 installation and Maven 3.9 or later.
- A running OrangeHRM instance for UI tests. The database verification test
  additionally needs the matching OrangeHRM MySQL database and employee test
  records.
- Either:
  - Docker Desktop with Docker Compose and access to the Selenium images, for
    the Grid-based suite; or
  - Locally installed browser(s) and compatible WebDriver binaries, for local
    browser execution. Selenium Manager may download drivers when network
    access permits.
- Network access to `jsonplaceholder.typicode.com` for the API test.

The Compose file starts Selenium Grid only. It does **not** install or start
OrangeHRM or MySQL. The configured Grid application URL uses
`host.docker.internal`, so the OrangeHRM service must be reachable from the
Docker host at that address and path.

## Configuration

Runtime settings are in
[`src/main/resources/config.properties`](src/main/resources/config.properties).
The main options are:

| Property | Purpose |
| --- | --- |
| `seleniumGrid` | `true` uses the configured remote Grid; `false` launches local browsers. |
| `gridURL` | Selenium Grid WebDriver endpoint. |
| `url_grid` | OrangeHRM URL reachable from a Grid browser container. |
| `url_local` | OrangeHRM URL opened by a locally launched browser. |
| `implicitWait`, `explicitWait` | Selenium element wait durations in seconds. |

The launcher accepts JVM overrides for local test runs:

- `-DseleniumGrid=false` selects local browsers.
- `-Dbrowser=chrome`, `-Dbrowser=firefox`, or `-Dbrowser=edge` selects the
  browser for suite entries.
- `-DappUrl=https://your-orangehrm-host/` overrides the configured application
  URL.

Set valid OrangeHRM credentials in the environment rather than committing them
to configuration or test data:

```powershell
$env:ORANGEHRM_USERNAME = "your-test-user"
$env:ORANGEHRM_PASSWORD = "your-test-password"
```

The same values can be supplied as JVM properties:
`-Dorangehrm.username=...` and `-Dorangehrm.password=...`. The valid-login
workbook row uses environment-variable placeholders; the data provider resolves
them at runtime and reports a clear error if either value is missing. Use a
dedicated test account and never commit credentials.

The database utility currently expects a local MySQL service on
`localhost:3306`, schema `orangehrm`, username `root`, and an empty password.
Adjust its connection settings in `DBConnection` if your test database differs.

## Run the tests

From the repository root, check the JDK and Maven:

```powershell
java -version
mvn -version
```

Build without running tests:

```powershell
mvn clean package -DskipTests
```

Run the TestNG suite against Selenium Grid (Grid and the required OrangeHRM
application/database must already be available):

```powershell
mvn clean test -DseleniumGrid=true
```

Run locally with Chrome and an explicitly selected application URL:

```powershell
mvn clean test `
  -DseleniumGrid=false `
  -Dbrowser=chrome `
  -DappUrl=https://your-orangehrm-host/
```

The suite configuration in
[`src/test/resources/testng.xml`](src/test/resources/testng.xml) is the
canonical test selection. It includes Chrome, Edge, and Firefox suite entries;
when using local execution, the browser override makes those entries launch
the selected local browser.

## Selenium Grid with Docker Compose

Start the Grid from the repository root:

```powershell
docker compose -f docker/docker-compose.yml up -d
```

Check Grid readiness:

```powershell
Invoke-RestMethod http://localhost:4444/status
```

When finished:

```powershell
docker compose -f docker/docker-compose.yml down
```

The Grid publishes ports `4442`, `4443`, and `4444`. The `chrome`, `firefox`,
and `edge` nodes are configured as headless browser containers.

## Test coverage

The TestNG suite currently includes:

- **Login tests:** a successful login/logout flow and invalid-credential
  validation using workbook data.
- **Home-page test:** OrangeHRM logo visibility after login.
- **Employee/database test:** employee lookup through the PIM page and
  comparison with MySQL records.
- **API test:** checks user 1 from JSONPlaceholder, including the response
  status, username, and email.
- **Browser smoke tests:** check the OrangeHRM page title.

The UI and database checks depend on valid application data and credentials.
The public OrangeHRM demo is not a substitute for the configured local database
or its employee fixtures.

## Reports

- Surefire/TestNG reports: `target/surefire-reports/`
- Extent report: `src/test/resources/ExtentReport/ExtentReport.html`
- Log files: `logs/`

Generated build output, logs, and report files are excluded from Git by
[`.gitignore`](.gitignore).

## Jenkins

The [`Jenkinsfile`](Jenkinsfile) expects Jenkins global tools named
`jdk-25` and `maven-3.9.9`. The agent also needs Docker Compose, access to the
Selenium images, and a reachable OrangeHRM instance/database for the UI and
database tests. Configure the OrangeHRM test credentials as Jenkins
environment variables (`ORANGEHRM_USERNAME` and `ORANGEHRM_PASSWORD`) rather
than adding them to the pipeline or repository.

## Troubleshooting

| Symptom | Likely cause and action |
| --- | --- |
| `SessionNotCreatedException` from `RemoteWebDriver` | The Grid at `gridURL` is unavailable or has no matching browser node. Start Compose and check `http://localhost:4444/status`. |
| Local Edge session fails with a driver-version message | Install/update EdgeDriver so its major version matches Microsoft Edge, or use another installed browser. |
| Browser reaches `localhost` but the OrangeHRM page is unavailable | `url_local` points to a local application that is not running. Start OrangeHRM or pass a reachable URL with `-DappUrl=...`. |
| Valid-login test reports a missing credential | Set `ORANGEHRM_USERNAME` and `ORANGEHRM_PASSWORD` (or the corresponding JVM properties) to valid test-account credentials. |
| PIM/database test fails after login | Confirm the account can access PIM, the expected employee record exists, and MySQL is available with the configured schema and connection settings. |
| API test cannot reach JSONPlaceholder | Check outbound network access to `jsonplaceholder.typicode.com`. |
