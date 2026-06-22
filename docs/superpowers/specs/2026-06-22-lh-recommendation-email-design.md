# LH Recommendation Email Design

## Goal

Build an operating-grade backend flow that recommends LH rental notices from a member's saved financial profile and sends email reminders for the member's favorite LH notices.

## Scope

- Add favorite storage for LH rental notices, separate from favorite apartment deals.
- Add authenticated APIs to toggle favorites, list favorites, fetch recommendations, and manually trigger favorite notice email checks for the current user.
- Score cached or recently fetched LH notices using the member financial profile, notice status, dates, region/type signals, and supply expected amount text.
- Send scheduled emails for favorite notices when an application period is upcoming, active, or closing soon.
- Record email logs so each user, notice, and event type is sent once.

## Architecture

- `rental.favorite` owns favorite LH notice persistence and favorite-facing APIs.
- `rental.recommendation` owns deterministic scoring and recommendation DTOs.
- `rental.email` owns notification event detection, duplicate-send checks, mail delivery, and the daily scheduler.
- Existing `RentalService` remains the source of LH notice search/detail data, and existing member/financial profile services remain the source of user data.

## Data Model

- `favorite_rental_notices(user_id, notice_id, created_at)` stores member favorites.
- `rental_notice_email_logs(email_log_id, user_id, notice_id, event_type, recipient_email, subject, sent_at)` stores successful sends and prevents duplicates.

## API

- `GET /api/rentals/favorites`: list current member's favorite LH notices.
- `POST /api/rentals/{noticeId}/favorite/toggle`: toggle a favorite LH notice.
- `GET /api/rentals/recommendations`: return ranked recommendations for the current member.
- `POST /api/rentals/favorites/emails/send`: run the email reminder check for the current member.

## Email Rules

- `APPLY_OPEN`: application starts today.
- `APPLY_ACTIVE`: application is active and has not been emailed before.
- `CLOSING_SOON`: application ends within three days, including today.
- Dates are parsed from the existing LH detail date strings using numeric year/month/day extraction.
- If a notice lacks valid dates, no scheduled email is sent for it.

## Testing

- Unit-test recommendation scoring with real DTOs and fake collaborators.
- Unit-test favorite toggle/list behavior.
- Unit-test email event detection and duplicate suppression.
- Controller integration tests should verify authentication boundaries once the existing repository compile errors are fixed.
