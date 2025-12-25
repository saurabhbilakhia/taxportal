# Task File: React UI for Client Portal (Kotlin Backend)

## Objective

Build a **mobile-first React user interface** that consumes APIs from the existing **`clientportal` Kotlin module**.
The UI must support **clients and accountants**, covering authentication, account management, client workflows, and accountant dashboards.

---

## Tech Stack & Standards

* **Frontend Framework:** React (latest stable)
* **Language:** TypeScript
* **State Management:** React Query (preferred) or Redux Toolkit
* **Routing:** React Router
* **Styling:** Tailwind CSS (mobile-first)
* **API Communication:** Axios or Fetch
* **Form Handling:** React Hook Form + Zod/Yup validation
* **Authentication:** JWT-based (as provided by backend APIs)

---

## Design & UX Requirements

### Mobile-First Design

* Design **mobile screens first**, then scale up to tablet and desktop.
* Use responsive breakpoints:

  * Mobile: default
  * Tablet: `md`
  * Desktop: `lg` and above
* All layouts must work **one-handed on mobile**.

### Color Scheme (Mandatory)

* **Text color:** `#1B4332`
* **Primary background:** `#FFFFFF`
* **Secondary background:** `#d8f3dc`

### Typography & UI Tone

* Clean, professional, tax/finance-grade UI
* Clear hierarchy: headings, labels, helper text
* Large tap targets for mobile users

---

## Architecture Instructions

### Project Structure

```
src/
 ├─ api/
 │   ├─ auth.ts
 │   ├─ client.ts
 │   ├─ accountant.ts
 │   └─ common.ts
 ├─ components/
 │   ├─ forms/
 │   ├─ layout/
 │   ├─ navigation/
 │   └─ shared/
 ├─ pages/
 │   ├─ auth/
 │   ├─ client/
 │   ├─ accountant/
 │   └─ error/
 ├─ hooks/
 ├─ types/
 └─ utils/
```

### API Integration Rules

* Each Kotlin API endpoint must have:

  * A **typed request model**
  * A **typed response model**
* Centralize error handling (401, 403, 500)
* Automatically redirect to login on **401 Unauthorized**

---

## Screens to Build

### 1. Login Screen

**Purpose:** Authenticate client or accountant

**Features**

* Email + password fields
* Role detection (client/accountant from API response)
* Loading and error states
* Forgot password link

**Post-Login Routing**

* Client → Client Portal
* Accountant → Accountant Dashboard

---

### 2. Sign Up Screen

**Purpose:** New client registration

**Features**

* Name, email, password, confirm password
* Validation (password strength)
* API-driven error handling
* Success confirmation screen

---

### 3. Change Password Screen

**Purpose:** Security management

**Features**

* Current password
* New password + confirmation
* Enforce backend password rules
* Logout after successful change

---

### 4. Client Portal

**Purpose:** Core client experience

**Must Include (based on APIs)**

* Profile summary
* Uploaded tax documents (T4, T2202, etc.)
* Upload new documents
* View filing status
* Payment status and receipts
* Notifications/messages from accountant

**UX Notes**

* Timeline-style status tracking
* Clear CTA buttons
* File upload optimized for mobile camera uploads

---

### 5. Accountant Dashboard

**Purpose:** Operational dashboard for accountants

**Must Include**

* List of client tax filings
* Status filters (new, in-progress, completed)
* Client detail view
* Document review access
* Messaging/notes section
* Payment confirmation indicators

**UX Notes**

* Table → card layout on mobile
* Bulk actions on desktop
* Fast filtering and search

---

### 6. Additional UI (As Required by APIs)

Build UI for **all remaining clientportal APIs**, including but not limited to:

* Notifications
* Payment flows
* Audit logs (if exposed)
* Admin or role-based views

Each API must have:

* A corresponding UI
* Proper empty/loading/error states
* Role-based access control

---

## Navigation & Layout

* Bottom navigation for mobile (Client Portal)
* Sidebar navigation for desktop (Accountant)
* Sticky headers for key actions
* Logout accessible on all screens

---

## Security & Access Control

* Protect routes using role-based guards
* Do not render accountant views to clients
* Token stored securely (HTTP-only cookie preferred if supported)

---

## Quality & Delivery Expectations

* Fully responsive
* No hardcoded API responses
* No inline styles
* Reusable components
* Clean, readable code
* Ready for production handoff

---

## Output Required from Opus 4.5

* Complete React UI codebase
* All screens wired to APIs
* Responsive layouts
* Clear separation of concerns
* Consistent usage of color palette and mobile-first design

---
