# Controller API contract

## Authorization
All endpoints require JWT token of user with role `controller`.

- `401 Unauthorized` — missing/invalid token.
- `403 Forbidden` — token is valid but role is not `controller`.

## GET `/controller/candidates`
Returns candidates with account or saved results (for current model this is all users with role `candidate`).

### Response `200 OK`
```json
[
  {
    "candidateId": "uuid",
    "fullName": "Иван Иванов",
    "email": "ivan@example.com",
    "completedSessionsCount": 3,
    "lastCompletedAt": "2026-02-01T10:15:30Z"
  }
]
```

## GET `/controller/candidates/{candidateId}/results`
Returns completed sessions of selected candidate ordered by completion time (desc).

### Response `200 OK`
```json
{
  "candidateId": "uuid",
  "fullName": "Иван Иванов",
  "sessions": [
    {
      "sessionId": "uuid",
      "completedAt": "2026-02-01T10:15:30Z",
      "summary": "Сильная сторона: ...",
      "scores": {
        "attention": 75.0,
        "stressResistance": 62.5,
        "responsibility": 80.0,
        "adaptability": 55.0,
        "decisionSpeedAccuracy": 70.0
      }
    }
  ]
}
```

### Errors
- `404 Not Found` — candidate not found.
- `400 Bad Request` — user exists but is not candidate.

## GET `/controller/results/{sessionId}`
Returns detailed result profile (same shape as candidate endpoint `/me/results/{sessionId}`).

### Response `200 OK`
```json
{
  "sessionId": "uuid",
  "completedAt": "2026-02-01T10:15:30Z",
  "scores": {
    "attention": 75.0,
    "stressResistance": 62.5,
    "responsibility": 80.0,
    "adaptability": 55.0,
    "decisionSpeedAccuracy": 70.0
  },
  "interpretations": {
    "attention": "...",
    "stressResistance": "...",
    "responsibility": "...",
    "adaptability": "...",
    "decisionSpeedAccuracy": "..."
  },
  "overallSummary": "..."
}
```

### Errors
- `404 Not Found` — completed result profile does not exist for session.
- `409 Conflict` — session exists but not completed.
