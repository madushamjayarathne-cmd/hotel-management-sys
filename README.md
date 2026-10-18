# Hotel Management System – Documentation

Console application written in Java 21. No database; data is held in Java collections and optionally saved to a file.

## 1. How to build and run

```
mkdir -p /tmp/out
javac --release 21 -d /tmp/out $(find src -name '*.java')
cp resources/* /tmp/out/
java -cp /tmp/out com.hotel.Application
```

In an IDE: language level 21, `src` as source root, `resources` as resource root, run `com.hotel.Application`.
Data file `hotel-data.ser` is created in the working directory.

## 4. Business rules

- Even number of rooms, numbered 1..n; one guest per room.
- First half standard (base price, default 20 EUR per day), second half business (base × 1.5 = 30 EUR per day).
- Room is chosen by the system, not by the user.
- Charge = daily price × calendar days stayed (same-day stay = 1 day).
- Price is frozen on each stay, so history and profit stay correct after price changes.

## 5. Changing rooms and prices
Edit `resources/application*.properties`: `hotel.total.rooms` (even), `hotel.standard.price`, `hotel.business.multiplier`. No code change needed. Restart applies it to new stays; old stays keep their old price. If the room count is lowered below a room number that has saved stays, those stays are no longer shown in per-room views.

## 6. Constraints and limitations

- Console only; single user, single process.
- Names: letters only (Unicode letters allowed); no spaces, hyphens or apostrophes, so "Anna-Maria" is rejected.
- Room count must be even and positive; multiplier must be > 1; price > 0.
- No room-type choice, no pre-booking, no dates entered by the user: check-in time is "now" and check-out time is "now".
- Billing uses calendar dates in the system time zone; a stay over midnight counts the next day. Changing the system time zone or clock affects day counts.
- The report's profit includes guests still in the hotel (charged up to today).
- The check-in time is stored in the stay, but there is no cancellation or editing of stays; a guest cannot be registered twice at once only because each registration takes a new room (no duplicate-guest check).
- Each save rewrites the entire file (fine for small data, not for large).
- Prices use `BigDecimal` with 2 decimals; currency is EUR, fixed in the UI text.