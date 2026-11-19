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
| POST | `/api/quizzes/{id}/start` | → `{attemptId, timeLimitSec, questions:[{id, text, optionA..D}]}` (shuffled, **no correct answers**) |
| PUT | `/api/attempts/{id}/progress` | `{answers:{"12":"B"}}` → 204 (saves answers, pushes live update) |
| POST | `/api/attempts/{id}/submit` | `{answers:{…}, autoSubmitted}` → `{score, total, percentage, correct, wrong, unanswered, timeTakenSec}` |
| GET | `/api/attempts/{id}/review` | → `[{text, options, chosen, correct}]` (only after submit) |
| GET | `/api/me/attempts` | → own history |

A submit arriving more than 30 s after the time limit → 409 "Time is over".

## LiveState
```json
{"quizId":3, "quizTitle":"Java Basics", "totalQuestions":10,
 "writing":[{"name":"Kiran","answered":6}],
 "leaderboard":[{"rank":1,"name":"Ravi","score":9,"total":10,"timeTakenSec":252}]}
```
