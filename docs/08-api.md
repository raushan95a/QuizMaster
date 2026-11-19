# 08 — API

JSON over HTTP. Logged-in requests send `Authorization: Bearer <token>`.
Errors: `{"error": "message"}` with status 400 / 401 / 403 / 404 / 409 / 500.

## Public
| Method | Path | Body → Response |
|---|---|---|
| GET | `/health` | → `{"status":"UP"}` |
| POST | `/api/auth/register` | `{fullName, username, password}` → 201 `{token, user}` |
| POST | `/api/auth/login` | `{username, password}` → `{token, user:{id, username, fullName, role}}` |
| GET | `/api/live/{quizId}` | → `LiveState` |
| WS | `/ws/live?quiz={quizId}` | server pushes `LiveState` on connect and after every change |

## Any logged-in user
| Method | Path | Response |
|---|---|---|
| GET | `/api/auth/me` | → `user` |
| POST | `/api/auth/logout` | → 204 |

## Admin
| Method | Path | Body → Response |
|---|---|---|
| GET | `/api/admin/quizzes` | → `[{id, title, subject, timeLimitMin, published, questionCount}]` |
| POST | `/api/admin/quizzes` | `{title, subject, description, timeLimitMin}` → 201 quiz |
| GET / PUT / DELETE | `/api/admin/quizzes/{id}` | quiz with questions (incl. correct options) / update / 204 |
| POST | `/api/admin/quizzes/{id}/publish` · `/unpublish` | → quiz |
| POST | `/api/admin/quizzes/{id}/questions` | `{text, optionA, optionB, optionC, optionD, correctOption}` → 201 question |
| PUT / DELETE | `/api/admin/questions/{qid}` | update / 204 |
| GET | `/api/admin/quizzes/{id}/results` | → `[{studentName, score, total, timeTakenSec, submittedAt, autoSubmitted}]` |

## Student
| Method | Path | Body → Response |
|---|---|---|
| GET | `/api/quizzes` | → published quizzes |
| POST | `/api/quizzes/{id}/start` | → `{attemptId, quizTitle, timeLimitSec, secondsLeft, questions:[{id, text, optionA..D}], savedAnswers:{"12":"B"}}` (shuffled, **no correct answers**). Resumes the student's unfinished attempt if time is left. |
| PUT | `/api/attempts/{id}/progress` | `{answers:{"12":"B"}}` → `{saved: n}` (auto-save; 400 "Time is over" after deadline + 30 s) |
| POST | `/api/attempts/{id}/submit` | `{answers:{…}, autoSubmitted}` → `{attemptId, quizId, quizTitle, score, total, percentage, correct, wrong, unanswered, timeTakenSec, autoSubmitted, submittedAt}` |
| GET | `/api/attempts/{id}/review` | → `[{text, optionA..D, chosen, correct}]` (owner or admin, only after submit) |
| GET | `/api/me/attempts` | → own submitted results (same shape as submit) |

Submit is safe to repeat (returns the stored result). A submit arriving more than 30 s after the time limit is
still accepted, but only the answers auto-saved in time are graded and it is marked auto-submitted.
An unfinished attempt whose time ran out is graded automatically when the student starts that quiz again.

## LiveState
```json
{"quizId":3, "quizTitle":"Java Basics", "totalQuestions":10,
 "writing":[{"name":"Kiran","answered":6}],
 "leaderboard":[{"rank":1,"name":"Ravi","score":9,"total":10,"timeTakenSec":252}]}
```
