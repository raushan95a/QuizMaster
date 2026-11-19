# 04 — Screens

JavaFX screens are built in Java code and styled with `client/styles.css`. `SceneManager` swaps screens in one window.
After login every screen has a `MenuBar`: File → Logout / Exit · Help → Keyboard Shortcuts (AWT) / About.

```
Login ⇄ Register
  ├─ Admin   → Admin Dashboard → Quiz Editor (questions inside)
  │                            → Live Results
  └─ Student → Student Dashboard → Quiz → Result → Review
                                 → History → Review
```

| # | Screen | Main controls |
|---|---|---|
| 1 | **Login** | `TextField`, `PasswordField`, Login button (Enter key works), link to Register, server URL shown at the bottom |
| 2 | **Register** | Full name, username, password, confirm password |
| 3 | **Admin Dashboard** | `ToolBar` (New, Edit, Delete, Publish/Unpublish, Live Results, Export CSV), `TableView` of quizzes |
| 4 | **Quiz Editor** | Title, subject, description, time limit `Spinner`; `ListView` of questions + Add/Edit/Delete; question dialog with 4 `TextField`s and `RadioButton`s for the correct option |
| 5 | **Live Results** | Quiz `ComboBox`; "Writing now" table (name, answered/total); "Submitted" table (rank, name, score, time); live-page link with Copy button; updates instantly (WebSocket), "● Live" status dot |
| 6 | **Student Dashboard** | Welcome label, `ListView` of published quizzes (cards with `DropShadow`), Start, My History |
| 7 | **Quiz** | Title, timer label (red under 1 min), `ProgressBar`, question text, 4 `RadioButton`s in a `ToggleGroup`, Previous/Next, numbered jump buttons, Submit |
| 8 | **Result** | Score with fade/scale animation, correct/wrong/unanswered, time taken, Review button |
| 9 | **Review** | Each question with your answer (✓ green / ✗ red) and the correct answer |
| 10 | **History** | `TableView` of own attempts; double-click → Review |
| — | **AWT Help window** | `Frame` + `TextArea` + Close `Button` (`ActionListener`, Esc via `KeyListener`, `WindowAdapter`) |

## Quiz screen
```
┌──────────────────────────────────────────────────────────┐
│ Java Basics                           ⏱ 09:42   [Submit] │
│ ████████████░░░░░░░░  Question 4 of 10                   │
├──────────────────────────────────────────────────────────┤
│  Which keyword prevents a method from being overridden?  │
│   ( ) A. static        (•) B. final                      │
│   ( ) C. private       ( ) D. abstract                   │
├──────────────────────────────────────────────────────────┤
│ [◄ Previous]   1 2 3 [4] 5 6 7 8 9 10   [Next ►]          │
└──────────────────────────────────────────────────────────┘
```

## Live page (`/live.html?quiz=<id>`, browser)
One HTML file with a little JavaScript, served by the server. Connects to `/ws/live?quiz=<id>`, re-renders on
every message, shows "Reconnecting…" and retries every 3 s if the connection drops. Large text for a projector, works on phones.
```
QuizMaster LIVE · Java Basics                          ● Live
Writing now (3):  Kiran 6/10 · Anu 2/10 · Ravi 9/10
🏆 Leaderboard
 1. Ravi   9/10   4:12
 2. Anu    8/10   5:01
```
