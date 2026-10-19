# Hotel Management System

A console-based hotel management application written in **Java 21**, built around object-oriented design, clean layering and testable code.

It supports guest registration, automatic room allocation, check-out with a receipt, room history, and occupancy/profit reporting. Data is kept in Java collections and saved to a local file; **no database is used**.

## 1. Features

- Register guests; the system picks any free room (any class).
- Reject registration when all rooms are occupied, or when the same guest is already checked in.
- Check out a guest and print a final receipt (days stayed and total price) after confirmation.
- List occupied rooms and who lives in them.
- Show every room's status and its guest history (with check-in and check-out times, oldest first).
- Occupancy report: times occupied and profit per room, sorted from most to least occupied.
- Room count and prices are configurable through properties files.
- Data survives application restarts.
- A `0` input goes back to the main menu at any prompt.

## 2. Requirements and Technologies

- JDK 21 or later, a terminal or a Java IDE
- Java collections, records, sealed interfaces, pattern-matching `switch`, streams, NIO, `BigDecimal`
- No external libraries and no test framework

## 3. Build and Run

### Terminal (macOS / Linux)

```
mkdir -p /tmp/out
javac --release 21 -d /tmp/out $(find src -name '*.java')
cp resources/* /tmp/out/
java -cp /tmp/out com.hotel.Application
```

Run the last command from the project root: the data file `hotel-data.log` is created in the working directory.

### IntelliJ IDEA

1. Open the project folder and set the Project SDK and language level to 21.
2. Mark `src` as *Sources Root* and `test` as *Test Sources Root*.
3. Mark `resources` as *Resources Root* (recommended).
4. Run `com.hotel.Application` with the **working directory set to the project root**.

## 4. Menu

```
=== Hotel ===
1. Register guest
2. Check out guest
3. Occupied rooms
4. Room history and status
5. Occupancy report
0. Exit
```

Example history output:

```
Room 1 (STANDARD) - occupied
   1. Ann Lee | in: 2026-10-18 03:28 | out: 2026-10-18 05:01
   2. Rio Perera | in: 2026-10-18 06:10 | out: still staying
```

## 5. Architecture

Dependencies point inwards and always go through interfaces.

```
Application ──▶ HotelFactory (builds and wires everything)

ConsoleMenu ──▶ HotelFacade ──▶ RegistrationService  ──▶ RoomAllocationStrategy
                (interface)  ├▶ CheckOutService         ──▶ RoomRepository / StayRepository
                             ├▶ RoomDetailsService      ──▶ (interfaces)
                             └▶ ReportService                   │
                                                      InMemory / FileBacked repositories
```

| Package | Responsibility |
|---|---|
| `com.hotel` | `Application` (entry point) and `HotelFactory` (the only place that wires concrete classes) |
| `ui` | `ConsoleMenu`: input and output only, no business logic. Replaceable by a GUI. |
| `facade` / `facade.impl` | `HotelFacade`: the single API for any front end; `HotelFacadeImpl` delegates to the services |
| `service.<feature>` | Use-case interfaces and result types (`RegistrationResult`, `CheckOutResult`, `Receipt`, `RoomHistory`, `RoomReport`) |
| `service.<feature>.impl` | Implementations (`RegistrationServiceImpl`, `CheckOutServiceImpl`, `RoomDetailsServiceImpl`, `ReportServiceImpl`, `FirstFreeRoomStrategy`) |
| `model` | `Room` (sealed) → `StandardRoom` → `BusinessRoom` (inheritance: price × multiplier), `Guest`, `Stay` (records) |
| `repository` / `repository.memory` | `RoomRepository` and `StayRepository` interfaces; in-memory and file-backed implementations |
| `config` | `HotelConfig`: loads and validates properties |

### Design decisions

- **Facade + use-case interfaces:** the UI knows one interface; each service can be mocked in tests.
- **`RoomAllocationStrategy`:** "which room does a guest get" is a replaceable rule (default: first free room). A new rule needs no change to the registration service.
- **Repository pattern:** services never touch files; a JDBC implementation can replace the in-memory/file classes without touching business logic.
- **Sealed results and records:** registration and check-out return typed outcomes (e.g. `Registered`, `NoFreeRooms`, `AlreadyRegistered`) handled with exhaustive pattern matching.
- **`BigDecimal`** for all money values.
- **Price frozen per stay:** each `Stay` stores the daily price at registration, so later price changes do not alter past profit.

## 6. Business Rules

- The number of rooms is positive and even; rooms are numbered from 1.
- The first half are Standard rooms, the second half Business rooms.
- Standard price: 20 EUR per day. Business price: 50% higher (30 EUR with the default multiplier, 1.5).
- One guest per room; room selection is automatic.
- Charges are per calendar day, minimum 1 day (a same-day stay counts as 1 day).
- Profit in the report is the sum of the stays' recorded totals; active stays are charged up to today.
- A guest with the same name and surname (case-insensitive) cannot be checked in twice at the same time.
- Names and surnames: letters only, not empty.

## 7. Configuration

Files in `resources/`: `application.properties` plus an environment file (`application-dev.properties`, `application-prod.properties`; the file is optional, so `test` also works). The environment is chosen with `-Dapp.env=prod` or the `APP_ENV` variable (default `dev`); the environment file overrides the base file.

| Property | Description |
|---|---|
| `hotel.total.rooms` | Total rooms (positive, even) |
| `hotel.standard.price` | Standard room price per day |
| `hotel.business.multiplier` | Business price multiplier (> 1) |

Invalid values stop the application at start-up with a clear message. A restart is needed after changes; existing stays keep their original daily price.

## 8. Data Persistence

Stays are stored in `hotel-data.log` (working directory) as an **append-only journal**:

1. Every save appends one line for the changed stay (constant write cost).
2. On start-up the file is replayed; the latest line per stay wins.
3. If superseded lines or an incomplete last line exist, the file is compacted once.

Rooms are not persisted; they are rebuilt from configuration on every start.

## 9. Testing

The tests use no framework: a small `Check` helper and a runner that executes every `test*` method.

```
./run-tests.sh
```

This compiles `src` and `test`, copies the resources and runs all suites (84 tests). It exits with code 1 on any failure.

Coverage by area:

| Area | Scenarios |
|---|---|
| `config` | Room count even/positive, valid prices |
| `model` | Guest validation, room prices (standard / business), stay day and total calculation |
| `repository` | Id assignment, updates, active lookup, order, file persistence, append-only writes, compaction, partial last line, corrupt file |
| `service` | Registration (room order, full hotel, duplicates, invalid names), check-out (receipt, preview vs. final, unknown/free room, price changes), history, report profit, free-room check, allocation strategies |
| `facade` | Delegation to mocked use cases |
| `ui` | Console flows with scripted input: register, confirm/cancel, back option, check-out, history, report, and a fake `HotelFacade` |

Time-dependent logic is tested with an injectable `Clock` (multi-day and over-midnight stays).

## 10. Constraints and Limitations

- Single user, single process, console only.
- Names: Unicode letters only (no spaces, hyphens or apostrophes); two different people with the same name cannot be checked in at once.
- Room type cannot be chosen by the guest; no advance reservations; stays cannot be edited or cancelled.
- Check-in and check-out times are always "now"; billing uses calendar dates in the system time zone.
- Reducing the configured room count can hide history of removed rooms.
- File persistence suits small data volumes, not concurrent access.
- Currency is fixed to EUR.

## 11. Possible Improvements

- JDBC repositories; a Swing or REST front end on top of `HotelFacade`.
- Make `FileBackedStayRepository` a decorator instead of a subclass.
- Richer name validation and room-type selection.
