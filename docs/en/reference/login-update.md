# Login Feature Update

This page is the English entry for the login implementation update.

- Full Chinese details: [`/docs/登录功能更新说明.md`](../../登录功能更新说明.md)

## Summary

- Added login endpoint: `POST /api/auth/login`
- Successful login returns `JWT + user profile`
- Admin account is initialized from environment variables
- Auto registration is disabled in current phase
- Frontend supports persisted login session and logout

