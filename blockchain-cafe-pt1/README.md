# Blockchain Café – Onboarding (Part 1)

## Was ist das?
Statische HTML-Seite für das Business-Onboarding.
Passt 1:1 zum Design des bestehenden `paste.txt` Codes.

## Starten in IntelliJ
1. `File → Open` → diesen Ordner wählen
2. `onboarding.html` öffnen → Rechtsklick → **Open in → Browser**
   (oder den "Browser-Button" oben rechts im Editor klicken)

Fertig. Kein `npm install`, kein Server nötig.

## Demo-Account (vorausgefüllt)
- E-Mail: `demo@blockchain.cafe`
- Passwort: `demo123`

## Was ist enthalten?
- Login / Registrierung (mit localStorage)
- 5-Schritt Onboarding:
  1. Account erstellt (automatisch)
  2. Unternehmen registrieren (Formular)
  3. Identität verifizieren (simuliert, 2,4s)
  4. USt-ID prüfen via EU VIES (echter API-Call via `/api/verify-vat`)
  5. E-Mail bestätigen (simuliert)
- Karten hinzufügen (nach Abschluss)
- Fortschrittsbalken & Sidebar-Status
- Identisches Design wie bestehender Code

## Hinweis: VIES API
Schritt 4 (USt-ID) ruft `/api/verify-vat` auf.
Das braucht einen laufenden Backend-Server.
Für reinen UI-Test kannst du in `checkVat()` den fetch() durch eine Simulation ersetzen.

## Nächste Schritte (Part 2)
- Integration mit deinem Java-Backend
- VIES-Proxy Route (`/api/verify-vat`)
- Mehr Seiten (z. B. Wallet, Rechnungen)
